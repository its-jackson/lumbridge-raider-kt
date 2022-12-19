package scripts.kt.lumbridge.raider.api.behaviors.woodcutting

import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.MyPlayer
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.types.WorldTile
import scripts.kotlin.api.canReach
import scripts.kotlin.api.walkTo
import scripts.kt.lumbridge.raider.api.resources.LUMBRIDGE_CASTLE

private const val NORMAL_DEAD_TREE = "Dead tree"
private const val NORMAL_TREE_NAME = "Tree"
private const val NORMAL_LOG_SPRITE_NAME = "Logs"
private const val NORMAL_LOG_SPRITE_ID = 1511

private const val OAK_TREE_NAME = "Oak"
private const val OAK_LOG_SPRITE_NAME = "Oak logs"
private const val OAK_LOG_SPRITE_ID = 1521

private const val WILLOW_TREE_NAME = "Willow"
private const val WILLOW_LOG_SPRITE_NAME = "Willow logs"
private const val WILLOW_LOG_SPRITE_ID = 1519

private const val YEW_TREE_NAME = "Yew"
private const val YEW_LOG_SPRITE_NAME = "Yew logs"
private const val YEW_LOG_SPRITE_ID = 1515

enum class Tree(
    val treeName: String,
    val treeLocation: String,
    val logSpriteName: String,
    val logSpriteId: Int,
    val centralRegionTile: WorldTile
) {
    NORMAL_DEAD_LUMBRIDGE_CASTLE_CANOE_STATION(
        NORMAL_DEAD_TREE,
        "$LUMBRIDGE_CASTLE Canoe Station",
        NORMAL_LOG_SPRITE_NAME,
        NORMAL_LOG_SPRITE_ID,
        WorldTile(3247, 3237, 0)
    ),
    NORMAL_DEAD_LUMBRIDGE_CASTLE(
        NORMAL_DEAD_TREE,
        LUMBRIDGE_CASTLE,
        NORMAL_LOG_SPRITE_NAME,
        NORMAL_LOG_SPRITE_ID,
        WorldTile(3169, 3243, 0)
    ),
    NORMAL_LUMBRIDGE_CASTLE(
        NORMAL_TREE_NAME,
        LUMBRIDGE_CASTLE,
        NORMAL_LOG_SPRITE_NAME,
        NORMAL_LOG_SPRITE_ID,
        WorldTile(3189, 3244, 0)
    ),
    OAK_LUMBRIDGE_CASTLE(
        OAK_TREE_NAME,
        LUMBRIDGE_CASTLE,
        OAK_LOG_SPRITE_NAME,
        OAK_LOG_SPRITE_ID,
        WorldTile(3204, 3243, 0)
    ),
    WILLOW_LUMBRIDGE_CASTLE_WINDMILL(
        WILLOW_TREE_NAME,
        "$LUMBRIDGE_CASTLE Windmill",
        WILLOW_LOG_SPRITE_NAME,
        WILLOW_LOG_SPRITE_ID,
        WorldTile(3173, 3265, 0)
    ),
    WILLOW_LUMBRIDGE_CASTLE_BAR(
        WILLOW_TREE_NAME,
        "$LUMBRIDGE_CASTLE Bar",
        WILLOW_LOG_SPRITE_NAME,
        WILLOW_LOG_SPRITE_ID,
        WorldTile(3235, 3241, 0)
    ),
    WILLOW_LUMBRIDGE_CASTLE_HOPS_PATCH(
        WILLOW_TREE_NAME,
        "$LUMBRIDGE_CASTLE Hops Patch",
        WILLOW_LOG_SPRITE_NAME,
        WILLOW_LOG_SPRITE_ID,
        WorldTile(3223, 3306, 0)
    ),
    YEW_LUMBRIDGE_CASTLE(
        YEW_TREE_NAME,
        LUMBRIDGE_CASTLE,
        YEW_LOG_SPRITE_NAME,
        YEW_LOG_SPRITE_ID,
        WorldTile(3169, 3231, 0)
    );

    private val actions = listOf(
        "Chop",
        "Chop down",
        "Chop-down"
    )

    private val regionId = centralRegionTile.regionId

    fun getTreeGameObjectQuery() = Query.gameObjects()
        .nameEquals(this@Tree.treeName)
        .filter { it.tile.regionId == this@Tree.regionId }

    fun canReachCentralTile() = canReach(this@Tree.centralRegionTile)

    fun isCharacterAtRegion() = MyPlayer.getTile().regionId == this@Tree.regionId

    fun walkToCentralRegionTile() = walkTo(this@Tree.centralRegionTile)

    fun dropLogs(): Boolean = Inventory.drop(this@Tree.logSpriteId) > 0

    fun chop(): Boolean = getTreeGameObjectQuery()
        .findBestInteractable()
        .map {
            if (!canReach(it) && !walkTo(it))
                return@map false
            this@Tree.actions.any { action ->
                it.actions.contains(action) && Waiting.waitUntil { it.interact(action) }
            }
        }
        .orElse(false)
}