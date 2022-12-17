package scripts.kt.lumbridge.raider.api.behaviors.firemaking

import org.tribot.script.sdk.*
import org.tribot.script.sdk.frameworks.behaviortree.*
import org.tribot.script.sdk.interfaces.Positionable
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.types.Area
import org.tribot.script.sdk.types.LocalTile
import org.tribot.script.sdk.types.WorldTile
import org.tribot.script.sdk.util.TribotRandom
import scripts.kotlin.api.canReach
import scripts.kotlin.api.waitAvgHumanReactionTime
import scripts.kotlin.api.waitUntilNotAnimating
import scripts.kotlin.api.walkTo
import scripts.kt.lumbridge.raider.api.ScriptTask
import kotlin.streams.toList

const val WALK_HERE_ACTION = "Walk here"
const val TINDERBOX_STRING = "Tinderbox"
const val TINDERBOX_ID = 590

/**
 * The implementation of the firemaking iterator.
 * (Only functions as inventory disposal for woodcutting right now.)
 */
fun IParentNode.firemakingBehavior(scriptTask: ScriptTask?) = sequence {
    var lane: FiremakingLane? = null
    var iterator: FiremakingLane.LaneIterator? = null
    var current: Positionable? = null

    repeatUntil({
        scriptTask?.woodcuttingData?.trees?.all { !Inventory.contains(it.logSpriteId) } == true
    }) {
        sequence {
            selector {
                condition { lane != null && iterator?.hasNext() == true }
                sequence {
                    perform { lane = FiremakingLane() }
                    perform { iterator = lane?.iterator() }
                }
            }

            sequence {
                condition { iterator?.hasNext() == true }
                perform { current = iterator?.next() }
            }

            sequence {
                condition { lane?.isPositionableLightable(current!!) == true }
                selector {
                    condition { current?.tile?.equals(MyPlayer.getTile()) == true }
                    condition { current?.tile?.let { it.isVisible && canReach(it) && it.leftClickOnScreen() } }
                    condition { current?.tile?.let { canReach(it) && it.interact(WALK_HERE_ACTION) } }
                    condition { current?.let { walkTo(it) } == true }
                }
                condition { Waiting.waitUntil(7500) { current?.tile?.equals(MyPlayer.getTile()) == true } }
            }

            selector {
                condition { GameState.getSelectedItemName() == TINDERBOX_STRING }
                sequence {
                    condition {
                        Query.inventory()
                            .idEquals(TINDERBOX_ID)
                            .findClosestToMouse()
                            .map { it.ensureSelected() }
                            .orElse(false)
                    }
                    condition { Waiting.waitUntil { GameState.getSelectedItemName() == TINDERBOX_STRING } }
                }
            }

            condition {
                scriptTask?.woodcuttingData?.trees
                    ?.map { it.logSpriteId }
                    ?.any { log ->
                        Query.inventory()
                            .idEquals(log)
                            .findClosestToMouse()
                            .map { it.click() }
                            .orElse(false)
                    }
            }

            condition { Waiting.waitUntilAnimating(2000) }

            condition { waitUntilNotAnimating(end = TribotRandom.normal(625, 5).toLong()) }
        }
    }

    perform { waitAvgHumanReactionTime() }
}

internal class FiremakingLane(
    private val radius: Double = 15.00,
    centreTile: WorldTile = MyPlayer.getTile()
) : Iterable<Positionable> {
    private val area: Area
    private val lane: LinkedHashSet<Positionable>

    init {
        area = Area.fromRadius(centreTile, radius.toInt())
        lane = findLongestLane()
    }

    fun isPositionableLightable(positionable: Positionable) = Query.tiles()
        .tileEquals(positionable)
        .filter { it.isWalkable && it.sceneSettings.isEmpty() }
        .filter { !Query.gameObjects().tileEquals(it).isInteractive.isAny }
        .isAny

    private fun findAllLightablePositionables() = Query.tiles()
        .inArea(area)
        .filter { isPositionableLightable(it) }
        .maxPathDistance(radius)
        .sortedByDistance()
        .stream()
        .map { it.toWorldTile() }
        .toList()

    private fun generateLanes(): LinkedHashSet<LinkedHashSet<Positionable>> {
        val validPositionables = findAllLightablePositionables()
        val out: LinkedHashSet<LinkedHashSet<Positionable>> = linkedSetOf()

        for (p in validPositionables) {
            var neighbor = p.tile.toLocalTile()
                .getNeighbor(LocalTile.Direction.WEST)

            val lane: LinkedHashSet<Positionable> = linkedSetOf()
            lane.add(p)

            while (isPositionableLightable(neighbor)) {
                lane.add(neighbor.toWorldTile())
                neighbor = neighbor.getNeighbor(LocalTile.Direction.WEST)
            }

            out.add(lane)
        }

        return out
    }

    private fun findLongestLane(): LinkedHashSet<Positionable> {
        val generated = generateLanes()
        val longest = generated.maxOfOrNull { it.size }
        val out: LinkedHashSet<Positionable>
        val targets: LinkedHashSet<LinkedHashSet<Positionable>> = linkedSetOf()

        for (lane in generated) {
            val isLongest = longest?.equals(lane.size) ?: false
            if (isLongest) {
                targets.add(lane)
            }
        }

        val randomness = (Math.random() * targets.size).toInt()
        out = targets.elementAt(randomness)

        Log.debug("[FiremakingLaneGenerator] $longest $out")

        return out
    }

    override fun iterator() = LaneIterator(lane)

    class LaneIterator(
        private val lane: LinkedHashSet<Positionable>
    ) : Iterator<Positionable> {
        private var index = 0
        private val length = lane.size

        override fun hasNext() = index < length

        override fun next() = lane.elementAt(index++)
    }
}