package scripts.kt.lumbridge.raider.api.behaviors.cooking

import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.antiban.PlayerPreferences
import org.tribot.script.sdk.frameworks.behaviortree.*
import scripts.kotlin.api.canReach
import scripts.kotlin.api.fetchResourceFromBank
import scripts.kotlin.api.walkTo
import scripts.kt.lumbridge.raider.api.ScriptTask

private val cookingWaitMean: Int =
    PlayerPreferences.preference(
        "scripts.kt.lumbridge.raider.api.behaviors.cooking.CookingBehavior.cookingWaitMean"
    ) { g: PlayerPreferences.Generator ->
        g.uniform(250, 600)
    }

private val cookingWaitStd: Int =
    PlayerPreferences.preference(
        "scripts.kt.lumbridge.raider.api.behaviors.cooking.CookingBehavior.cookingWaitStd"
    ) { g: PlayerPreferences.Generator ->
        g.uniform(5, 15)
    }

fun IParentNode.cookingBehavior(scriptTask: ScriptTask?) = sequence {
    fetchResourceFromBank(scriptTask?.resourceGainedCondition)
    condition { isCookRawFood() }
    walkToAndCookRange(scriptTask)
    perform { Waiting.waitNormal(cookingWaitMean, cookingWaitStd) }
}

fun IParentNode.walkToAndCookRange(scriptTask: ScriptTask?) = sequence {
    val best = Range.LUMBRIDGE_COOKING_TUTOR_RANGE
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
    .filter { it.name.contains("Raw") }
    .filterNot { it.definition.isNoted }
    .any()
