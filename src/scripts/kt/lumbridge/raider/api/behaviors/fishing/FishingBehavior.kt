package scripts.kt.lumbridge.raider.api.behaviors.fishing

import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.Waiting.waitUntilAnimating
import org.tribot.script.sdk.frameworks.behaviortree.*
import org.tribot.script.sdk.frameworks.behaviortree.nodes.SequenceNode
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.tasks.Amount
import org.tribot.script.sdk.tasks.BankTask
import scripts.kt.lumbridge.raider.api.Behavior
import scripts.kt.lumbridge.raider.api.Disposal
import scripts.kt.lumbridge.raider.api.ScriptTask
import scripts.kt.lumbridge.raider.api.behaviors.banking.walkToAndOpenBank
import scripts.kt.lumbridge.raider.api.behaviors.canReach
import scripts.kt.lumbridge.raider.api.behaviors.cooking.isCookRawFood
import scripts.kt.lumbridge.raider.api.behaviors.cooking.walkToAndCookRange
import scripts.kt.lumbridge.raider.api.behaviors.walkTo
import scripts.waitUntilNotAnimating

/**
 * The Fishing Behavior SequenceNode:
 *  Initialize bank task,
 *  Getting the required fishing equipment/bait,
 *  Inventory disposal methods,
 *  Walking,
 *  Interacting,
 *  Waiting
 *
 * Fish spots covered thus far:
 *  Lumbridge Swamp - shrimps, anchovies, sardine, herring
 *  Lumbridge Castle - salmon, trout, pike
 *
 * Disposal methods covered thus far:
 *  Normal banking,
 *  Normal dropping,
 *  Cook then bank,
 *  Cook then drop
 */
fun IParentNode.fishingBehavior(scriptTask: ScriptTask?): SequenceNode = sequence("Fishing behavior") {
    // ensure character behavior is fishing
    condition { scriptTask?.behavior == Behavior.FISHING }

    // ensure the character has a disposal method
    condition { scriptTask?.disposal != null }

    // ensure the bank task is initialized and the inventory is in good state
    selector {
        condition { scriptTask?.bankTask != null }
        sequence {
            perform { initBankTask(scriptTask) }
            repeatUntil({ scriptTask?.bankTask?.isSatisfied() == true }) {
                sequence {
                    walkToAndOpenBank()
                    condition { scriptTask?.bankTask?.execute()?.isEmpty }
                }
            }
        }
    }

    // restock fishing equipment and bait from bank
    selector {
        condition { isFishingEquipmentSatisfied(scriptTask) }
        sequence {
            walkToAndOpenBank()
            condition { scriptTask?.bankTask?.execute()?.isEmpty }
        }
    }

    // normal banking disposal - WORKING
    selector {
        condition { scriptTask?.disposal != Disposal.BANK }
        condition { !Inventory.isFull() }
        sequence {
            walkToAndOpenBank()
            condition { scriptTask?.bankTask?.execute()?.isEmpty }
        }
    }

    // normal dropping - WORKING
    selector {
        condition { scriptTask?.disposal != Disposal.DROP }
        condition { !Inventory.isFull() }
        condition { dropAll(scriptTask) }
    }

    // cook then bank disposal - WORKING
    selector {
        condition { scriptTask?.disposal != Disposal.COOK_THEN_BANK }
        condition { !Inventory.isFull() }
        selector {
            sequence {
                condition { !isCookRawFood() }
                walkToAndOpenBank()
                condition { scriptTask?.bankTask?.execute()?.isEmpty }
            }
            sequence {
                condition { isCookRawFood() }
                walkToAndCookRange()
                condition { !isCookRawFood() }
                walkToAndOpenBank()
                condition { scriptTask?.bankTask?.execute()?.isEmpty }
            }
        }
    }

    // cook then drop disposal - WORKING
    selector {
        condition { scriptTask?.disposal != Disposal.COOK_THEN_DROP }
        condition { !Inventory.isFull() }
        selector {
            sequence {
                condition { !isCookRawFood() }
                condition { dropAll(scriptTask) }
            }
            sequence {
                condition { isCookRawFood() }
                walkToAndCookRange()
                condition { !isCookRawFood() }
                condition { dropAll(scriptTask) }
            }
        }
    }

    // ensure the fishing spot is nearby and reachable
    selector {
        condition { scriptTask?.fishSpot?.let { canReach(it.position) && it.position.distance() < 15 } }
        condition { scriptTask?.fishSpot?.let { walkTo(it.position) } }
    }

    // interact with the fishing spot (walk to, rotate camera, and click)
    condition { completeFishingAction(scriptTask) }
}

/**
 * Find a reachable fish spot then attempt to interact with it.
 *  (Rotate camera, walking, and clicking).
 */
private fun completeFishingAction(scriptTask: ScriptTask?): Boolean = Query.npcs()
    .idEquals(*scriptTask?.fishSpot?.ids ?: intArrayOf())
    .findBestInteractable()
    .map { fishSpot ->
        if (!canReach(fishSpot))
            return@map walkTo(fishSpot)
        fishSpot.actions
            .any { action ->
                scriptTask?.fishSpot?.actions?.let {
                    it.contains(action) && fishSpot.interact(action) &&
                            waitUntilAnimating(10000) && waitUntilNotAnimating(end = 2000)
                } ?: false
            }
    }
    .orElse(false)

/**
 * Determine if the character has the required bait / net etc.
 */
private fun isFishingEquipmentSatisfied(scriptTask: ScriptTask?): Boolean =
    scriptTask?.fishSpot?.equipmentReq?.entries
        ?.all { entry ->
            Inventory.getCount(entry.key) >= entry.value &&
                    (scriptTask.fishSpot.baitReq.isEmpty() ||
                            scriptTask.fishSpot.baitReq.entries
                                .all { bait -> Inventory.getCount(bait.key) >= bait.value })
        } == true

/**
 * Drop all items that are NOT the fishing equipment OR bait.
 */
private fun dropAll(scriptTask: ScriptTask?): Boolean {
    val toDrop = Inventory.getAll()
        .filter {
            scriptTask?.fishSpot?.equipmentReq
                ?.containsKey(it.id) == false &&
                    !scriptTask.fishSpot.baitReq
                        .containsKey(it.id)
        }
        .map { it.id }

    return Inventory.drop(*toDrop.toIntArray()) > 0
}

/**
 * The necessary items for the active task.
 */
private fun initBankTask(scriptTask: ScriptTask?) {
    val bankTaskBuilder = BankTask.builder()

    scriptTask?.fishSpot?.equipmentReq?.entries
        ?.forEach { equipment -> bankTaskBuilder.addInvItem(equipment.key, Amount.of(equipment.value)) }
    scriptTask?.fishSpot?.baitReq?.entries
        ?.forEach { bait -> bankTaskBuilder.addInvItem(bait.key, Amount.fill(bait.value)) }

    scriptTask?.bankTask = bankTaskBuilder.build()
}