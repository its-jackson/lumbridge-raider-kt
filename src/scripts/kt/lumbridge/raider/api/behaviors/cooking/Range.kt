package scripts.kt.lumbridge.raider.api.behaviors.cooking

import org.tribot.script.sdk.MakeScreen
import org.tribot.script.sdk.Quest
import org.tribot.script.sdk.Waiting.waitUntil
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.types.WorldTile
import scripts.kotlin.api.makeAllAvailableItems

private val cookableWhiteList = arrayOf("Cooked", "Raw")

enum class Range(
    x: Int, y: Int, z: Int,
    val action: String,
    val obj: String
) {
    LUMBRIDGE_CASTLE_RANGE(
        3211, 3215, 0,
        "Cook",
        "Cooking range"
    ),
    LUMBRIDGE_COOKING_TUTOR_RANGE(
        3231, 3197, 0,
        "Cook",
        "Range"
    ),
    LUMBRIDGE_FARMER_POT_EAST(
        3226, 3290, 0,
        "",
        "Cooking pot"
    ),
    LUMBRIDGE_FARMER_POT_WEST(
        3186, 3274, 0,
        "",
        "Cooking pot"
    );

    val position = WorldTile(x, y, z)

    companion object {
        val best: Range
            get() {
                return if (Quest.COOKS_ASSISTANT.state == Quest.State.COMPLETE)
                    LUMBRIDGE_CASTLE_RANGE
                else
                    Range.values().minByOrNull { it.position.distance() }!!
            }

        fun cookRawFood(bestRange: Range): Boolean {
            return if (MakeScreen.isOpen()) {
                makeAllAvailableItems(cookableWhiteList)
            }
            else {
                Query.gameObjects()
                    .nameContains(bestRange.obj)
                    .isReachable
                    .findBestInteractable()
                    .map { range ->
                        if (range.actions.isEmpty())
                            Query.inventory()
                                .nameContains("Raw")
                                .findClosestToMouse()
                                .map { waitUntil { it.useOn(range) } }
                                .orElse(false)
                        else
                            range.interact(bestRange.action)
                    }
                    .orElse(false) &&
                        waitUntil { MakeScreen.isOpen() } && makeAllAvailableItems(cookableWhiteList)
            }
        }
    }
}