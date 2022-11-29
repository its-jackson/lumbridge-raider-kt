package scripts.kt.lumbridge.raider.api.behaviors.combat

import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.frameworks.behaviortree.*
import scripts.kotlin.api.eatingAction
import scripts.kotlin.api.isLootableItemsFound
import scripts.kotlin.api.lootItems
import scripts.kt.lumbridge.raider.api.ScriptTask
import scripts.kt.lumbridge.raider.api.behaviors.banking.executeBankTask

fun IParentNode.completeCombatAction(scriptTask: ScriptTask?) = sequence {
    // ensure the inventory is in good state
    executeBankTask(
        scriptTask = scriptTask,
        bankCondition = {
            scriptTask?.scriptBankTask?.isSatisfied() == false ||
                    (scriptTask?.scriptCombatData?.lootGroundItems == true && Inventory.isFull())
        }
    )

    // complete the combat action
    sequence {
        // ensure the monster location is nearby and reachable
        selector {
            condition { scriptTask?.scriptCombatData?.monsters?.any { it.isCentralPositionNearby() && it.canReachCentralPosition() } }
            condition { scriptTask?.scriptCombatData?.monsters?.any { it.walkToCentralPosition() } }
        }
        // ensure the monster is available and ready to be attacked
        selector {
            condition { scriptTask?.scriptCombatData?.monsters?.any { it.getMonsterNpcQuery().isAny } }
            perform { scriptTask?.scriptCombatData?.monsters?.any { Waiting.waitUntil { it.getMonsterNpcQuery().isAny } } }
        }
        // attack the monster
        selector {
            condition { scriptTask?.scriptCombatData?.monsters?.any { it.isFighting() } }
            perform {
                scriptTask?.scriptCombatData?.monsters?.any {
                    it.attack(actions = listOf { eatingAction().tick() })
                }
            }
        }
    }

    // loot any items that are dropped after monster death
    lootingAction(scriptTask)
}

fun IParentNode.lootingAction(scriptTask: ScriptTask?) = selector {
    condition { scriptTask?.scriptCombatData?.lootGroundItems == false }
    condition { !isLootableItemsFound() }
    condition { lootItems() > 0 }
}