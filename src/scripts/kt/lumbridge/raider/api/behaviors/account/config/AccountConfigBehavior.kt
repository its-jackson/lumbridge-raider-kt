package scripts.kt.lumbridge.raider.api.behaviors.account.config

import org.tribot.script.sdk.Bank
import org.tribot.script.sdk.Camera
import org.tribot.script.sdk.Options
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.frameworks.behaviortree.*
import org.tribot.script.sdk.query.Query
import scripts.kotlin.api.performKill
import scripts.kotlin.api.waitAvgHumanReactionTime
import scripts.kotlin.api.walkToAndOpenBank
import scripts.kt.lumbridge.raider.api.ScriptTask

private fun getNewBankAccountGuideWidgetQuery() = Query.widgets()
    .inIndexPath(664, 29)
    .actionContains("Close")
    .isVisible

private fun closeNewBankAccountGuideWidget() = getNewBankAccountGuideWidgetQuery()
    .findFirst()
    .map { it.click() }
    .orElse(false)

fun IParentNode.accountConfigBehavior(scriptTask: ScriptTask?) = sequence {
    selector {
        condition { scriptTask?.accountConfigData?.solveNewCharacterBankAccGuide == false }
        selector {
            sequence {
                condition { getNewBankAccountGuideWidgetQuery().isAny }
                condition { closeNewBankAccountGuideWidget() }
                condition { Bank.close() }
                perform { waitAvgHumanReactionTime() }
            }
            sequence {
                walkToAndOpenBank()
                condition { Waiting.waitUntil { getNewBankAccountGuideWidgetQuery().isAny } }
                condition { closeNewBankAccountGuideWidget() }
                condition { Bank.close() }
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

    perform { scriptTask?.accountConfigData?.cameraZoomPercent?.let { Camera.setZoomPercent(it) } }

    performKill { Options.closeAllSettings() }
}