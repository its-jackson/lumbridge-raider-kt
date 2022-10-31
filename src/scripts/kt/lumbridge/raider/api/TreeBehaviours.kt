package scripts.kt.lumbridge.raider.api

import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.Login
import org.tribot.script.sdk.Options
import org.tribot.script.sdk.Waiting.waitUntil
import org.tribot.script.sdk.antiban.Antiban
import org.tribot.script.sdk.frameworks.behaviortree.*
import org.tribot.script.sdk.frameworks.behaviortree.nodes.SequenceNode
import org.tribot.script.sdk.interfaces.Positionable
import org.tribot.script.sdk.query.GroundItemQuery
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.types.WorldTile
import org.tribot.script.sdk.walking.GlobalWalking
import org.tribot.script.sdk.walking.LocalWalking
import org.tribot.script.sdk.walking.WalkState

/**
Composite nodes: sequence and selector, the sequence node behaves as an AND gate.
The selector node behaves as an OR gate.

Decorative nodes: inverter, repeatUntil, succeeder, condition.
inverter: invert the result of the child. A child fails, and it will return success to its parent,
or a child succeeds, and it will return failure to the parent.

conditional: ensures that we can skip steps we don't need to do. So if the condition is satisfied,
great! We move on. If not, we do something to satisfy it.

Leaf nodes: perform, terminal node that will always return success.
 */

// this behaviour tree ensures the user is logged in first.
// then it will ensure the inventory is empty
// before entering the main script logic.
fun initBehaviorTree(): IBehaviorNode = behaviorTree {
    sequence {
        selector {
            inverter { condition { !Login.isLoggedIn() } }
            repeatUntil({ Login.isLoggedIn() }) { condition { Login.login() } }
        }
        selector {
            inverter { condition { !Inventory.isEmpty() } }
            repeatUntil({ Inventory.isEmpty() }) { walkToAndDepositInvBank() }
        }
    }
}

// this behaviour tree is the main logic tree for the script.
// it decides the behaviour of the character.
fun logicBehaviourTree(scriptTask: ScriptTask?): IBehaviorNode = behaviorTree {
    sequence {
        abstractBehaviour(scriptTask)
        specificBehaviour(scriptTask)
    }
}

/**
 * Carryout the high level abstraction behaviours
 *
 * Logging in, setting the next task, turning on character run, and killing the script
 */
fun IParentNode.abstractBehaviour(scriptTask: ScriptTask?): SequenceNode = sequence("Generic behaviour") {
    // character login
    selector {
        repeatUntil({ Login.isLoggedIn() }) { condition { Login.login() } }
    }
    // character running
    selector {
        condition { !Antiban.shouldTurnOnRun() || Options.isRunEnabled() }
        condition { Options.setRunEnabled(true) }
    }
}

/**
 * Carryout the character specific tasks
 *
 * Melee, Ranged, Magic, Cooking, Firemaking, Woodcutting, Fishing, Mining, Smithing, and Prayer
 */
fun IParentNode.specificBehaviour(scriptTask: ScriptTask?): SequenceNode = sequence("Specific behaviour") {
    selector {
        combatMeleeBehaviour(scriptTask)
        fishingBehavior(scriptTask)
    }
}

fun canReach(p: Positionable): Boolean = LocalWalking.createMap()
    .canReach(p)

fun walkTo(tile: WorldTile) = GlobalWalking.walkTo(tile) {
    if (Antiban.shouldTurnOnRun() && !Options.isRunEnabled()) Options.setRunEnabled(true)
    WalkState.CONTINUE
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

//fun IParentNode.perform(name: String = "", func: () -> Unit): Unit {
//
//    val node = object : IBehaviorNode {
//        override var name: String = ""
//
//        override fun tick(): BehaviorTreeStatus {
//            func()
//            return BehaviorTreeStatus.SUCCESS
//        }
//    }
//
//    this.initNode("[Perform] $name", node) {}
//}