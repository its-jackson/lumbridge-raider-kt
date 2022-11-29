package scripts.kt.lumbridge.raider.api.behaviors.prayer

import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.antiban.AntibanProperties
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.util.TribotRandom
import scripts.kotlin.api.getAsInventory
import scripts.kotlin.api.waitUntilNotAnimating

enum class Bone(
    val spriteName: String,
    val spriteId: Int,
) {
    NORMAL("Bones", 526),
    BIG("Big bones", 532);

    val action = "Bury"

    fun getBonesInventoryQuery() = Query.inventory()
        .idEquals(this@Bone.spriteId)

    fun buryAll(
        dropPattern: Inventory.DropPattern = AntibanProperties.getPropsForCurrentChar()
            .determineDropPattern()
    ): Int {
        // ty naton/nullable
        val inventoryItems = getAsInventory(getBonesInventoryQuery().toList())

        var buryCount = 0

        for (index in dropPattern.dropList) {
            val toBury = inventoryItems[index]

            val result = toBury?.click(this@Bone.action) == true &&
                    Waiting.waitUntilAnimating(3000) &&
                    waitUntilNotAnimating(end = TribotRandom.normal(200, 50).toLong())

            if (result) buryCount++
        }

        return buryCount
    }

    override fun toString() = this@Bone.spriteName
}