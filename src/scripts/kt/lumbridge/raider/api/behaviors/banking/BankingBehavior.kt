package scripts.kt.lumbridge.raider.api.behaviors.banking

import org.tribot.script.sdk.Bank
import org.tribot.script.sdk.Equipment
import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.frameworks.behaviortree.*
import org.tribot.script.sdk.frameworks.behaviortree.nodes.SelectorNode
import org.tribot.script.sdk.frameworks.behaviortree.nodes.SequenceNode
import org.tribot.script.sdk.tasks.Amount
import org.tribot.script.sdk.tasks.BankTask
import org.tribot.script.sdk.tasks.EquipmentReq
import org.tribot.script.sdk.types.InventoryItem
import org.tribot.script.sdk.walking.GlobalWalking
import scripts.kt.lumbridge.raider.api.Behavior
import scripts.kt.lumbridge.raider.api.Disposal
import scripts.kt.lumbridge.raider.api.ScriptTask

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

fun IParentNode.executeBankTask(
    scriptTask: ScriptTask?,
    bankCondition: () -> Boolean = { scriptTask?.bankTask?.isSatisfied() == false },
) = selector {
    condition { !bankCondition() }
    sequence {
        walkToAndOpenBank()
        condition { scriptTask?.bankTask?.execute()?.isEmpty }
    }
}

fun IParentNode.normalBankingDisposal(scriptTask: ScriptTask?): SelectorNode = selector {
    condition { scriptTask?.disposal != Disposal.BANK }
    executeBankTask(
        scriptTask = scriptTask,
        bankCondition = { Inventory.isFull() }
    )
}

/**
 * Ensure the bank task is initialized and the inventory is in good state.
 */
fun IParentNode.initializeBankTask(scriptTask: ScriptTask?): SelectorNode = selector {
    condition { scriptTask?.bankTask != null }
    sequence {
        perform { initBankTask(scriptTask) }
        repeatUntil(BehaviorTreeStatus.SUCCESS) {
            executeBankTask(scriptTask)
        }
    }
}

private fun getAddInvItem(
    bankTaskBuilder: BankTask.Builder,
    id: Int,
    amount: Int
) {
    bankTaskBuilder.addInvItem(
        id = id,
        amount = Amount.of(amount)
    )
}

private fun getAddEquipItem(
    bankTaskBuilder: BankTask.Builder,
    slot: Equipment.Slot,
    id: Int,
    amount: Int
) {
    bankTaskBuilder.addEquipmentItem(
        EquipmentReq.slot(slot)
            .item(
                id = id,
                amount = Amount.of(amount)
            )
    )
}

/**
 * The necessary items for the active task.
 */
private fun initBankTask(scriptTask: ScriptTask?) {
    val bankTaskBuilder = BankTask.builder()

    when (scriptTask?.behavior) {
        Behavior.COMBAT_MELEE,
        Behavior.COMBAT_MAGIC,
        Behavior.COMBAT_RANGED -> {
            scriptTask.combatData?.inventoryItems
                ?.let { inventoryItems ->
                    getInventoryMap(inventoryItems).forEach {
                        getAddInvItem(
                            bankTaskBuilder = bankTaskBuilder,
                            id = it.key,
                            amount = it.value
                        )
                    }
                } ?: getInventoryMap()
                .forEach {
                    getAddInvItem(
                        bankTaskBuilder = bankTaskBuilder,
                        id = it.key,
                        amount = it.value
                    )
                }


            scriptTask.combatData?.equipmentItems
                ?.forEach {
                    getAddEquipItem(
                        bankTaskBuilder = bankTaskBuilder,
                        slot = it.slot,
                        id = it.id,
                        amount = it.stack
                    )
                } ?: Equipment.getAll()
                .forEach {
                    getAddEquipItem(
                        bankTaskBuilder = bankTaskBuilder,
                        slot = it.slot,
                        id = it.id,
                        amount = it.stack
                    )
                }
        }

        Behavior.FISHING -> {
            scriptTask.fishingData?.fishSpot?.equipmentReq?.entries
                ?.forEach { equipment -> bankTaskBuilder.addInvItem(equipment.key, Amount.of(equipment.value)) }
            scriptTask.fishingData?.fishSpot?.baitReq?.entries
                ?.forEach { bait -> bankTaskBuilder.addInvItem(bait.key, Amount.fill(bait.value)) }
        }

        Behavior.MINING -> {
            scriptTask.miningData?.pickaxe
                ?.let {
                    if (scriptTask.miningData.wieldPickaxe)
                        bankTaskBuilder.addEquipmentItem(
                            EquipmentReq.slot(Equipment.Slot.WEAPON)
                                .item(
                                    it.id,
                                    Amount.of(1)
                                )
                        )
                    else
                        bankTaskBuilder.addInvItem(it.id, Amount.of(1))
                }
        }

        Behavior.WOODCUTTING -> {
            scriptTask.woodcuttingData?.axe
                ?.let {
                    if (scriptTask.woodcuttingData.wieldAxe) {
                        bankTaskBuilder.addEquipmentItem(
                            EquipmentReq.slot(Equipment.Slot.WEAPON)
                                .item(it.id, Amount.of(1))
                        )
                    } else {
                        bankTaskBuilder.addInvItem(it.id, Amount.of(1))
                    }
                }
        }

        else -> {
            throw IllegalArgumentException("Behavior must be supported.")
        }
    }

    scriptTask.bankTask = bankTaskBuilder.build()
}

fun getInventoryMap(
    current: List<InventoryItem> = Inventory.getAll()
): Map<Int, Int> {
    val map = mutableMapOf<Int, Int>()

    current.forEach {
        if (map.containsKey(it.id)) {
            val value = map[it.id]!!
            map[it.id] = value + it.stack
        } else {
            map[it.id] = it.stack
        }
    }

    return map
}