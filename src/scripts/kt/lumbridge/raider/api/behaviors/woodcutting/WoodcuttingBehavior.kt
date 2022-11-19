package scripts.kt.lumbridge.raider.api.behaviors.woodcutting

import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.frameworks.behaviortree.IParentNode
import org.tribot.script.sdk.frameworks.behaviortree.condition
import org.tribot.script.sdk.frameworks.behaviortree.selector
import org.tribot.script.sdk.frameworks.behaviortree.sequence
import scripts.kotlin.api.waitUntilNotAnimating
import scripts.kt.lumbridge.raider.api.Behavior
import scripts.kt.lumbridge.raider.api.ScriptTask
import scripts.kt.lumbridge.raider.api.behaviors.banking.initializeBankTask
import scripts.kt.lumbridge.raider.api.behaviors.banking.normalBankingDisposal

fun IParentNode.woodcuttingBehavior(scriptTask: ScriptTask?) = sequence {
    condition { scriptTask?.behavior == Behavior.WOODCUTTING }
    initializeBankTask(scriptTask)
    normalBankingDisposal(scriptTask)
    completeWoodcuttingAction(scriptTask)
}

fun IParentNode.completeWoodcuttingAction(scriptTask: ScriptTask?) = sequence {
    // ensure the character is within the cutting region
    selector {
        condition { scriptTask?.woodcuttingData?.trees?.any { it.isCharacterAtRegion() && it.canReachCentralTile() } }
        condition { scriptTask?.woodcuttingData?.trees?.any { it.walkToCentralRegionTile() } }
    }
    // complete the woodcutting action
    sequence {
        condition { scriptTask?.woodcuttingData?.trees?.any { it.chop() } }
        condition { Waiting.waitUntilAnimating(10000) }
        condition { waitUntilNotAnimating(end = 50) }
    }
}
