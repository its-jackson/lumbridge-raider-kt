package scripts.kt.lumbridge.raider.api.behaviors.prayer

import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.antiban.AntibanProperties
import org.tribot.script.sdk.antiban.PlayerPreferences
import org.tribot.script.sdk.antiban.PlayerPreferences.Generator
import org.tribot.script.sdk.frameworks.behaviortree.IParentNode
import org.tribot.script.sdk.frameworks.behaviortree.condition
import org.tribot.script.sdk.frameworks.behaviortree.sequence
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.util.TribotRandom
import scripts.kotlin.api.getAsInventory
import scripts.kotlin.api.fetchResourceFromBank
import scripts.kotlin.api.waitUntilNotAnimating
import scripts.kt.lumbridge.raider.api.ScriptTask

fun IParentNode.prayerBehavior(scriptTask: ScriptTask?) = sequence {
    fetchResourceFromBank(scriptTask?.resourceGainedCondition)
    condition {
        val buryCount = buryAll(scriptTask)
        scriptTask?.resourceGainedCondition?.updateSumDirectly(buryCount)
        buryCount > 0
    }
}

private fun getBonesInventoryQuery(scriptTask: ScriptTask?) = scriptTask?.resourceGainedCondition?.id
    ?.let {
        Query.inventory()
            .idEquals(it)
    }

/**
 * Uses the TRiBot drop pattern for burying and,
 * the method for getting the nullable items based on index.
 * Yoink.
 */
private fun buryAll(
    scriptTask: ScriptTask?,
    buryPattern: Inventory.DropPattern? = scriptTask?.prayerData?.buryPattern,
    buryMean: Int = PlayerPreferences.preference("buryMean") { g: Generator -> g.uniform(150, 200) },
    buryStd: Int = PlayerPreferences.preference("buryStd") { g: Generator -> g.uniform(15, 30) },
): Int {
    val inventoryItems = getBonesInventoryQuery(scriptTask)
        ?.toList()
        ?.let { getAsInventory(it) } ?: return 0

    var actualBuryPattern = AntibanProperties.getPropsForCurrentChar()
        .determineDropPattern()

    var buryCount = 0

    if (buryPattern != null) actualBuryPattern = buryPattern

    for (index in actualBuryPattern.dropList) {
        val toBury = inventoryItems[index]

        val result = toBury?.click("Bury") == true &&
                Waiting.waitUntilAnimating(TribotRandom.normal(2400,86)) &&
                waitUntilNotAnimating(end = TribotRandom.normal(buryMean, buryStd).toLong())

        if (result) buryCount++
    }

    return buryCount
}
