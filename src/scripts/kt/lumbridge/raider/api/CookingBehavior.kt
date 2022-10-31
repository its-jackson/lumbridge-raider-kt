package scripts.kt.lumbridge.raider.api

import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.frameworks.behaviortree.IParentNode
import org.tribot.script.sdk.frameworks.behaviortree.condition
import org.tribot.script.sdk.frameworks.behaviortree.nodes.SequenceNode
import org.tribot.script.sdk.frameworks.behaviortree.selector
import org.tribot.script.sdk.frameworks.behaviortree.sequence

fun isCookRawFood(): Boolean = Inventory.getAll()
    .filter { it.name.contains("Raw") }
    .filterNot { it.definition.isNoted }
    .any()

fun IParentNode.walkToAndCookRange(): SequenceNode = sequence {
    selector {
        condition { canReach(Range.optimalRange.position) }
        condition { walkTo(Range.optimalRange.position) }
    }
    condition { Range.cookRawFood(Range.optimalRange) }
}