package scripts.kt.lumbridge.raider.api

import org.tribot.script.sdk.MakeScreen
import org.tribot.script.sdk.Quest
import org.tribot.script.sdk.Waiting.waitUntil
import org.tribot.script.sdk.Waiting.waitUntilAnimating
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.types.WorldTile
import scripts.waitUntilNotAnimating

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
        val optimalRange: Range
            get() {
                return if (Quest.COOKS_ASSISTANT.state == Quest.State.COMPLETE)
                    LUMBRIDGE_CASTLE_RANGE
                else
                    Range.values().minByOrNull { it.position.distance() }!!
            }

        fun cookRawFood(optimalRange: Range): Boolean = Query.gameObjects()
            .nameContains(optimalRange.obj)
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
                    range.interact(optimalRange.action)
            }
            .orElse(false) &&
                waitUntil { MakeScreen.isOpen() } &&
                waitUntil { MakeScreen.makeAll { it.isVisible && it.actions.isNotEmpty() } } &&
                waitUntil { !MakeScreen.isOpen() } &&
                waitUntilAnimating(3000) &&
                waitUntilNotAnimating(end = 1500)
    }
}