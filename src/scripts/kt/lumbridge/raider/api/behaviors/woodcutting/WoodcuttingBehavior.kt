package scripts.kt.lumbridge.raider.api.behaviors.woodcutting

import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.antiban.PlayerPreferences
import org.tribot.script.sdk.frameworks.behaviortree.*
import org.tribot.script.sdk.util.TribotRandom
import scripts.kotlin.api.waitUntilNotAnimating
import scripts.kt.lumbridge.raider.api.ScriptDisposal
import scripts.kt.lumbridge.raider.api.ScriptTask
import scripts.kt.lumbridge.raider.api.behaviors.banking.initializeBankTask
import scripts.kt.lumbridge.raider.api.behaviors.banking.normalBankingDisposal
import scripts.kt.lumbridge.raider.api.behaviors.firemaking.firemakingBehavior

private val woodcuttingWaitMean: Int =
    PlayerPreferences.preference(
        "scripts.kt.lumbridge.raider.api.behaviors.woodcutting.WoodcuttingBehavior.woodcuttingWaitMean"
    ) { g: PlayerPreferences.Generator ->
        g.uniform(300, 5000)
    }

private val woodcuttingWaitStd: Int =
    PlayerPreferences.preference(
        "scripts.kt.lumbridge.raider.api.behaviors.woodcutting.WoodcuttingBehavior.woodcuttingWaitStd"
    ) { g: PlayerPreferences.Generator ->
        g.uniform(5, 30)
    }

fun IParentNode.woodcuttingBehavior(scriptTask: ScriptTask?) = sequence {
    initializeBankTask(scriptTask)
    normalBankingDisposal(scriptTask)
    normalDropLogsDisposal(scriptTask)
    chopThenBurnDisposal(scriptTask)
    completeWoodcuttingAction(scriptTask)
    perform { Waiting.waitNormal(woodcuttingWaitMean, woodcuttingWaitStd) }
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
    condition { scriptTask?.disposal != ScriptDisposal.DROP }
    condition { !Inventory.isFull() }
    condition { scriptTask?.woodcuttingData?.trees?.map { it.dropLogs() }?.any() == true }
}

private fun IParentNode.chopThenBurnDisposal(scriptTask: ScriptTask?) = selector {
    condition { scriptTask?.disposal != ScriptDisposal.CHOP_THEN_BURN }
    condition { !Inventory.isFull() }
    firemakingBehavior(scriptTask)
}
