package scripts.kt.lumbridge.raider.api.behaviors.mining

import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.types.WorldTile
import scripts.kotlin.api.canReach
import scripts.kotlin.api.walkTo
import scripts.kt.lumbridge.raider.api.resources.LUMBRIDGE_SWAMP_EAST
import scripts.kt.lumbridge.raider.api.resources.LUMBRIDGE_SWAMP_WEST

private const val TIN_ORE_SPRITE_NAME = "Tin ore"
private const val COPPER_ORE_SPRITE_NAME = "Copper ore"
private const val COAL_ORE_SPRITE_NAME = "Coal"
private const val MITHRIL_ORE_SPRITE_NAME = "Mithril ore"
private const val ADAMANTITE_ORE_SPRITE_NAME = "Adamantite ore"

private const val TIN_ORE_SPRITE_ID = 438
private const val COPPER_ORE_SPRITE_ID = 436
private const val COAL_ORE_SPRITE_ID = 453
private const val MITHRIL_ORE_SPRITE_ID = 447
private const val ADAMANTITE_ORE_SPRITE_ID = 449

private val tinRockColors = shortArrayOf(53)
private val copperRockColors = shortArrayOf(4645)
private val coalRockColors = shortArrayOf(10508)
private val mithrilRockColors = shortArrayOf(-22239)
private val adamantiteRockColors = shortArrayOf(21662)

private val lumbridgeSwampMineEastCoords = intArrayOf(3227, 3147, 0)
private val lumbridgeSwampMineWestCoords = intArrayOf(3147, 3149, 0)

enum class Rock(
    val oreSpriteName: String,
    val oreSpriteId: Int,
    val location: String,
    val colors: ShortArray,
    x: Int, y: Int, z: Int
) {
    TIN_LUMBRIDGE_SWAMP(
        TIN_ORE_SPRITE_NAME,
        TIN_ORE_SPRITE_ID,
        LUMBRIDGE_SWAMP_EAST,
        tinRockColors,
        lumbridgeSwampMineEastCoords[0], lumbridgeSwampMineEastCoords[1], lumbridgeSwampMineEastCoords[2]
    ),
    COPPER_LUMBRIDGE_SWAMP(
        COPPER_ORE_SPRITE_NAME,
        COPPER_ORE_SPRITE_ID,
        LUMBRIDGE_SWAMP_EAST,
        copperRockColors,
        lumbridgeSwampMineEastCoords[0], lumbridgeSwampMineEastCoords[1], lumbridgeSwampMineEastCoords[2]
    ),
    COAL_LUMBRIDGE_SWAMP(
        COAL_ORE_SPRITE_NAME,
        COAL_ORE_SPRITE_ID,
        LUMBRIDGE_SWAMP_WEST,
        coalRockColors,
        lumbridgeSwampMineWestCoords[0], lumbridgeSwampMineWestCoords[1], lumbridgeSwampMineWestCoords[2]
    ),
    MITHRIL_LUMBRIDGE_SWAMP(
        MITHRIL_ORE_SPRITE_NAME,
        MITHRIL_ORE_SPRITE_ID,
        LUMBRIDGE_SWAMP_WEST,
        mithrilRockColors,
        lumbridgeSwampMineWestCoords[0], lumbridgeSwampMineWestCoords[1], lumbridgeSwampMineWestCoords[2]
    ),
    ADAMANTITE_LUMBRIDGE_SWAMP(
        ADAMANTITE_ORE_SPRITE_NAME,
        ADAMANTITE_ORE_SPRITE_ID,
        LUMBRIDGE_SWAMP_WEST,
        adamantiteRockColors,
        lumbridgeSwampMineWestCoords[0], lumbridgeSwampMineWestCoords[1], lumbridgeSwampMineWestCoords[2]
    );

    val position = WorldTile(x, y, z)

    fun getRockGameObjectQuery() = Query.gameObjects()
        .nameEquals("Rocks")
        .filter { it.definition.modifiedColors.any { color -> this@Rock.colors.contains(color) } }

    fun getOreInventoryQuery() = Query.inventory()
        .idEquals(this@Rock.oreSpriteId)

    fun dropOre() = Inventory.drop(getOreInventoryQuery().toList()) > 0

    fun mineOre(): Boolean = getRockGameObjectQuery()
        .findBestInteractable()
        .map {
            if (!canReach(it))
                return@map walkTo(it) &&
                        Waiting.waitUntil { it.interact("Mine") }
            Waiting.waitUntil { it.interact("Mine") }
        }
        .orElse(false)

    override fun toString() = oreSpriteName
}