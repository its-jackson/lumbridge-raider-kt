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
        g.uniform(300, 2600)
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
        condition { scriptTask?.combatData?.attackStyle == null }
        condition { scriptTask?.combatData?.attackStyle?.let { Combat.isAttackStyleSet(it) } }
        sequence {
            condition { scriptTask?.combatData?.attackStyle?.let { Combat.isAttackStyleAvailable(it) } }
            condition { scriptTask?.combatData?.attackStyle?.let { Combat.setAttackStyle(it) } }
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
            condition { scriptTask?.combatData?.monsters?.any { it.isCentralPositionNearby() && it.canReachCentralPosition() } }
            condition { scriptTask?.combatData?.monsters?.any { it.walkToCentralPosition() } }
        }
        // ensure the monster is available and ready to be attacked
        selector {
            condition { scriptTask?.combatData?.monsters?.any { it.getMonsterNpcQuery().isAny } }
            perform { scriptTask?.combatData?.monsters?.any { Waiting.waitUntil { it.getMonsterNpcQuery().isAny } } }
        }
        // attack the monster, wait, loot
        selector {
            condition { scriptTask?.combatData?.monsters?.any { it.isFighting() } }
            perform {
                scriptTask?.combatData?.monsters?.any {
                    it.attack(actions = listOf { eatingAction().tick() })
                }
            }
        }
    }

    // do the looting stuff
    lootingAction(scriptTask)

    // wait using seeded normal dist
    perform { Waiting.waitNormal(combatWaitMean, combatWaitStd) }
}

fun IParentNode.lootingAction(scriptTask: ScriptTask?) = selector {
    condition { scriptTask?.combatData?.lootGroundItems == false }
    condition { !isLootableItemsFound() }
    condition { lootItems() > 0 }
}

private fun getBankingConditionList(scriptTask: ScriptTask?) = listOf(
    {
        scriptTask?.combatData?.lootGroundItems == true && Inventory.isFull()
    },

    {
        scriptTask?.combatData?.inventoryMap?.isNotEmpty() == true &&
                scriptTask.combatData.inventoryMap?.any { Inventory.contains(it.key) } == false
    },

    {
        scriptTask?.combatData?.equipmentItems?.isNotEmpty() == true &&
                scriptTask.combatData.equipmentItems?.any { Equipment.contains(it.id) } == false
    },

    {
        scriptTask?.combatData?.inventoryMap?.isNotEmpty() == true &&
                scriptTask.combatData.inventoryMap
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