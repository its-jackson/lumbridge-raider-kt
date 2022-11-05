package scripts.kt.lumbridge.raider.api.behaviors.banking

import org.tribot.script.sdk.Bank
import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.frameworks.behaviortree.IParentNode
import org.tribot.script.sdk.frameworks.behaviortree.condition
import org.tribot.script.sdk.frameworks.behaviortree.nodes.SequenceNode
import org.tribot.script.sdk.frameworks.behaviortree.selector
import org.tribot.script.sdk.frameworks.behaviortree.sequence
import org.tribot.script.sdk.walking.GlobalWalking

/**
 * This function can be called on an [IParentNode], which means it can be called in lambdas for nodes
 * such as "sequence" and "selector".
 */
fun IParentNode.walkToAndOpenBank(): SequenceNode = sequence {
    selector {
        condition { Bank.ensureOpen() }
        sequence {
            selector {
                condition { Bank.isNearby() } // at the bank? good, we are done now.
                condition { GlobalWalking.walkToBank() } // we aren't at the bank, let's walk to it.
            }
            condition { Bank.ensureOpen() }
        }
    }
}

fun IParentNode.walkToAndDepositInvBank(closeBank: Boolean = true): SequenceNode = sequence {
    walkToAndOpenBank()
    selector {
        condition { Inventory.isEmpty() }
        condition { Bank.depositInventory() }
    }
    selector {
        condition { !closeBank }
        condition { Bank.close() }
    }
}