package scripts.kt.lumbridge.raider.api.behaviors.combat

import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.types.WorldTile
import scripts.kotlin.api.canReach
import scripts.kotlin.api.waitUntilNotAnimating
import scripts.kotlin.api.walkTo
import scripts.kt.lumbridge.raider.api.resources.*

private const val CHICKEN_MONSTER_NAME = "Chicken"
private const val COW_MONSTER_NAME = "Cow"
private const val COW_CALF_MONSTER_NAME = "Cow calf"
private const val GOBLIN_MONSTER_NAME = "Goblin"
private const val GIANT_RAT_MONSTER_NAME = "Giant rat"
private const val GIANT_FROG_MONSTER_NAME = "Giant frog"
private const val BIG_FROG_MONSTER_NAME = "Big frog"
private const val FROG_MONSTER_NAME = "Frog"

private val lumbridgeSwampEastCoords = arrayOf(3218, 3180, 0)
private val lumbridgeSwampWestCoords = arrayOf(3170, 3189, 0)

enum class Monster(
    x: Int, y: Int, z: Int,
    val monsterName: String,
) {
    CHICKEN_LUMBRIDGE_EAST(
        3230, 3297, 0,
        CHICKEN_MONSTER_NAME
    ) {
        override fun location() = LUMBRIDGE_CASTLE_EAST
    },
    CHICKEN_LUMBRIDGE_WEST(
        3178, 3296, 0,
        CHICKEN_MONSTER_NAME
    ) {
        override fun location() = LUMBRIDGE_CASTLE_WEST
    },
    COW_CALF_LUMBRIDGE_EAST(
        3259, 3276, 0,
        COW_CALF_MONSTER_NAME
    ) {
        override fun location() = LUMBRIDGE_CASTLE_EAST
    },
    COW_CALF_LUMBRIDGE_WEST(
        3203, 3291, 0,
        COW_CALF_MONSTER_NAME
    ) {
        override fun location() = LUMBRIDGE_CASTLE_WEST
    },
    GOBLIN_LUMBRIDGE_CASTLE(
        3252, 3235, 0,
        GOBLIN_MONSTER_NAME
    ) {
        override fun location() = LUMBRIDGE_CASTLE
    },
    COW_LUMBRIDGE_EAST(
        3259, 3276, 0,
        COW_MONSTER_NAME
    ) {
        override fun location() = LUMBRIDGE_CASTLE_EAST
    },
    COW_LUMBRIDGE_WEST(
        3203, 3291, 0,
        COW_MONSTER_NAME
    ) {
        override fun location() = LUMBRIDGE_CASTLE_WEST
    },
    GIANT_RAT_LUMBRIDGE_SWAMP_EAST(
        lumbridgeSwampEastCoords[0], lumbridgeSwampEastCoords[1], lumbridgeSwampEastCoords[2],
        GIANT_RAT_MONSTER_NAME
    ) {
        override fun location() = LUMBRIDGE_SWAMP_EAST
    },
    GIANT_RAT_LUMBRIDGE_SWAMP_WEST(
        lumbridgeSwampWestCoords[0], lumbridgeSwampWestCoords[1], lumbridgeSwampWestCoords[2],
        GIANT_RAT_MONSTER_NAME
    ) {
        override fun location() = LUMBRIDGE_SWAMP_WEST
    },
    GIANT_FROG_LUMBRIDGE_SWAMP(
        3198, 3181, 0,
        GIANT_FROG_MONSTER_NAME
    ) {
        override fun location() = LUMBRIDGE_SWAMP
    },
    BIG_FROG_LUMBRIDGE_SWAMP_EAST(
        lumbridgeSwampEastCoords[0], lumbridgeSwampEastCoords[1], lumbridgeSwampEastCoords[2],
        BIG_FROG_MONSTER_NAME
    ) {
        override fun location() = LUMBRIDGE_SWAMP_EAST
    },
    FROG_LUMBRIDGE_SWAMP_EAST(
        lumbridgeSwampEastCoords[0], lumbridgeSwampEastCoords[1], lumbridgeSwampEastCoords[2],
        FROG_MONSTER_NAME
    ) {
        override fun location() = LUMBRIDGE_SWAMP_EAST
    },
    FROG_LUMBRIDGE_SWAMP_WEST(
        lumbridgeSwampWestCoords[0], lumbridgeSwampWestCoords[1], lumbridgeSwampWestCoords[2],
        FROG_MONSTER_NAME
    ) {
        override fun location() = LUMBRIDGE_SWAMP_WEST
    };

    val centralPosition = WorldTile(x, y, z)

    open fun location() = ""

    fun getMonsterNpcQuery() = Query.npcs()
        .nameEquals(this@Monster.monsterName)
        .isNotBeingInteractedWith

    fun canReachCentralPosition() = canReach(this@Monster.centralPosition)

    fun isCentralPositionNearby() = this@Monster.centralPosition.distance() < 15

    fun walkToCentralPosition() = walkTo(this@Monster.centralPosition)

    fun attack(
        actions: List<() -> Unit> = listOf()
    ): Boolean = getMonsterNpcQuery()
        .findBestInteractable()
        .map {
            if (!canReach(it) && !walkTo(it))
                return@map false
            it.interact("Attack") &&
                    Waiting.waitUntil(6500) { it.isInteractingWithMe } &&
                    Waiting.waitUntilAnimating(3000) &&
                    waitUntilNotAnimating(
                        end = 2500,
                        step = 200,
                        actions = actions
                    ) &&
                    Waiting.waitUntil(2500) { !it.isValid }
        }
        .orElse(false)

    fun isFighting() = Query.npcs()
        .nameEquals(this@Monster.monsterName)
        .isInteractingWithMe
        .isAny
}