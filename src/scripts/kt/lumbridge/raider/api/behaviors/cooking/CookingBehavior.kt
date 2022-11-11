package scripts.kt.lumbridge.raider.api.behaviors.cooking

import org.tribot.script.sdk.Bank
import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.frameworks.behaviortree.*
import org.tribot.script.sdk.frameworks.behaviortree.nodes.SequenceNode
import org.tribot.script.sdk.query.Query
import scripts.kt.lumbridge.raider.api.Behavior
import scripts.kt.lumbridge.raider.api.ScriptTask
import scripts.kt.lumbridge.raider.api.behaviors.banking.walkToAndDepositInvBank
import scripts.kt.lumbridge.raider.api.behaviors.canReach
import scripts.kt.lumbridge.raider.api.behaviors.walkTo

fun IParentNode.cookingBehavior(scriptTask: ScriptTask?): SequenceNode = sequence {
    condition { scriptTask?.behavior == Behavior.COOKING }

    sequence {
        selector {
            condition { inventoryHasCookingItems(scriptTask) }
            sequence {
                walkToAndDepositInvBank(closeBank = false)
                condition { bankHasCookingItems(scriptTask) }
                condition { withdrawCookingItems(scriptTask) }
                condition { Bank.close() }
            }
        }

        // ensure the item is "Raw" and therefore "Cookable"
        condition { isCookRawFood() }

        // attempt to cook at the nearby and best range,
        // and update the stop condition for the script task.
        completeCookingAction(scriptTask)
    }
}

fun IParentNode.completeCookingAction(scriptTask: ScriptTask?): SequenceNode = sequence {
    val amountBefore = getSumStacks()
    walkToAndCookRange()
    perform { updateCookingItemSum(scriptTask, amountBefore) }
}

fun IParentNode.walkToAndCookRange(): SequenceNode = sequence {
    selector {
        condition { canReach(Range.optimalRange.position) }
        condition { walkTo(Range.optimalRange.position) }
    }
    condition { Range.cookRawFood(Range.optimalRange) }
}

fun isCookRawFood(): Boolean = Inventory.getAll()
    .filter { it.name.contains("Raw") }
    .filterNot { it.definition.isNoted }
    .any()

private fun inventoryHasCookingItems(scriptTask: ScriptTask?): Boolean = scriptTask?.resourceGainedCondition
    ?.let {
        it.id.let { item -> Inventory.contains(item) }
    } ?: false

private fun bankHasCookingItems(scriptTask: ScriptTask?): Boolean = scriptTask?.resourceGainedCondition
    ?.let {
        it.id.let { item -> Bank.contains(item) }
    } ?: false

private fun withdrawCookingItems(scriptTask: ScriptTask?): Boolean = scriptTask?.resourceGainedCondition
    ?.let {
        if (it.remainder in 1..28) {
            it.id.let { item ->
                Bank.withdraw(
                    item,
                    it.remainder
                ) && Waiting.waitUntil { Inventory.contains(item) }
            }
        } else {
            it.id.let { item ->
                Bank.withdrawAll(item) && Waiting.waitUntil { Inventory.contains(item) }
            }
        }
    } ?: false

private fun updateCookingItemSum(scriptTask: ScriptTask?, amountBefore: Int) = scriptTask?.resourceGainedCondition
    ?.let {
        val amountAfter = getSumStacks()
        val updateAmount = amountAfter - amountBefore
        it.updateSum(updateAmount)
    }

private fun getSumStacks() = Query.inventory()
    .sumStacks()