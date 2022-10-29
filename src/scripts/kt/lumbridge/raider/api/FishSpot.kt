package scripts.kt.lumbridge.raider.api

import org.tribot.script.sdk.types.WorldTile
import scripts.data.ItemID
import scripts.data.NpcID.*

private const val LUMBRIDGE_SWAMP = "Lumbridge Swamp"
private const val LUMBRIDGE_CASTLE = "Lumbridge Castle"
private const val USE_ACTION = "Use"

private val lumbridgeSwampFishingCoords = intArrayOf(3238, 3158, 0)
private val lumbridgeCastleFishingCoords = intArrayOf(3241, 3250, 0)

private val smallFishingNetEquipmentReq = mapOf(ItemID.SMALL_FISHING_NET to 1)

private val fishingRodEquipmentReq = mapOf(ItemID.FISHING_ROD to 1)
private val fishingRodBaitReq = mapOf(ItemID.FISHING_BAIT to 1)
private val fishingRodFishSpotActions = arrayOf("Bait")

private val flyFishingRodEquipmentReq = mapOf(ItemID.FLY_FISHING_ROD to 1)
private val flyFishingRodBaitReq = mapOf(ItemID.FEATHER to 1)
private val flyFishingRodFishSpotActions = arrayOf("Lure")

//shrimps/anchovies
private val shrimpsAnchoviesFishSpotIds = intArrayOf(
    FISHING_SPOT_1514, FISHING_SPOT_1517, FISHING_SPOT_1518,
    FISHING_SPOT_1521, FISHING_SPOT_1523, FISHING_SPOT_1524,
    FISHING_SPOT_1525, FISHING_SPOT_1528, FISHING_SPOT_1530,
    FISHING_SPOT_1544, FISHING_SPOT_3913, FISHING_SPOT_7155,
    FISHING_SPOT_7459, FISHING_SPOT_7462, FISHING_SPOT_7467,
    FISHING_SPOT_7469, FISHING_SPOT_7947, FISHING_SPOT_10513
)
private val shrimpAnchoviesFishSpotActions = arrayOf(
    "Small Net", "Net"
)
private val shrimpAnchoviesFishSpriteIds = intArrayOf(
    ItemID.RAW_SHRIMPS, ItemID.RAW_ANCHOVIES
)
private val shrimpAnchoviesFishSpriteNames = arrayOf(
    "Raw shrimps", "Raw anchovies"
)
//

//sardine/herring
private val sardineHerringFishSpotIds = intArrayOf(
    FISHING_SPOT_1514, FISHING_SPOT_1517, FISHING_SPOT_1518,
    FISHING_SPOT_1521, FISHING_SPOT_1523, FISHING_SPOT_1524,
    FISHING_SPOT_1525, FISHING_SPOT_1528, FISHING_SPOT_1530,
    FISHING_SPOT_1544, FISHING_SPOT_3913, FISHING_SPOT_7155,
    FISHING_SPOT_7459, FISHING_SPOT_7462, FISHING_SPOT_7467,
    FISHING_SPOT_7469, FISHING_SPOT_7947, FISHING_SPOT_10513
)
private val sardineHerringFishSpriteIds = intArrayOf(
    ItemID.RAW_SARDINE, ItemID.RAW_HERRING
)
private val sardineHerringFishSpriteNames = arrayOf(
    "Raw sardine", "Raw herring"
)
//

//pike
private val pikeSalmonTroutFishSpotIds = intArrayOf(
    ROD_FISHING_SPOT, ROD_FISHING_SPOT_1508, ROD_FISHING_SPOT_1509,
    ROD_FISHING_SPOT_1513, ROD_FISHING_SPOT_1515, ROD_FISHING_SPOT_1516,
    ROD_FISHING_SPOT_1526, ROD_FISHING_SPOT_1527, ROD_FISHING_SPOT_7463,
    ROD_FISHING_SPOT_7464, ROD_FISHING_SPOT_7468, ROD_FISHING_SPOT_8524
)
private val pikeFishSpriteIds = intArrayOf(ItemID.RAW_PIKE)
private val pikeFishSpriteNames = arrayOf("Raw pike")
//

//salmon/trout
private val salmonTroutSpriteIds = intArrayOf(
    ItemID.RAW_SALMON, ItemID.RAW_TROUT
)
private val salmonTroutFishSpriteNames = arrayOf(
    "Raw salmon", "Raw trout"
)
//

enum class FishSpot(
    val ids: IntArray,
    val actions: Array<String>,
    val spriteIds: IntArray,
    val spriteNames: Array<String>,
    val spritePrimaryAction: String,
    val equipmentReq: Map<Int, Int>, // key: equipment id, value: the exact amount needed
    val baitReq: Map<Int, Int>, // key: bait id, value: minimum bait amount required
    x: Int, y: Int, z: Int
) {

    SHRIMPS_ANCHOVIES_LUMBRIDGE_SWAMP(
        shrimpsAnchoviesFishSpotIds,
        shrimpAnchoviesFishSpotActions,
        shrimpAnchoviesFishSpriteIds,
        shrimpAnchoviesFishSpriteNames,
        USE_ACTION,
        smallFishingNetEquipmentReq,
        mapOf(), // no bait is required for netting shrimps/anchovies
        lumbridgeSwampFishingCoords[0], lumbridgeSwampFishingCoords[1], lumbridgeSwampFishingCoords[2],
    ) {
        override fun location(): String = LUMBRIDGE_SWAMP
    },
    SARDINE_HERRING_LUMBRIDGE_SWAMP(
        sardineHerringFishSpotIds,
        fishingRodFishSpotActions,
        sardineHerringFishSpriteIds,
        sardineHerringFishSpriteNames,
        USE_ACTION,
        fishingRodEquipmentReq,
        fishingRodBaitReq,
        lumbridgeSwampFishingCoords[0], lumbridgeSwampFishingCoords[1], lumbridgeSwampFishingCoords[2],
    ) {
        override fun location(): String = LUMBRIDGE_SWAMP
    },
    PIKE_LUMBRIDGE_CASTLE(
        pikeSalmonTroutFishSpotIds,
        fishingRodFishSpotActions,
        pikeFishSpriteIds,
        pikeFishSpriteNames,
        USE_ACTION,
        fishingRodEquipmentReq,
        fishingRodBaitReq,
        lumbridgeCastleFishingCoords[0], lumbridgeCastleFishingCoords[1], lumbridgeCastleFishingCoords[2]
    ) {
        override fun location(): String = LUMBRIDGE_CASTLE
    },
    SALMON_TROUT_LUMBRIDGE_CASTLE(
        pikeSalmonTroutFishSpotIds,
        flyFishingRodFishSpotActions,
        salmonTroutSpriteIds,
        salmonTroutFishSpriteNames,
        USE_ACTION,
        flyFishingRodEquipmentReq,
        flyFishingRodBaitReq,
        lumbridgeCastleFishingCoords[0], lumbridgeCastleFishingCoords[1], lumbridgeCastleFishingCoords[2]
    ) {
        override fun location(): String = LUMBRIDGE_CASTLE
    }
    ;

    val position = WorldTile(x, y, z)

    open fun location() = ""

    override fun toString(): String = "${spriteNames.fold("") { acc, s -> "$acc/$s" }} at ${location()}"
}