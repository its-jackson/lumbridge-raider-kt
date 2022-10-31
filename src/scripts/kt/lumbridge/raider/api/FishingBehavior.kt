package scripts.kt.lumbridge.raider.api

import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.frameworks.behaviortree.*
import org.tribot.script.sdk.frameworks.behaviortree.nodes.SequenceNode
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.tasks.Amount
import org.tribot.script.sdk.tasks.BankTask
import org.tribot.script.sdk.walking.GlobalWalking
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
        condition {
            scriptTask?.fishSpot?.equipmentReq?.entries
                ?.all { entry ->
                    Inventory.getCount(entry.key) >= entry.value &&
                            (scriptTask.fishSpot.baitReq.isEmpty() ||
                                    scriptTask.fishSpot.baitReq.entries
                                        .all { bait -> Inventory.getCount(bait.key) >= bait.value })
                } == true
        }
        sequence {
            walkToAndOpenBank()
            condition { scriptTask?.bankTask?.execute()?.isEmpty }
        }
    }

    // normal banking disposal - WORKING
    selector {
        condition { scriptTask?.bankDisposal == false }
        condition { !Inventory.isFull() }
        sequence {
            walkToAndOpenBank()
            condition { scriptTask?.bankTask?.execute()?.isEmpty }
        }
    }

    // normal dropping - WORKING
    selector {
        condition { scriptTask?.dropDisposal == false }
        condition { !Inventory.isFull() }
        condition { dropAll(scriptTask) }
    }

    // cook then bank disposal - WORKING
    selector {
        condition { scriptTask?.cookThenBankDisposal == false }
        condition { !Inventory.isFull() }
        sequence {
            selector {
                condition { !isCookRawFood() }
                repeatUntil({ !isCookRawFood() }) { walkToAndCookRange() }
            }
            walkToAndOpenBank()
            condition { scriptTask?.bankTask?.execute()?.isEmpty }
        }
    }

    // cook then drop disposal - WORKING
    selector {
        condition { scriptTask?.cookThenDropDisposal == false }
        condition { !Inventory.isFull() }
        condition { !isCookRawFood() }
        sequence {
            repeatUntil({ !isCookRawFood() }) { walkToAndCookRange() }
            condition { dropAll(scriptTask) }
        }
    }

    // ensure the fishing spot is nearby and reachable
    selector {
        condition { scriptTask?.fishSpot?.let { canReach(it.position) && it.position.distance() < 15 } }
        condition { scriptTask?.fishSpot?.let { walkTo(it.position) } }
    }

    // interact with the fishing spot and start animating
    sequence {
        // interact with the fishing spot (walk to, rotate camera, and click)
        condition { interactWithFishSpot(scriptTask) }

        // wait until start animating
        condition { Waiting.waitUntilAnimating(10000) }

        // wait until not animating
        condition { waitUntilNotAnimating(end = 2000) }
    }
}

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
 * Find a reachable fish spot then attempt to interact with it.
 *  (Rotate camera, walking, and clicking).
 */
private fun interactWithFishSpot(scriptTask: ScriptTask?): Boolean = Query.npcs()
    .idEquals(*scriptTask?.fishSpot?.ids ?: intArrayOf())
    .findBestInteractable()
    .map { fishSpot ->
        if (!canReach(fishSpot))
            return@map GlobalWalking.walkTo(fishSpot)
        fishSpot.actions
            .any { action ->
                scriptTask?.fishSpot?.actions?.let {
                    it.contains(action) && fishSpot.interact(action)
                } ?: false
            }
    }
    .orElse(false)

/**
 * The fishing equipment and bait to use for the active task.
 */
private fun initBankTask(scriptTask: ScriptTask?) {
    val bankTaskBuilder = BankTask.builder()

    scriptTask?.fishSpot?.equipmentReq?.entries
        ?.forEach { equipment -> bankTaskBuilder.addInvItem(equipment.key, Amount.of(equipment.value)) }
    scriptTask?.fishSpot?.baitReq?.entries
        ?.forEach { bait -> bankTaskBuilder.addInvItem(bait.key, Amount.fill(bait.value)) }

    scriptTask?.bankTask = bankTaskBuilder.build()
}