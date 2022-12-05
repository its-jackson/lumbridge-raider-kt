package scripts.kt.lumbridge.raider.api.behaviors.fishing

import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.Waiting.waitUntil
import org.tribot.script.sdk.antiban.PlayerPreferences
import org.tribot.script.sdk.frameworks.behaviortree.*
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

private val fishingWaitMean: Int =
    PlayerPreferences.preference(
        "scripts.kt.lumbridge.raider.api.behaviors.fishing.FishingBehavior.fishingWaitMean"
    )
    { g: PlayerPreferences.Generator ->
        g.uniform(200, 4000)
    }

private val fishingWaitStd: Int =
    PlayerPreferences.preference(
        "scripts.kt.lumbridge.raider.api.behaviors.fishing.FishingBehavior.fishingWaitStd"
    ) { g: PlayerPreferences.Generator ->
        g.uniform(20, 30)
    }

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
fun IParentNode.fishingBehavior(scriptTask: ScriptTask?) = sequence {
    // ensure the bank task is initialized and the inventory is in good state
    initializeBankTask(scriptTask)

    // restock fishing equipment and bait from bank
    selector {
        executeBankTask(
            scriptTask = scriptTask,
            bankingConditions = listOf { !isFishingEquipmentSatisfied(scriptTask) }
        )
    }

    // normal banking disposal - WORKING
    normalBankingDisposal(scriptTask)

    // normal dropping - WORKING
    selector {
        condition { scriptTask?.disposal != ScriptDisposal.DROP }
        condition { !Inventory.isFull() }
        condition { dropAll(scriptTask) }
    }

    // cook then bank disposal - WORKING
    selector {
        condition { scriptTask?.disposal != ScriptDisposal.COOK_THEN_BANK }
        condition { !Inventory.isFull() }
        selector {
            sequence {
                condition { !isCookRawFood() }
                walkToAndOpenBank()
                condition { scriptTask?.bankTask?.execute()?.isEmpty }
            }
            sequence {
                condition { isCookRawFood() }
                walkToAndCookRange(scriptTask)
                condition { !isCookRawFood() }
                walkToAndOpenBank()
                condition { scriptTask?.bankTask?.execute()?.isEmpty }
            }
        }
    }

    // cook then drop disposal - WORKING
    selector {
        condition { scriptTask?.disposal != ScriptDisposal.COOK_THEN_DROP }
        condition { !Inventory.isFull() }
        selector {
            sequence {
                condition { !isCookRawFood() }
                condition { dropAll(scriptTask) }
            }
            sequence {
                condition { isCookRawFood() }
                walkToAndCookRange(scriptTask)
                condition { !isCookRawFood() }
                condition { dropAll(scriptTask) }
            }
        }
    }

    // ensure the fishing spot is nearby and reachable
    selector {
        condition { scriptTask?.fishingData?.fishSpot?.let { it.position.distance() < 15 && canReach(it.position) } }
        condition { scriptTask?.fishingData?.fishSpot?.let { walkTo(it.position) } }
    }

    // interact with the fishing spot (walk to, rotate camera, and click)
    completeFishingAction(scriptTask)

    perform { Waiting.waitNormal(fishingWaitMean, fishingWaitStd) }
}

/**
 * Find a reachable fish spot then attempt to interact with it.
 *  (Rotate camera, walking, and clicking).
 */
private fun IParentNode.completeFishingAction(scriptTask: ScriptTask?) = sequence {
    selector {
        condition { scriptTask?.fishingData?.fishSpot?.getFishSpotQuery()?.isAny }
        perform { waitUntil { scriptTask?.fishingData?.fishSpot?.getFishSpotQuery()?.isAny == true } }
    }
    condition { scriptTask?.fishingData?.fishSpot?.fish() }
}


/**
 * Determine if the character has the required bait / net etc.
 */
private fun isFishingEquipmentSatisfied(scriptTask: ScriptTask?) =
    scriptTask?.fishingData?.fishSpot?.equipmentReq?.entries
        ?.all { entry ->
            Inventory.getCount(entry.key) >= entry.value &&
                    (scriptTask.fishingData.fishSpot.baitReq.isEmpty() ||
                            scriptTask.fishingData.fishSpot.baitReq.entries
                                .all { bait -> Inventory.getCount(bait.key) >= bait.value })
        } == true

/**
 * Drop all items that are NOT the fishing equipment OR bait.
 */
private fun dropAll(scriptTask: ScriptTask?): Boolean {
    val toDrop = Inventory.getAll()
        .filter {
            scriptTask?.fishingData?.fishSpot?.equipmentReq
                ?.containsKey(it.id) == false &&
                    !scriptTask.fishingData.fishSpot.baitReq
                        .containsKey(it.id)
        }
        .map { it.id }

    return Inventory.drop(*toDrop.toIntArray()) > 0
}