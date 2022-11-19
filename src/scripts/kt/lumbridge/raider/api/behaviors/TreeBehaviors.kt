package scripts.kt.lumbridge.raider.api.behaviors

import org.tribot.script.sdk.Camera
import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.Login
import org.tribot.script.sdk.Options
import org.tribot.script.sdk.Waiting.waitUntil
import org.tribot.script.sdk.antiban.Antiban
import org.tribot.script.sdk.frameworks.behaviortree.*
import org.tribot.script.sdk.frameworks.behaviortree.nodes.SequenceNode
import org.tribot.script.sdk.query.GroundItemQuery
import org.tribot.script.sdk.query.Query
import scripts.kotlin.api.performKill
import scripts.kotlin.api.scriptControl
import scripts.kt.lumbridge.raider.api.ScriptTask
import scripts.kt.lumbridge.raider.api.behaviors.banking.walkToAndDepositInvBank
import scripts.kt.lumbridge.raider.api.behaviors.combat.combatMeleeBehavior
import scripts.kt.lumbridge.raider.api.behaviors.cooking.cookingBehavior
import scripts.kt.lumbridge.raider.api.behaviors.fishing.fishingBehavior
import scripts.kt.lumbridge.raider.api.behaviors.mining.miningBehavior
import scripts.kt.lumbridge.raider.api.behaviors.woodcutting.woodcuttingBehavior

/**
Composite nodes: sequence and selector, the sequence node behaves as an AND gate.
The selector node behaves as an OR gate.

Decorative nodes: inverter, repeatUntil, succeeder, condition.
inverter: invert the result of the child. A child fails, and it will return success to its parent,
or a child succeeds, and it will return failure to the parent.

condition: ensures that we can skip steps we don't need to do. So if the condition is satisfied,
great! We move on. If not, we do something to satisfy it.

Leaf nodes: perform, terminal node that will always return success.
 */

/**
 * This behavior tree ensures the user is logged in first.
 * Then it will ensure the inventory is empty, before entering the main script logic.
 */
fun initializeScriptBehaviorTree(): IBehaviorNode = behaviorTree {
    repeatUntil(BehaviorTreeStatus.KILL) {
        sequence {
            selector {
                inverter { condition { !Login.isLoggedIn() } }
                condition { Login.login() }
            }
            selector {
                inverter { condition { !Inventory.isEmpty() } }
                walkToAndDepositInvBank()
            }
            performKill { Camera.setZoomPercent(0.00) }
        }
    }
}

/**
 * This behavior tree is the main logic tree for the script.
 * It decides the behaviour of the character based on the active script task.
 * The task session will end if the character fails too many times consecutively each tick.
 */
fun scriptLogicBehaviorTree(scriptTask: ScriptTask?): IBehaviorNode = behaviorTree {
    sequence {
        scriptControl { abstractBehavior(scriptTask) }
        scriptControl { specificBehavior(scriptTask) }
    }
}

/**
 * Carryout the high level character abstraction behaviors.
 *
 * Logging in, turning on character run.
 */
fun IParentNode.abstractBehavior(scriptTask: ScriptTask?): SequenceNode = sequence("Abstract behavior") {
    // character login
    selector {
        condition { Login.isLoggedIn() }
        condition { Login.login() }
    }
    // character running
    selector {
        condition { !Antiban.shouldTurnOnRun() || Options.isRunEnabled() }
        condition { Options.setRunEnabled(true) }
    }
}

/**
 * Carryout the character specific behaviors.
 *
 * Melee, Ranged, Magic, Cooking, Firemaking, Woodcutting, Fishing, Mining, Smithing, and Prayer
 */
fun IParentNode.specificBehavior(scriptTask: ScriptTask?): SequenceNode = sequence("Specific behavior") {
    selector {
        combatMeleeBehavior(scriptTask)
        fishingBehavior(scriptTask)
        cookingBehavior(scriptTask)
        miningBehavior(scriptTask)
        woodcuttingBehavior(scriptTask)
    }
}

fun lootItems(scriptTask: ScriptTask?): Int = lootItems(scriptTask?.npc?.lootableGroundItems ?: arrayOf())

fun lootItems(items: Array<String>): Int = lootableItemsQuery(items)
    .toList()
    .fold(0) { runningSum, item ->
        if (Inventory.isFull()) return runningSum
        val before = Inventory.getCount(item.id)

        if (!item.interact("Take")) return runningSum
        if (!waitUntil(2500) { Inventory.getCount(item.id) > before }) return runningSum

        val result = runningSum + item.stack
        result
    }

fun foundLootableItems(scriptTask: ScriptTask?): Boolean =
    foundLootableItems(scriptTask?.npc?.lootableGroundItems ?: arrayOf())

fun foundLootableItems(items: Array<String>): Boolean = lootableItemsQuery(items).isAny

fun lootableItemsQuery(items: Array<String>): GroundItemQuery = Query.groundItems()
    .nameContains(*items)
    .isReachable
    .maxDistance(2.5)
