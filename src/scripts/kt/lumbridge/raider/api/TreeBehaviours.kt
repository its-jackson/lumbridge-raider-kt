package scripts.kt.lumbridge.raider.api

import org.tribot.script.sdk.*
import org.tribot.script.sdk.Waiting.waitUntil
import org.tribot.script.sdk.Waiting.waitUntilAnimating
import org.tribot.script.sdk.antiban.Antiban
import org.tribot.script.sdk.frameworks.behaviortree.*
import org.tribot.script.sdk.frameworks.behaviortree.nodes.SequenceNode
import org.tribot.script.sdk.query.GroundItemQuery
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.types.WorldTile
import org.tribot.script.sdk.walking.GlobalWalking
import org.tribot.script.sdk.walking.LocalWalking
import org.tribot.script.sdk.walking.WalkState
import scripts.waitUntilNotAnimating

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

/**
 * Carryout the character generic tasks
 *
 * Logging in, setting the next task, turning on character run, and killing the script
 */
fun IParentNode.genericBehaviour(): SequenceNode = sequence("Generic behaviour") {
    // terminate
    selector {
        condition { !ScriptTaskRunner.isRunnerComplete() }
        sequence { BehaviorTreeStatus.KILL }
    }

    // next task
    selector {
        inverter {
            condition { ScriptTaskRunner.isSatisfied() }
        }
        sequence {
            walkToAndDepositInvBank()
            perform { ScriptTaskRunner.setNext() }
        }
    }

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

fun IParentNode.specificBehaviour(): SequenceNode = sequence("Specific behaviour") {
    selector {
        combatMeleeBehaviour()
    }
}

/**
 * Carryout the character specific tasks
 *
 * Melee, Ranged, Magic, Cooking, Firemaking, Woodcutting, Fishing, Mining, Smithing, and Prayer
 */
fun IParentNode.combatMeleeBehaviour(): SequenceNode = sequence("Combat behaviour") {
    // ensure sequence is melee combat
    condition { ScriptTaskRunner.activeTask?.behaviour == Behaviour.COMBAT_MELEE }

    // character cooking
    selector {
        inverter { condition { isCookRawFood(ScriptTaskRunner.activeTask) } }
        repeatUntil({ !isCookRawFood(ScriptTaskRunner.activeTask) }) {
            walkToAndCookRange()
        }
    }

    // character banking
    selector {
        inverter {
            condition { isBankNeeded(ScriptTaskRunner.activeTask) }
        }
        walkToAndDepositInvBank()
    }

    // character combat
    selector {
        condition { isCombat(ScriptTaskRunner.activeTask) }
        walkToAndAttackNpc(ScriptTaskRunner.activeTask)
    }

    // character looting
    selector {
        repeatUntil({ !foundLootableItems(ScriptTaskRunner.activeTask) || Inventory.isFull() }) {
            sequence {
                condition { lootItems(ScriptTaskRunner.activeTask) }
            }
        }
    }
}

/**
 * This function can be called on an [IParentNode], which means it can be called in lambdas for nodes
 * such as "sequence" and "selector".
 */
fun IParentNode.walkToAndOpenBank(): SequenceNode {
    return sequence {
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
}

fun IParentNode.walkToAndDepositInvBank(): SequenceNode {
    return sequence {
        walkToAndOpenBank()
        condition { Bank.depositInventory() && Bank.close() }
    }
}

fun IParentNode.walkToAndCookRange(): SequenceNode {
    return sequence {
        selector {
            condition { isAtTile(Range.optimalRange.position) }
            condition { walkTo(Range.optimalRange.position) }
        }
        condition { Range.cookRawFood(Range.optimalRange) }
    }
}

fun IParentNode.walkToAndAttackNpc(scriptTask: ScriptTask?): SequenceNode {
    return walkToAndAttackNpc(
        scriptTask?.npc?.names ?: arrayOf(),
        scriptTask?.npc?.position ?: WorldTile(0, 0, 0)
    )
}

fun IParentNode.walkToAndAttackNpc(npcs: Array<String>, tile: WorldTile): SequenceNode {
    return sequence {
        selector {
            condition { isAtTile(tile) }
            condition { walkTo(tile) }
        }
        condition { attackNpc(npcs) }
    }
}

fun isBankNeeded(scriptTask: ScriptTask?): Boolean {
    return scriptTask?.bankDisposal == true && Inventory.isFull()
            ||
            (scriptTask?.cookThenBankDisposal == true
                    &&
                    (!Inventory.contains("Raw chicken", "Raw beef") && Inventory.isFull()))
}

fun isCookRawFood(scriptTask: ScriptTask?): Boolean {
    return scriptTask?.cookThenBankDisposal == true && (
            Inventory.isFull() &&
                    Inventory.getAll()
                        .filter { it.name.contains("Raw") }
                        .filterNot { it.definition.isNoted }
                        .any()
            )
}

fun isCombat(npcs: Array<String>): Boolean {
    return Query.npcs()
        .nameContains(*npcs)
        .isFacingMe
        .isInteractingWithMe
        .minHealthBarPercent(1.0)
        .isAny && MyPlayer.get().map { it.hitsplats }
        .map { it.size > 0 }
        .orElse(false)
}

fun isCombat(scriptTask: ScriptTask?): Boolean {
    return isCombat(
        scriptTask?.npc?.names ?: arrayOf()
    )
}

fun isCombat(npc: String): Boolean {
    return isCombat(arrayOf(npc))
}

fun isAtTile(tile: WorldTile): Boolean {
    return LocalWalking.createMap().canReach(tile)
}

fun walkTo(tile: WorldTile): Boolean {
    return GlobalWalking.walkTo(tile) {
        if (Antiban.shouldTurnOnRun() && !Options.isRunEnabled()) Options.setRunEnabled(true)
        if (LocalWalking.createMap().canReach(tile)) WalkState.SUCCESS else WalkState.CONTINUE
    }
}

fun attackNpc(npcs: Array<String>): Boolean {
    return Query.npcs()
        .nameContains(*npcs)
        .isReachable
        .isNotBeingInteractedWith
        .findBestInteractable()
        .map {
            it.interact("Attack") && waitUntilAnimating(6500) &&
                    waitUntil { it.isInteractingWithMe } &&
                    waitUntilNotAnimating(2500, 100) &&
                    waitUntil(2500) { !it.isValid }
        }
        .orElse(false)
}

fun attackNpc(npc: String): Boolean {
    return attackNpc(arrayOf(npc))
}

fun lootItems(scriptTask: ScriptTask?): Boolean {
    return lootItems(
        scriptTask?.npc?.lootableGroundItems ?: arrayOf()
    )
}

fun lootItems(items: Array<String>): Boolean {
    return lootableItemsQuery(items)
        .toList()
        .all {
            val before = Inventory.getAll()
                .fold(0) { runningSum, item -> runningSum + item.stack }
            return it.interact("Take") && waitUntil {
                Inventory.getAll()
                    .fold(0) { runningSum, item -> runningSum + item.stack } > before
            }
        }
}

fun foundLootableItems(scriptTask: ScriptTask?): Boolean {
    return foundLootableItems(
        scriptTask?.npc?.lootableGroundItems ?: arrayOf()
    )
}

fun foundLootableItems(items: Array<String>): Boolean {
    return lootableItemsQuery(items).isAny
}

fun lootableItemsQuery(items: Array<String>): GroundItemQuery {
    return Query.groundItems()
        .nameContains(*items)
        .isReachable
        .maxDistance(2.5)
}

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