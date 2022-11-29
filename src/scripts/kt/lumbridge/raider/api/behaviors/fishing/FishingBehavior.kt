package scripts.kt.lumbridge.raider.api.behaviors.fishing

import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.Waiting.waitUntil
import org.tribot.script.sdk.frameworks.behaviortree.*
import org.tribot.script.sdk.frameworks.behaviortree.nodes.SequenceNode
import scripts.kotlin.api.canReach
import scripts.kotlin.api.walkTo
import scripts.kotlin.api.walkToAndOpenBank
import scripts.kt.lumbridge.raider.api.ScriptDisposal
import scripts.kt.lumbridge.raider.api.ScriptTask
import scripts.kt.lumbridge.raider.api.behaviors.banking.executeBankTask
import scripts.kt.lumbridge.raider.api.behaviors.banking.initializeBankTask
import scripts.kt.lumbridge.raider.api.behaviors.banking.normalBankingDisposal
import scripts.kt.lumbridge.raider.api.behaviors.cooking.isCookRawFood
import scripts.kt.lumbridge.raider.api.behaviors.cooking.walkToAndCookRange

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
fun IParentNode.fishingBehavior(scriptTask: ScriptTask?): SequenceNode = sequence {
    // ensure the bank task is initialized and the inventory is in good state
    initializeBankTask(scriptTask)

    // restock fishing equipment and bait from bank
    selector {
        executeBankTask(
            scriptTask = scriptTask,
            bankCondition = { !isFishingEquipmentSatisfied(scriptTask) }
        )
    }

    // normal banking disposal - WORKING
    normalBankingDisposal(scriptTask)

    // normal dropping - WORKING
    selector {
        condition { scriptTask?.scriptDisposal != ScriptDisposal.DROP }
        condition { !Inventory.isFull() }
        condition { dropAll(scriptTask) }
    }

    // cook then bank disposal - WORKING
    selector {
        condition { scriptTask?.scriptDisposal != ScriptDisposal.COOK_THEN_BANK }
        condition { !Inventory.isFull() }
        selector {
            sequence {
                condition { !isCookRawFood() }
                walkToAndOpenBank()
                condition { scriptTask?.scriptBankTask?.execute()?.isEmpty }
            }
            sequence {
                condition { isCookRawFood() }
                walkToAndCookRange()
                condition { !isCookRawFood() }
                walkToAndOpenBank()
                condition { scriptTask?.scriptBankTask?.execute()?.isEmpty }
            }
        }
    }

    // cook then drop disposal - WORKING
    selector {
        condition { scriptTask?.scriptDisposal != ScriptDisposal.COOK_THEN_DROP }
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
        condition { scriptTask?.scriptFishingData?.fishSpot?.let { it.position.distance() < 15 && canReach(it.position) } }
        condition { scriptTask?.scriptFishingData?.fishSpot?.let { walkTo(it.position) } }
    }

    // interact with the fishing spot (walk to, rotate camera, and click)
    completeFishingAction(scriptTask)
}

/**
 * Find a reachable fish spot then attempt to interact with it.
 *  (Rotate camera, walking, and clicking).
 */
private fun IParentNode.completeFishingAction(scriptTask: ScriptTask?) = sequence {
    selector {
        condition { scriptTask?.scriptFishingData?.fishSpot?.getFishSpotQuery()?.isAny }
        perform { waitUntil { scriptTask?.scriptFishingData?.fishSpot?.getFishSpotQuery()?.isAny == true } }
    }
    condition { scriptTask?.scriptFishingData?.fishSpot?.fish() }
}


/**
 * Determine if the character has the required bait / net etc.
 */
private fun isFishingEquipmentSatisfied(scriptTask: ScriptTask?): Boolean =
    scriptTask?.scriptFishingData?.fishSpot?.equipmentReq?.entries
        ?.all { entry ->
            Inventory.getCount(entry.key) >= entry.value &&
                    (scriptTask.scriptFishingData.fishSpot.baitReq.isEmpty() ||
                            scriptTask.scriptFishingData.fishSpot.baitReq.entries
                                .all { bait -> Inventory.getCount(bait.key) >= bait.value })
        } == true

/**
 * Drop all items that are NOT the fishing equipment OR bait.
 */
private fun dropAll(scriptTask: ScriptTask?): Boolean {
    val toDrop = Inventory.getAll()
        .filter {
            scriptTask?.scriptFishingData?.fishSpot?.equipmentReq
                ?.containsKey(it.id) == false &&
                    !scriptTask.scriptFishingData.fishSpot.baitReq
                        .containsKey(it.id)
        }
        .map { it.id }

    return Inventory.drop(*toDrop.toIntArray()) > 0
}