package scripts.kt.lumbridge.raider.api.behaviors.fishing

import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.types.WorldTile
import org.tribot.script.sdk.util.TribotRandom
import scripts.data.ItemID
import scripts.kotlin.api.canReach
import scripts.kotlin.api.waitUntilNotAnimating
import scripts.kotlin.api.walkTo
import scripts.kt.lumbridge.raider.api.resources.LUMBRIDGE_CASTLE
import scripts.kt.lumbridge.raider.api.resources.LUMBRIDGE_SWAMP

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
private val shrimpsAnchoviesSardineHerringFishSpotIds = intArrayOf(1530)
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

private val sardineHerringFishSpriteIds = intArrayOf(
    ItemID.RAW_SARDINE, ItemID.RAW_HERRING
)
private val sardineHerringFishSpriteNames = arrayOf(
    "Raw sardine", "Raw herring"
)
//

//pike
private val pikeSalmonTroutFishSpotIds = intArrayOf(1527)
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
        shrimpsAnchoviesSardineHerringFishSpotIds,
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
        shrimpsAnchoviesSardineHerringFishSpotIds,
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
    };

    val position = WorldTile(x, y, z)

    open fun location() = ""

    fun fish(): Boolean = getFishSpotQuery()
        .findBestInteractable()
        .map { fishSpot ->
            if (!canReach(fishSpot)) return@map walkTo(fishSpot)
            fishSpot.actions
                .any { action ->
                    this@FishSpot.actions.contains(action) && Waiting.waitUntil { fishSpot.interact(action) } &&
                            Waiting.waitUntilAnimating(10000) &&
                            waitUntilNotAnimating(end = TribotRandom.normal(2000, 187).toLong())
                }
        }
        .orElse(false)

    fun getFishSpotQuery() = Query.npcs()
        .idEquals(*this@FishSpot.ids)
}