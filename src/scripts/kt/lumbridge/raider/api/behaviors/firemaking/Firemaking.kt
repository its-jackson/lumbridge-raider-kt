package scripts.kt.lumbridge.raider.api.behaviors.firemaking

import org.tribot.script.sdk.MyPlayer
import org.tribot.script.sdk.frameworks.behaviortree.IParentNode
import org.tribot.script.sdk.frameworks.behaviortree.sequence
import org.tribot.script.sdk.interfaces.Positionable
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.types.Area
import org.tribot.script.sdk.types.LocalTile
import org.tribot.script.sdk.types.WorldTile
import scripts.kt.lumbridge.raider.api.ScriptTask
import kotlin.streams.toList

/**
 * The implementation of the firemaking iterator
 */
fun IParentNode.firemakingBehavior(scriptTask: ScriptTask?) = sequence {

}

enum class FiremakingLanePattern {
    LONGEST,
    RANDOM
}

class FiremakingLane(
    private val laneType: FiremakingLanePattern = FiremakingLanePattern.LONGEST,
    private val radius: Double = 15.00,
    centreTile: WorldTile = MyPlayer.getTile()
) : Iterable<Positionable> {
    private val area: Area = Area.fromRadius(centreTile, radius.toInt())

    private val lane: LinkedHashSet<Positionable>
        get() {
            return if (laneType == FiremakingLanePattern.LONGEST)
                findLongestLane() ?: linkedSetOf()
            else
                findRandomLane() ?: linkedSetOf()
        }

    private fun isPositionableLightable(positionable: Positionable) = Query.tiles()
        .tileEquals(positionable)
        .filter { it.isWalkable }
        .filter { it.sceneSettings.isEmpty() }
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
        val lanes: LinkedHashSet<LinkedHashSet<Positionable>> = linkedSetOf()

        for (p in validPositionables) {
            var neighbor = p.tile.toLocalTile()
                .getNeighbor(LocalTile.Direction.WEST)
            val lane: LinkedHashSet<Positionable> = linkedSetOf()
            while (isPositionableLightable(neighbor)) {
                lane.add(p)
                neighbor = neighbor.getNeighbor(LocalTile.Direction.WEST)
            }
            lanes.add(lane)
        }

        return lanes
    }

    private fun findLongestLane() = generateLanes().maxByOrNull { it.size }

    private fun findRandomLane() = generateLanes().randomOrNull()

    override fun iterator() = LaneIterator(lane)

    class LaneIterator(private val lane: LinkedHashSet<Positionable>) : Iterator<Positionable> {
        private var index = 0

        override fun hasNext() = index < lane.size

        override fun next() = lane.elementAt(index++)
    }
}