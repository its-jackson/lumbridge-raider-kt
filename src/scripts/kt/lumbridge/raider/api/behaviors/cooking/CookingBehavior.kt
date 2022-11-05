package scripts.kt.lumbridge.raider.api.behaviors.cooking

import org.tribot.script.sdk.Bank
import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.frameworks.behaviortree.*
import org.tribot.script.sdk.frameworks.behaviortree.nodes.SequenceNode
import org.tribot.script.sdk.query.Query
import scripts.kt.lumbridge.raider.api.*
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
        cookingAction(scriptTask)
    }
}

fun IParentNode.cookingAction(scriptTask: ScriptTask?): SequenceNode = sequence {
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

fun inventoryHasCookingItems(scriptTask: ScriptTask?): Boolean {
    if (scriptTask?.stop !is ResourceGainedCondition) return false
    scriptTask.stop as ResourceGainedCondition
    return scriptTask.stop.id.let { Inventory.contains(it) }
}

fun bankHasCookingItems(scriptTask: ScriptTask?): Boolean {
    if (scriptTask?.stop !is ResourceGainedCondition) return false
    scriptTask.stop as ResourceGainedCondition
    return scriptTask.stop.id.let { Bank.contains(it) }
}

fun withdrawCookingItems(scriptTask: ScriptTask?): Boolean {
    if (scriptTask?.stop !is ResourceGainedCondition) return false
    scriptTask.stop as ResourceGainedCondition
    return if (scriptTask.stop.remainder in 1..28) {
        scriptTask.stop.id.let {
            Bank.withdraw(
                it,
                scriptTask.stop.remainder
            ) && Waiting.waitUntil { Inventory.contains(it) }
        }
    } else {
        scriptTask.stop.id.let { Bank.withdrawAll(it) && Waiting.waitUntil { Inventory.contains(it) } }
    }
}

fun updateCookingItemSum(scriptTask: ScriptTask?, amountBefore: Int) {
    if (scriptTask?.stop !is ResourceGainedCondition) return
    scriptTask.stop as ResourceGainedCondition
    val amountAfter = getSumStacks()
    val updateAmount = amountAfter - amountBefore
    scriptTask.stop.updateSum(updateAmount)
}

private fun getSumStacks() = Query.inventory()
    .sumStacks()