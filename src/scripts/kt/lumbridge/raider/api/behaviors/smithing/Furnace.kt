package scripts.kt.lumbridge.raider.api.behaviors.smithing

import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.types.WorldTile
import scripts.kotlin.api.canReach
import scripts.kotlin.api.walkTo
import scripts.kt.lumbridge.raider.api.resources.LUMBRIDGE_CASTLE

enum class Furnace(
    val ids: IntArray,
    val location: String,
    x: Int, y: Int, z: Int
) {
    FURNACE_LUMBRIDGE_CASTLE(
        intArrayOf(24009),
        LUMBRIDGE_CASTLE,
        3227, 3255, 0
    );

    val position = WorldTile(x, y, z)
    val gameObjectName = "Furnace"
    val gameObjectAction = "Smelt"

    fun getFurnaceGameObjectQuery() = Query.gameObjects()
        .idEquals(*this@Furnace.ids)

    fun interact() = getFurnaceGameObjectQuery()
        .findBestInteractable()
        .map { furnace ->
            if (!canReach(furnace))
                return@map walkTo(furnace) &&
                        Waiting.waitUntil { furnace.interact(this@Furnace.gameObjectAction) }
            furnace.interact(this@Furnace.gameObjectAction)
        }
}