package scripts.kt.lumbridge.raider.api

import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.frameworks.behaviortree.*
import org.tribot.script.sdk.frameworks.behaviortree.nodes.SelectorNode
import org.tribot.script.sdk.frameworks.behaviortree.nodes.SequenceNode

fun IParentNode.combatMeleeBehaviour(scriptTask: ScriptTask?): SequenceNode = sequence("Combat behavior") {
    // ensure sequence is melee combat
    condition { scriptTask?.behavior == Behavior.COMBAT_MELEE }

    // character combat banking
    combatBankingBehaviour(scriptTask)

    // character cooking
    // TODO FIX
    selector {
        inverter { condition { isCookRawFood() } }
        repeatUntil({ !isCookRawFood() }) {
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
fun IParentNode.combatBankingBehaviour(scriptTask: ScriptTask?): SelectorNode = selector {
    selector {
        inverter {
            condition { isCombatBankingNeeded(scriptTask) }
        }
        walkToAndDepositInvBank()
    }
}

fun isCombatBankingNeeded(scriptTask: ScriptTask?): Boolean = scriptTask?.bankDisposal == true
        && Inventory.isFull()
        ||
        (scriptTask?.cookThenBankDisposal == true
                &&
                (!Inventory.contains("Raw chicken", "Raw beef") && Inventory.isFull()))

fun IParentNode.walkToAndAttackNpc(scriptTask: ScriptTask?): SequenceNode = sequence {
    selector {
        condition { (scriptTask != null) && scriptTask.npc?.let { Npc.isCombat(it) } == false }
    }
    selector {
        condition { (scriptTask != null) && scriptTask.npc?.let { canReach(it.position) } == true }
        condition { (scriptTask != null) && scriptTask.npc?.let { walkTo(it.position) } == true }
    }
    condition { (scriptTask != null) && scriptTask.npc?.let { Npc.attack(it) } == true }
}