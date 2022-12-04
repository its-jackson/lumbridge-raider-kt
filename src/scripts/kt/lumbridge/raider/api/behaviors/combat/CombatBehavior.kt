package scripts.kt.lumbridge.raider.api.behaviors.combat

import org.tribot.script.sdk.Combat
import org.tribot.script.sdk.Equipment
import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.antiban.PlayerPreferences
import org.tribot.script.sdk.frameworks.behaviortree.*
import org.tribot.script.sdk.query.Query
import scripts.kotlin.api.eatingAction
import scripts.kotlin.api.isLootableItemsFound
import scripts.kotlin.api.lootItems
import scripts.kt.lumbridge.raider.api.ScriptTask
import scripts.kt.lumbridge.raider.api.behaviors.banking.executeBankTask
import scripts.kt.lumbridge.raider.api.behaviors.banking.initializeBankTask

private val combatWaitMean: Int =
    PlayerPreferences.preference(
        "scripts.kt.lumbridge.raider.api.behaviors.combat.CombatBehavior.combatWaitMean"
    )
    { g: PlayerPreferences.Generator ->
        g.uniform(300, 3000)
    }

private val combatWaitStd: Int =
    PlayerPreferences.preference(
        "scripts.kt.lumbridge.raider.api.behaviors.combat.CombatBehavior.combatWaitStd"
    ) { g: PlayerPreferences.Generator ->
        g.uniform(30, 60)
    }

fun IParentNode.combatBehavior(scriptTask: ScriptTask?) = sequence {
    // ensure the bank task is initialized,
    // and the character has the correct items/equipment
    initializeBankTask(scriptTask)

    // execute the complete combat action {
    //  walking,
    //  waiting,
    //  attacking,
    //  banking,
    //  looting,
    //  get hyper,
    // }
    completeCombatAction(scriptTask)
}

fun IParentNode.completeCombatAction(scriptTask: ScriptTask?) = sequence {
    // make sure auto retaliate is always on...
    selector {
        condition { Combat.isAutoRetaliateOn() }
        perform { Combat.setAutoRetaliate(true) }
    }

    // ensure the attack style is selected
    selector {
        condition { scriptTask?.scriptCombatData?.attackStyle == null }
        condition { scriptTask?.scriptCombatData?.attackStyle?.let { Combat.isAttackStyleSet(it) } }
        sequence {
            condition { scriptTask?.scriptCombatData?.attackStyle?.let { Combat.isAttackStyleAvailable(it) } }
            condition { scriptTask?.scriptCombatData?.attackStyle?.let { Combat.setAttackStyle(it) } }
        }
    }

    // ensure the inventory is in good state
    executeBankTask(
        bankingConditions = getBankingConditionList(scriptTask),
        scriptTask = scriptTask
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
        // attack the monster, wait, loot
        selector {
            condition { scriptTask?.scriptCombatData?.monsters?.any { it.isFighting() } }
            perform {
                scriptTask?.scriptCombatData?.monsters?.any {
                    it.attack(actions = listOf { eatingAction().tick() })
                }
            }
        }
    }

    // wait using seeded normal dist
    perform { Waiting.waitNormal(combatWaitMean, combatWaitStd) }

    // do the looting stuff
    lootingAction(scriptTask)
}

fun IParentNode.lootingAction(scriptTask: ScriptTask?) = selector {
    condition { scriptTask?.scriptCombatData?.lootGroundItems == false }
    condition { !isLootableItemsFound() }
    condition { lootItems() > 0 }
}

private fun getBankingConditionList(scriptTask: ScriptTask?) = listOf(
    {
        scriptTask?.scriptCombatData?.lootGroundItems == true && Inventory.isFull()
    },

    {
        scriptTask?.scriptCombatData?.inventoryMap?.isNotEmpty() == true &&
                scriptTask.scriptCombatData.inventoryMap?.any { Inventory.contains(it.key) } == false
    },

    {
        scriptTask?.scriptCombatData?.equipmentItems?.isNotEmpty() == true &&
                scriptTask.scriptCombatData.equipmentItems?.any { Equipment.contains(it.id) } == false
    },

    {
        scriptTask?.scriptCombatData?.inventoryMap?.isNotEmpty() == true &&
                scriptTask.scriptCombatData.inventoryMap
                    ?.any {
                        Query.inventory()
                            .idEquals(it.key)
                            .filter { item ->
                                item.name.contains("rune", ignoreCase = true) &&
                                        item.stack <= 10
                            }
                            .isAny
                    } == true
    },
)