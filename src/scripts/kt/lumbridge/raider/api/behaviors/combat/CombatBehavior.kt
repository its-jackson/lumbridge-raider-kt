package scripts.kt.lumbridge.raider.api.behaviors.combat

import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.frameworks.behaviortree.*
import org.tribot.script.sdk.frameworks.behaviortree.nodes.SelectorNode
import org.tribot.script.sdk.frameworks.behaviortree.nodes.SequenceNode
import scripts.kotlin.api.canReach
import scripts.kotlin.api.walkTo
import scripts.kt.lumbridge.raider.api.Behavior
import scripts.kt.lumbridge.raider.api.Disposal
import scripts.kt.lumbridge.raider.api.ScriptTask
import scripts.kt.lumbridge.raider.api.behaviors.banking.walkToAndDepositInvBank
import scripts.kt.lumbridge.raider.api.behaviors.cooking.isCookRawFood
import scripts.kt.lumbridge.raider.api.behaviors.cooking.walkToAndCookRange
import scripts.kt.lumbridge.raider.api.behaviors.foundLootableItems
import scripts.kt.lumbridge.raider.api.behaviors.lootItems

fun IParentNode.combatMeleeBehavior(scriptTask: ScriptTask?): SequenceNode = sequence("Combat behavior") {
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

fun isCombatBankingNeeded(scriptTask: ScriptTask?): Boolean = scriptTask?.disposal == Disposal.BANK
        && Inventory.isFull()
        ||
        (scriptTask?.disposal == Disposal.COOK_THEN_DROP
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