package scripts.kt.lumbridge.raider.api.behaviors.combat

import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.types.WorldTile
import scripts.kotlin.api.canReach
import scripts.kotlin.api.waitUntilNotAnimating
import scripts.kotlin.api.walkTo
import scripts.kt.lumbridge.raider.api.resources.LUMBRIDGE_CASTLE_EAST
import scripts.kt.lumbridge.raider.api.resources.LUMBRIDGE_CASTLE_WEST

private const val CHICKEN_MONSTER_NAME = "Chicken"
private const val COW_MONSTER_NAME = "Cow"
private const val COW_CALF_MONSTER_NAME = "Cow calf"

private val chickenMonsterLoot = arrayOf(
    "Feather",
    "Bones",
    "Raw chicken"
)
private val cowMonsterLoot = arrayOf(
    "Cowhide",
    "Bones",
    "Raw beef"
)

enum class Monster(
    x: Int, y: Int, z: Int,
    val monsterName: String,
    val monsterLootableGroundItems: Array<String>
) {
    CHICKEN_LUMBRIDGE_EAST(
        3230, 3297, 0,
        CHICKEN_MONSTER_NAME,
        chickenMonsterLoot
    ) {
        override fun location() = LUMBRIDGE_CASTLE_EAST
    },
    CHICKEN_LUMBRIDGE_WEST(
        3178, 3296, 0,
        CHICKEN_MONSTER_NAME,
        chickenMonsterLoot
    ) {
        override fun location() = LUMBRIDGE_CASTLE_WEST
    },
    COW_CALF_LUMBRIDGE_EAST(
        3259, 3276, 0,
        COW_CALF_MONSTER_NAME,
        cowMonsterLoot
    ) {
        override fun location() = LUMBRIDGE_CASTLE_EAST
    },
    COW_CALF_LUMBRIDGE_WEST(
        3203, 3291, 0,
        COW_CALF_MONSTER_NAME,
        cowMonsterLoot
    ) {
        override fun location() = LUMBRIDGE_CASTLE_WEST
    },
    COW_LUMBRIDGE_EAST(
        3259, 3276, 0,
        COW_MONSTER_NAME,
        cowMonsterLoot
    ) {
        override fun location() = LUMBRIDGE_CASTLE_EAST
    },
    COW_LUMBRIDGE_WEST(
        3203, 3291, 0,
        COW_MONSTER_NAME,
        cowMonsterLoot
    ) {
        override fun location() = LUMBRIDGE_CASTLE_WEST
    };

    val centralPosition = WorldTile(x, y, z)

    open fun location() = ""

    override fun toString(): String = this@Monster.monsterName

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
                        end = 2000,
                        step = 250,
                        actions = actions
                    ) &&
                    Waiting.waitUntil(2500) { !it.isValid }
        }
        .orElse(false)

    fun isFighting() = Query.npcs()
        .nameEquals(this@Monster.monsterName)
        .minHealthBarPercent(1.0)
        .isInteractingWithMe
        .isFacingMe
        .isAny
}