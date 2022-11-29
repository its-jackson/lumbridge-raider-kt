package scripts.kt.lumbridge.raider.api.behaviors.prayer

import org.tribot.script.sdk.Bank
import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.frameworks.behaviortree.IParentNode
import org.tribot.script.sdk.frameworks.behaviortree.condition
import org.tribot.script.sdk.frameworks.behaviortree.selector
import org.tribot.script.sdk.frameworks.behaviortree.sequence
import scripts.kotlin.api.walkToAndDepositInvBank
import scripts.kt.lumbridge.raider.api.ScriptTask

fun IParentNode.prayerBehavior(scriptTask: ScriptTask?) = sequence {
    selector {
        condition {
            scriptTask?.scriptPrayerData?.bone
                ?.getBonesInventoryQuery()
                ?.isAny
        }
        sequence {
            walkToAndDepositInvBank(closeBank = false)
            condition {
                scriptTask?.scriptPrayerData?.bone?.spriteId
                    ?.let { Bank.contains(it) }
            }
            condition {
                scriptTask?.scriptPrayerData?.bone?.spriteId
                    ?.let {
                        Bank.withdrawAll(it) && Waiting.waitUntil {
                            Inventory.contains(
                                it
                            )
                        }
                    }
            }
            condition { Bank.close() }
        }
    }

    condition {
        scriptTask?.scriptPrayerData?.buryPattern
            .let { buryPattern ->
                var buryCount = 0

                if (buryPattern == null)
                    scriptTask?.scriptPrayerData?.bone
                        ?.buryAll()
                        ?.let {
                            buryCount = it
                        }
                else
                    scriptTask?.scriptPrayerData?.bone
                        ?.buryAll(buryPattern)
                        ?.let {
                            buryCount = it
                        }

                buryCount > 0
            }
    }
}