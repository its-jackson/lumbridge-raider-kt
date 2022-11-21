package scripts.kt.lumbridge.raider.api.behaviors.questing.cooks.assistant

import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.Log
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.frameworks.behaviortree.*
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.types.GroundItem
import org.tribot.script.sdk.types.WorldTile
import scripts.kotlin.api.canReach
import scripts.kotlin.api.performKill
import scripts.kotlin.api.waitUntilNotAnimating
import scripts.kotlin.api.walkTo
import scripts.kt.lumbridge.raider.api.Behavior
import scripts.kt.lumbridge.raider.api.ScriptTask
import scripts.kt.lumbridge.raider.api.behaviors.banking.walkToAndDepositInvBank
import scripts.kt.lumbridge.raider.api.behaviors.combat.Monster
import scripts.kt.lumbridge.raider.api.behaviors.cooking.Range
import scripts.kt.lumbridge.raider.api.behaviors.questing.Quest

private const val ITEM_1 = "Bucket"
private const val ITEM_2 = "Pot"
private const val ITEM_3 = "Egg"
private const val ITEM_4 = "Bucket of milk"
private const val ITEM_5 = "Pot of flour"

private val bucketTile = WorldTile(3216, 9624, 0)
private val potTile = Range.LUMBRIDGE_CASTLE_RANGE.position
private val eggTile = Monster.CHICKEN_LUMBRIDGE_EAST.centralPosition
private val bucketOfMilkTile = Monster.COW_LUMBRIDGE_EAST.centralPosition
private val wheatTile = WorldTile(3158, 3300, 0)
private val flourBinTile = WorldTile(3166, 3308, 0)
private val hopperTile = flourBinTile.translate(0, 0, 2)

fun IParentNode.cooksAssistantBehavior(
    scriptTask: ScriptTask?,
    bag: MutableSet<String> = mutableSetOf()
) = sequence {
    condition { scriptTask?.behavior == Behavior.QUESTING }
    condition { scriptTask?.questingData?.quest?.let { it == Quest.COOKS_ASSISTANT } }

    // clean inventory for quest
    selector {
        condition { Inventory.getEmptySlots() >= 4 }
        walkToAndDepositInvBank()
    }

    // get item_1
    selector {
        condition { bag.contains(ITEM_1) }
        sequence {
            walkToAndTakeGroundItem(
                position = bucketTile,
                itemName = ITEM_1
            )
            perform { bag.add(ITEM_1) }
        }
    }

    // get item_2
    selector {
        condition { bag.contains(ITEM_2) }
        sequence {
            walkToAndTakeGroundItem(
                position = potTile,
                itemName = ITEM_2
            )
            perform { bag.add(ITEM_2) }
        }
    }

    // get item_3
    selector {
        condition { bag.contains(ITEM_3) }
        sequence {
            walkToAndTakeGroundItem(
                position = eggTile,
                itemName = ITEM_3
            )
            perform { bag.add(ITEM_3) }
        }
    }

    // get item_4
    selector {
        condition { bag.contains(ITEM_4) }
        sequence {
            walkToAndMilkCow()
            perform { bag.add(ITEM_4) }
        }
    }

    // get item_5
    selector {
        condition { bag.contains(ITEM_5) }
        sequence {
            walkToAndPickWheat()
            walkToAndFillHopper()
            walkToAndPullHopperLever()
            walkToAndEmptyFlourBin()
            perform { bag.add(ITEM_5) }
        }
    }

    // complete the quest / turn it in
    finishQuest(scriptTask)

    // end task session
    performKill { Log.debug("Quest completed successfully: ${scriptTask?.questingData?.quest}") }
}

private fun IParentNode.walkToAndTakeGroundItem(
    position: WorldTile,
    itemName: String
) = sequence {
    selector {
        condition { canReach(position) }
        condition { walkTo(position) }
    }
    condition {
        Query.groundItems()
            .nameEquals(itemName)
            .findBestInteractable()
            .map {
                takeAction(
                    item = itemName,
                    groundItem = it
                )
            }
            .orElse(false)
    }
}

private fun takeAction(
    action: String = "Take",
    item: String,
    groundItem: GroundItem
): Boolean = groundItem.interact(action) &&
        Waiting.waitUntil { Inventory.contains(item) }

private fun IParentNode.walkToAndMilkCow() = sequence {
    selector {
        condition { canReach(bucketOfMilkTile) }
        condition { walkTo(bucketOfMilkTile) }
    }
    condition {
        Query.gameObjects()
            .nameEquals("Dairy cow")
            .findBestInteractable()
            .map {
                it.interact("Milk") && // milk me daddy
                        Waiting.waitUntilAnimating(10000) &&
                        waitUntilNotAnimating() && Waiting.waitUntil { Inventory.contains(ITEM_4) }
            }
            .orElse(false)
    }
}

private fun IParentNode.walkToAndPickWheat() = sequence {
    selector {
        condition { canReach(wheatTile) }
        condition { walkTo(wheatTile) }
    }
    condition {
        Query.gameObjects()
            .nameEquals("Wheat")
            .findBestInteractable()
            .map {
                it.interact("Pick") &&
                        Waiting.waitUntil { Inventory.contains("Grain") }
            }
            .orElse(false)
    }
}

private fun IParentNode.walkToAndFillHopper() = sequence {
    selector {
        condition { canReach(hopperTile) }
        condition { walkTo(hopperTile) }
    }
    condition {
        Query.gameObjects()
            .nameEquals("Hopper")
            .findBestInteractable()
            .map {
                it.interact("Fill") &&
                        Waiting.waitUntilAnimating(7500) &&
                        waitUntilNotAnimating() &&
                        Waiting.waitUntil { !Inventory.contains("Grain") }
            }
            .orElse(false)
    }
}

private fun IParentNode.walkToAndPullHopperLever() = sequence {
    selector {
        condition { canReach(hopperTile) }
        condition { walkTo(hopperTile) }
    }
    condition {
        Query.gameObjects()
            .nameEquals("Hopper controls")
            .findBestInteractable()
            .map {
                it.interact("Operate") &&
                        Waiting.waitUntilAnimating(7500) &&
                        waitUntilNotAnimating()
            }
            .orElse(false)
    }
}

private fun IParentNode.walkToAndEmptyFlourBin() = sequence {
    selector {
        condition { canReach(flourBinTile) }
        condition { walkTo(flourBinTile) }
    }
    condition {
        Query.gameObjects()
            .nameEquals("Flour bin")
            .findBestInteractable()
            .map {
                it.interact("Empty") &&
                        Waiting.waitUntil { Inventory.contains(ITEM_5) }
            }
            .orElse(false)
    }
}

private fun IParentNode.finishQuest(scriptTask: ScriptTask?) = sequence {
    repeatUntil({ Query.widgets().textContains("Congratulations!").isAny }) {
        sequence {
            selector {
                condition { scriptTask?.questingData?.quest?.let { it.isQuestNpcPositionNearby() && it.canReachQuestNpcPosition() } }
                condition { scriptTask?.questingData?.quest?.walkToQuestNpcPosition() }
            }
            condition { scriptTask?.questingData?.quest?.handleQuestNpcDialog() }
        }
    }
}