package scripts.kt.lumbridge.raider.api.behaviors.combat

import org.tribot.script.sdk.MyPlayer
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.types.WorldTile
import scripts.waitUntilNotAnimating

private const val LUMBRIDGE_EAST = "Lumbridge East"
private const val LUMBRIDGE_WEST = "Lumbridge West"

private val chicken = arrayOf("Chicken")
private val chickenLoot = arrayOf("Feather", "Bones", "Raw chicken")

private val cow = arrayOf("Cow", "Cow calf")
private val cowLoot = arrayOf("Cowhide", "Bones", "Raw beef")

enum class Npc(
    x: Int,
    y: Int,
    z: Int,
    val names: Array<String>,
    val lootableGroundItems: Array<String>
) {
    CHICKENS_LUMBRIDGE_EAST(
        3230, 3297, 0,
        chicken,
        chickenLoot
    ) {
        override fun location(): String = LUMBRIDGE_EAST
    },
    CHICKENS_LUMBRIDGE_WEST(
        3178, 3296, 0,
        chicken,
        chickenLoot
    ) {
        override fun location(): String = LUMBRIDGE_WEST
    },
    COWS_LUMBRIDGE_EAST(
        3259, 3276, 0,
        cow,
        cowLoot
    ) {
        override fun location(): String = LUMBRIDGE_EAST
    },
    COWS_LUMBRIDGE_WEST(
        3203, 3291, 0,
        cow,
        cowLoot
    ) {
        override fun location(): String = LUMBRIDGE_WEST
    };

    val position = WorldTile(x, y, z)

    open fun location() = ""

    override fun toString(): String = "${names[0]} at ${location()}"

    companion object {
        fun attack(npc: Npc): Boolean = attack(npc.names)

        fun attack(npc: String): Boolean = attack(arrayOf(npc))

        private fun attack(npcs: Array<String>): Boolean = Query.npcs()
            .nameContains(*npcs)
            .isReachable
            .isNotBeingInteractedWith
            .findBestInteractable()
            .map {
                it.interact("Attack") && Waiting.waitUntilAnimating(6500) &&
                        Waiting.waitUntil { it.isInteractingWithMe } &&
                        waitUntilNotAnimating(2500, 100) &&
                        Waiting.waitUntil(2500) { !it.isValid }
            }
            .orElse(false)

        fun isCombat(npc: Npc): Boolean = isCombat(npc.names)

        fun isCombat(npc: String): Boolean = isCombat(arrayOf(npc))

        private fun isCombat(npcs: Array<String>): Boolean = Query.npcs()
            .nameContains(*npcs)
            .isFacingMe
            .isInteractingWithMe
            .minHealthBarPercent(1.0)
            .isAny && MyPlayer.get().map { it.hitsplats }
            .map { it.size > 0 }
            .orElse(false)
    }
}