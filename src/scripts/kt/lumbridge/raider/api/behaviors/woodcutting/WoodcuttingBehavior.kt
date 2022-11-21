package scripts.kt.lumbridge.raider.api.behaviors.woodcutting

import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.frameworks.behaviortree.*
import org.tribot.script.sdk.util.TribotRandom
import scripts.kotlin.api.waitUntilNotAnimating
import scripts.kt.lumbridge.raider.api.Behavior
import scripts.kt.lumbridge.raider.api.Disposal
import scripts.kt.lumbridge.raider.api.ScriptTask
import scripts.kt.lumbridge.raider.api.behaviors.banking.initializeBankTask
import scripts.kt.lumbridge.raider.api.behaviors.banking.normalBankingDisposal

fun IParentNode.woodcuttingBehavior(scriptTask: ScriptTask?) = sequence {
    condition { scriptTask?.behavior == Behavior.WOODCUTTING }
    initializeBankTask(scriptTask)
    normalBankingDisposal(scriptTask)
    normalDropLogsDisposal(scriptTask)
    completeWoodcuttingAction(scriptTask)
}

private fun IParentNode.completeWoodcuttingAction(scriptTask: ScriptTask?) = sequence {
    // ensure the character is within the cutting region
    selector {
        condition { scriptTask?.woodcuttingData?.trees?.any { it.isCharacterAtRegion() && it.canReachCentralTile() } }
        condition { scriptTask?.woodcuttingData?.trees?.any { it.walkToCentralRegionTile() } }
    }
    // ensure tree is available
    selector {
        condition { scriptTask?.woodcuttingData?.trees?.any { it.getTreeGameObjectQuery().isAny } }
        perform { scriptTask?.woodcuttingData?.trees?.any { Waiting.waitUntil { it.getTreeGameObjectQuery().isAny } } }
    }
    // complete the woodcutting action
    sequence {
        condition { scriptTask?.woodcuttingData?.trees?.any { it.chop() } }
        condition { Waiting.waitUntilAnimating(TribotRandom.uniform(7500, 10000)) }
        condition { waitUntilNotAnimating(end = TribotRandom.uniform(50.0, 600.0).toLong()) }
    }
}

private fun IParentNode.normalDropLogsDisposal(scriptTask: ScriptTask?) = selector {
    condition { scriptTask?.disposal != Disposal.DROP }
    condition { !Inventory.isFull() }
    condition { scriptTask?.woodcuttingData?.trees?.map { it.dropLogs() }?.any() == true }
}
