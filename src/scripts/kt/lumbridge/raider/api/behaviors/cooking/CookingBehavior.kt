package scripts.kt.lumbridge.raider.api.behaviors.cooking

import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.frameworks.behaviortree.*
import scripts.kotlin.api.canReach
import scripts.kotlin.api.fetchResourceFromBank
import scripts.kotlin.api.walkTo
import scripts.kt.lumbridge.raider.api.ScriptTask

fun IParentNode.cookingBehavior(scriptTask: ScriptTask?) = sequence {
    fetchResourceFromBank(scriptTask?.resourceGainedCondition)
    condition { isCookRawFood() }
    walkToAndCookRange(scriptTask)
}

fun IParentNode.walkToAndCookRange(scriptTask: ScriptTask?) = sequence {
    val best = Range.best
    val position = best.position

    selector {
        condition { position.distance() < 5 && position.isVisible && canReach(position) }
        condition { walkTo(position) }
    }

    condition {
        val amountBefore = scriptTask?.resourceGainedCondition
            ?.let {
                Inventory.getCount(it.id)
            } ?: 0

        Range.cookRawFood(best)

        val amountAfter = scriptTask?.resourceGainedCondition
            ?.let {
                Inventory.getCount(it.id)
            } ?: 0

        val cookedAmount = amountBefore - amountAfter
        scriptTask?.resourceGainedCondition?.updateSumDirectly(cookedAmount)
        cookedAmount > 0
    }
}

fun isCookRawFood() = Inventory.getAll()
    .any { it.name.contains("Raw") && !it.definition.isNoted }
