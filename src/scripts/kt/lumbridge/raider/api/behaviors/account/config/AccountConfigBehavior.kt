package scripts.kt.lumbridge.raider.api.behaviors.account.config

import org.tribot.script.sdk.Camera
import org.tribot.script.sdk.Options
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.frameworks.behaviortree.*
import org.tribot.script.sdk.query.Query
import scripts.kotlin.api.performKill
import scripts.kotlin.api.waitAvgHumanReactionTime
import scripts.kotlin.api.walkToAndOpenBank
import scripts.kt.lumbridge.raider.api.ScriptTask

private fun getMainWidgetQuery() = Query.widgets()
    .inIndexPath(664, 29)
    .actionContains("Close")
    .isVisible

private fun closeNewBankAccountWidget() = getMainWidgetQuery()
    .findFirst()
    .map { it.click() }
    .orElse(false)

fun IParentNode.accountConfigBehavior(scriptTask: ScriptTask?) = sequence {
    selector {
        condition { scriptTask?.accountConfigData?.solveNewCharacterBankSetup == false }
        selector {
            sequence {
                condition { getMainWidgetQuery().isAny }
                condition { closeNewBankAccountWidget() }
                perform { waitAvgHumanReactionTime() }
            }
            sequence {
                walkToAndOpenBank()
                condition { Waiting.waitUntil { getMainWidgetQuery().isAny } }
                condition { closeNewBankAccountWidget() }
                perform { waitAvgHumanReactionTime() }
            }
        }
    }

    selector {
        sequence {
            condition { scriptTask?.accountConfigData?.enableShiftClick == true }
            selector {
                condition { Options.isShiftClickDropEnabled() }
                condition { Options.setShiftClickDrop(true) }
            }
            perform { waitAvgHumanReactionTime() }
        }
        sequence {
            condition { scriptTask?.accountConfigData?.enableShiftClick == false }
            selector {
                inverter { condition { Options.isShiftClickDropEnabled() } }
                condition { Options.setShiftClickDrop(false) }
            }
            perform { waitAvgHumanReactionTime() }
        }
    }

    selector {
        sequence {
            condition { scriptTask?.accountConfigData?.enableRoofs == true }
            selector {
                condition { Options.isRoofsEnabled() }
                condition { Options.setRemoveRoofsEnabled(false) }
            }
            perform { waitAvgHumanReactionTime() }
        }
        sequence {
            condition { scriptTask?.accountConfigData?.enableRoofs == false }
            selector {
                inverter { condition { Options.isRoofsEnabled() } }
                condition { Options.setRemoveRoofsEnabled(true) }
            }
            perform { waitAvgHumanReactionTime() }
        }
    }

    performKill { scriptTask?.accountConfigData?.cameraZoomPercent?.let { Camera.setZoomPercent(it) } }
}