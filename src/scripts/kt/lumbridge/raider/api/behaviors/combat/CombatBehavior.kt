package scripts.kt.lumbridge.raider.api.behaviors.combat

import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.frameworks.behaviortree.*
import scripts.kotlin.api.isLootableItemsFound
import scripts.kotlin.api.lootItems
import scripts.kt.lumbridge.raider.api.ScriptTask
import scripts.kt.lumbridge.raider.api.behaviors.banking.executeBankTask

fun IParentNode.completeCombatAction(scriptTask: ScriptTask?) = sequence {
    // ensure the inventory is in good state
    executeBankTask(
        scriptTask = scriptTask,
        bankCondition = {
            scriptTask?.bankTask?.isSatisfied() == false ||
                    (scriptTask?.combatData?.lootGroundItems == true && Inventory.isFull())
        }
    )

    // complete the combat action
    sequence {
        // ensure the monster location is nearby and reachable
        selector {
            condition { scriptTask?.combatData?.monsters?.any { it.isCentralPositionNearby() && it.canReachCentralPosition() } }
            condition { scriptTask?.combatData?.monsters?.any { it.walkToCentralPosition() } }
        }
        // ensure the monster is available and ready to be attacked
        selector {
            condition { scriptTask?.combatData?.monsters?.any { it.getMonsterNpcQuery().isAny } }
            perform { scriptTask?.combatData?.monsters?.any { Waiting.waitUntil { it.getMonsterNpcQuery().isAny } } }
        }
        // attack the monster
        selector {
            // TODO Eating food inside the inventory that is "Eatable" while in combat
            condition { scriptTask?.combatData?.monsters?.any { it.isFighting() } }
            perform { scriptTask?.combatData?.monsters?.any { it.attack() } }
        }
    }

    // loot any items that are dropped after monster death
    lootingAction(scriptTask)
}

private fun IParentNode.lootingAction(scriptTask: ScriptTask?) = selector {
    condition { scriptTask?.combatData?.lootGroundItems == false }
    condition { Inventory.isFull() }
    condition { scriptTask?.combatData?.monsters?.none { isLootableItemsFound(it.monsterLootableGroundItems) } }
    condition { scriptTask?.combatData?.monsters?.any { lootItems(it.monsterLootableGroundItems) > 0 } }
}