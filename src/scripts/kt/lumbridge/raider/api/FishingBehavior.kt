package scripts.kt.lumbridge.raider.api

import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.frameworks.behaviortree.*
import org.tribot.script.sdk.frameworks.behaviortree.nodes.SequenceNode
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.tasks.Amount
import org.tribot.script.sdk.tasks.BankTask
import scripts.waitUntilNotAnimating

/**
 * The Fishing Behavior SequenceNode:
 *
 * Get the fishing items,
 *  inventory disposal methods,
 *  walking,
 *  interacting,
 *  and waiting.
 *
 * Fish spots covered thus far:
 *  Lumbridge Swamp - shrimps/anchovies, sardine/herring
 *  Lumbridge Castle - salmon/trout, pike
 *
 */
fun IParentNode.fishingBehavior(scriptTask: ScriptTask?): SequenceNode = sequence("Fishing behavior") {
    // ensure character behavior is fishing
    condition { scriptTask?.behavior == Behavior.FISHING }

    // ensure the bank task is initialized
    selector {
        condition { scriptTask?.bankTask != null }
        sequence {
            walkToAndOpenBank()
            perform {
                val bankTaskBuilder = BankTask.builder()
                scriptTask?.fishSpot?.equipmentReq?.entries
                    ?.forEach { equipment -> bankTaskBuilder.addInvItem(equipment.key, Amount.of(equipment.value)) }
                scriptTask?.fishSpot?.baitReq?.entries
                    ?.forEach { bait -> bankTaskBuilder.addInvItem(bait.key, Amount.fill(bait.value)) }
                scriptTask?.bankTask = bankTaskBuilder.build()
            }
            condition { scriptTask?.bankTask?.execute()?.isEmpty }
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

    // normal banking disposal
    selector {
        condition { scriptTask?.bankDisposal == false }
        condition { !Inventory.isFull() }
        sequence {
            walkToAndOpenBank()
            condition { scriptTask?.bankTask?.execute()?.isEmpty }
        }
    }

    // cook then bank disposal
    selector {
        inverter { condition { scriptTask?.cookThenBankDisposal == true } }
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

    // ensure the fishing spot is nearby and reachable
    selector {
        condition { scriptTask?.fishSpot?.let { canReachTile(it.position) } }
        condition { scriptTask?.fishSpot?.let { walkTo(it.position) } }
    }

    // interact with the fishing spot and start animating
    sequence {
        // interact with the fishing spot
        condition {
            Query.npcs()
                .idEquals(*scriptTask?.fishSpot?.ids ?: intArrayOf())
                .isReachable
                .findBestInteractable()
                .map { fishSpot ->
                    fishSpot.actions
                        .any { action ->
                            scriptTask?.fishSpot?.actions?.let {
                                it.contains(action) && fishSpot.interact(action)
                            } ?: false
                        }
                }
                .orElse(false)
        }

        // wait until start animating
        condition { Waiting.waitUntilAnimating(7500) }

        // wait until not animating
        condition { waitUntilNotAnimating(end = 2000) }
    }
}