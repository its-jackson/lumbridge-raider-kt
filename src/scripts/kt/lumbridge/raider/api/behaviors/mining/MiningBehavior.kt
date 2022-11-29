package scripts.kt.lumbridge.raider.api.behaviors.mining

import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.Waiting.waitUntil
import org.tribot.script.sdk.Waiting.waitUntilAnimating
import org.tribot.script.sdk.frameworks.behaviortree.*
import org.tribot.script.sdk.util.TribotRandom
import scripts.kotlin.api.canReach
import scripts.kt.lumbridge.raider.api.ScriptDisposal
import scripts.kt.lumbridge.raider.api.ScriptTask
import scripts.kt.lumbridge.raider.api.behaviors.banking.initializeBankTask
import scripts.kt.lumbridge.raider.api.behaviors.banking.normalBankingDisposal
import scripts.kotlin.api.waitUntilNotAnimating
import scripts.kotlin.api.walkTo

/**
 * The Mining Behavior Sequence
 *
 * -All rocks in lumbridge (tin, copper, coal, mithril, and addy).
 *
 * -All pickaxes (bronze thru crystal).
 *
 * -Multiple inventory disposals (banking, power mining, and M1D1).
 *
 * -Rock priority (mine rocks based on priority and competitively mine,
 *                  meaning the bot will interrupt the current mining action
 *                      to mine a higher priority rock if it spawns).
 */
fun IParentNode.miningBehavior(scriptTask: ScriptTask?) = sequence {
    initializeBankTask(scriptTask)
    normalBankingDisposal(scriptTask)
    normalOreDroppingDisposal(scriptTask)
    mineOneDropOneDisposal(scriptTask)
    completeMiningAction(scriptTask)
}

private fun IParentNode.completeMiningAction(scriptTask: ScriptTask?) = sequence {
    // remember the priority of the current rock being mined
    var rockPriority = -1

    // ensure the rocks are nearby and reachable
    selector {
        condition {
            scriptTask?.scriptMiningData?.rocks
                ?.map { it.position }
                ?.any { it.distance() < 10 && canReach(it) }
        }
        condition {
            scriptTask?.scriptMiningData?.rocks
                ?.map { it.position }
                ?.any { walkTo(it) }
        }
    }

    // complete the full mining action
    selector {
        // if all rock object queries have no elements then wait until any rock spawns
        sequence {
            condition {
                scriptTask?.scriptMiningData?.rocks
                    ?.map { it.getRockGameObjectQuery() }
                    ?.all { !it.isAny }
            }
            perform {
                waitUntil {
                    scriptTask?.scriptMiningData?.rocks
                        ?.map { it.getRockGameObjectQuery() }
                        ?.any { it.isAny } == true
                }
            }
        }

        // mine the first available rock found in the sequence of queries
        // remember the priority to break out the current mining action
        // if a higher priority rock spawns
        sequence {
            condition {
                scriptTask?.scriptMiningData?.rocks?.indices
                    ?.firstOrNull {
                        scriptTask.scriptMiningData.rocks[it].mineOre()
                    }.let {
                        if (it == null) return@let false
                        rockPriority = it
                        return@let true
                    }
            }
            condition { waitUntilAnimating(TribotRandom.uniform(3000, 7500)) }
            condition {
                waitUntilNotAnimating(
                    end = TribotRandom.uniform(10, 600).toLong(),
                    interrupt = {
                        scriptTask?.scriptMiningData?.rocks
                            ?.map { it.getRockGameObjectQuery() }
                            ?.filterIndexed { priority, rockQuery -> priority < rockPriority && rockQuery.isAny }
                            ?.any() == true
                    }
                )
            }
        }
    }
}

private fun IParentNode.normalOreDroppingDisposal(scriptTask: ScriptTask?) = selector {
    condition { scriptTask?.scriptDisposal != ScriptDisposal.DROP }
    condition { !Inventory.isFull() }
    condition {
        scriptTask?.scriptMiningData?.rocks
            ?.map { it.dropOre() }
            ?.any()
    }
}

private fun IParentNode.mineOneDropOneDisposal(scriptTask: ScriptTask?) = selector {
    condition { scriptTask?.scriptDisposal != ScriptDisposal.M1D1 }
    condition {
        scriptTask?.scriptMiningData?.rocks
            ?.map { it.getOreInventoryQuery() }
            ?.all { !it.isAny }
    }
    condition {
        scriptTask?.scriptMiningData?.rocks
            ?.map { it.dropOre() }
            ?.any()
    }
}