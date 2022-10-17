package scripts.kt.lumbridge.raider.api

import org.tribot.script.sdk.Bank
import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.Login
import org.tribot.script.sdk.Options
import org.tribot.script.sdk.Waiting.waitUntil
import org.tribot.script.sdk.antiban.Antiban
import org.tribot.script.sdk.frameworks.behaviortree.*
import org.tribot.script.sdk.frameworks.behaviortree.nodes.SelectorNode
import org.tribot.script.sdk.frameworks.behaviortree.nodes.SequenceNode
import org.tribot.script.sdk.query.GroundItemQuery
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.types.WorldTile
import org.tribot.script.sdk.walking.GlobalWalking
import org.tribot.script.sdk.walking.LocalWalking
import org.tribot.script.sdk.walking.WalkState

/**
Composite nodes: sequence and selector, the sequence node behaves as an AND gate.
The selector node behaves as an OR gate.

Decorative nodes: inverter, repeatUntil, succeeder, conditional.
inverter: invert the result of the child. A child fails, and it will return success to its parent,
or a child succeeds, and it will return failure to the parent.

conditional: ensures that we can skip steps we don't need to do. So if the condition is satisfied,
great! We move on. If not, we do something to satisfy it.

Leaf nodes: perform, terminal node that will always return success.
(actually perform is a method, that takes a lambda param
that defines the node object ref and returns unit)
 */

// this behaviour tree ensures the user is logged in first.
// then it will ensure the inventory is empty
// before entering the main script logic.
fun initBehaviour(): IBehaviorNode = behaviorTree {
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
fun logicBehaviour(scriptTask: ScriptTask?): IBehaviorNode = behaviorTree {
    sequence {
        selector { abstractBehaviour(scriptTask) }
        selector { specificBehaviour(scriptTask) }
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
    // character banking
    bankingBehaviour(scriptTask)
}

fun IParentNode.specificBehaviour(scriptTask: ScriptTask?): SequenceNode = sequence("Specific behaviour") {
    selector {
        combatMeleeBehaviour(scriptTask)
    }
}

/**
 * Carryout the character specific tasks
 *
 * Melee, Ranged, Magic, Cooking, Firemaking, Woodcutting, Fishing, Mining, Smithing, and Prayer
 */
fun IParentNode.combatMeleeBehaviour(scriptTask: ScriptTask?): SequenceNode = sequence("Combat behaviour") {
    // ensure sequence is melee combat
    condition { scriptTask?.behaviour == Behaviour.COMBAT_MELEE }
    // character cooking
    selector {
        inverter { condition { isCookRawFood(scriptTask) } }
        repeatUntil({ !isCookRawFood(scriptTask) }) {
            walkToAndCookRange()
        }
    }
    // character combat
    selector {
        walkToAndAttackNpc(scriptTask)
    }
    // character looting
    selector {
        condition { !foundLootableItems(scriptTask) }
        condition { Inventory.isFull() }
        condition { lootItems(scriptTask) > 0 }
    }
}

/**
 * Carryout the characters banking
 */
fun IParentNode.bankingBehaviour(scriptTask: ScriptTask?): SelectorNode = selector {
    // character banking
    selector {
        inverter {
            condition { isBankNeeded(scriptTask) }
        }
        walkToAndDepositInvBank()
    }
}

/**
 * This function can be called on an [IParentNode], which means it can be called in lambdas for nodes
 * such as "sequence" and "selector".
 */
fun IParentNode.walkToAndOpenBank(): SequenceNode = sequence {
    selector {
        condition { Bank.ensureOpen() }
        sequence {
            selector {
                condition { Bank.isNearby() } // at the bank? good, we are done now.
                condition { GlobalWalking.walkToBank() } // we aren't at the bank, let's walk to it.
            }
            condition { Bank.ensureOpen() }
        }
    }
}

fun IParentNode.walkToAndDepositInvBank(): SequenceNode = sequence {
    walkToAndOpenBank()
    condition { Bank.depositInventory() && Bank.close() }
}

fun IParentNode.walkToAndCookRange(): SequenceNode = sequence {
    selector {
        condition { isAtTile(Range.optimalRange.position) }
        condition { walkTo(Range.optimalRange.position) }
    }
    condition { Range.cookRawFood(Range.optimalRange) }
}

fun IParentNode.walkToAndAttackNpc(scriptTask: ScriptTask?): SequenceNode = sequence {
    selector {
        condition { (scriptTask != null) && !Npc.isCombat(scriptTask.npc) }
    }
    selector {
        condition { (scriptTask != null) && isAtTile(scriptTask.npc.position) }
        condition { (scriptTask != null) && walkTo(scriptTask.npc.position) }
    }
    condition { (scriptTask != null) && Npc.attack(scriptTask.npc) }
}

fun isBankNeeded(scriptTask: ScriptTask?): Boolean = scriptTask?.bankDisposal == true
        && Inventory.isFull()
        ||
        (scriptTask?.cookThenBankDisposal == true
                &&
                (!Inventory.contains("Raw chicken", "Raw beef") && Inventory.isFull()))


fun isCookRawFood(scriptTask: ScriptTask?): Boolean = scriptTask?.cookThenBankDisposal == true && (
        Inventory.isFull() &&
                Inventory.getAll()
                    .filter { it.name.contains("Raw") }
                    .filterNot { it.definition.isNoted }
                    .any()
        )

fun isAtTile(tile: WorldTile): Boolean = LocalWalking.createMap().canReach(tile)

fun walkTo(tile: WorldTile): Boolean = GlobalWalking.walkTo(tile) {
    if (Antiban.shouldTurnOnRun() && !Options.isRunEnabled()) Options.setRunEnabled(true)
    if (LocalWalking.createMap().canReach(tile)) WalkState.SUCCESS else WalkState.CONTINUE
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