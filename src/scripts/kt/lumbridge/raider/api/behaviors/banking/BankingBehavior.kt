package scripts.kt.lumbridge.raider.api.behaviors.banking

import org.tribot.script.sdk.Equipment
import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.frameworks.behaviortree.*
import org.tribot.script.sdk.frameworks.behaviortree.nodes.SelectorNode
import org.tribot.script.sdk.tasks.Amount
import org.tribot.script.sdk.tasks.BankTask
import org.tribot.script.sdk.tasks.EquipmentReq
import org.tribot.script.sdk.types.InventoryItem
import scripts.kotlin.api.walkToAndOpenBank
import scripts.kt.lumbridge.raider.api.ScriptBehavior
import scripts.kt.lumbridge.raider.api.ScriptDisposal
import scripts.kt.lumbridge.raider.api.ScriptTask

fun IParentNode.executeBankTask(
    scriptTask: ScriptTask?,
    bankCondition: () -> Boolean = { scriptTask?.scriptBankTask?.isSatisfied() == false },
) = selector {
    condition { !bankCondition() }
    sequence {
        walkToAndOpenBank()
        condition { scriptTask?.scriptBankTask?.execute()?.isEmpty }
    }
}

fun IParentNode.normalBankingDisposal(scriptTask: ScriptTask?): SelectorNode = selector {
    condition { scriptTask?.scriptDisposal != ScriptDisposal.BANK }
    executeBankTask(
        scriptTask = scriptTask,
        bankCondition = { Inventory.isFull() }
    )
}

/**
 * Ensure the bank task is initialized and the inventory is in good state.
 */
fun IParentNode.initializeBankTask(scriptTask: ScriptTask?): SelectorNode = selector {
    condition { scriptTask?.scriptBankTask != null }
    sequence {
        perform { initBankTask(scriptTask) }
        repeatUntil(BehaviorTreeStatus.SUCCESS) {
            executeBankTask(scriptTask)
        }
    }
}

/**
 * The necessary items for the active task.
 */
private fun initBankTask(scriptTask: ScriptTask?) {
    val bankTaskBuilder = BankTask.builder()

    when (scriptTask?.scriptBehavior) {
        ScriptBehavior.COMBAT_MELEE,
        ScriptBehavior.COMBAT_MAGIC,
        ScriptBehavior.COMBAT_RANGED -> {
            scriptTask.scriptCombatData?.inventoryItems
                ?.let { inventoryItems ->
                    getInventoryMap(inventoryItems)
                        .forEach {
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


            scriptTask.scriptCombatData?.equipmentItems
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

        ScriptBehavior.FISHING -> {
            scriptTask.scriptFishingData?.fishSpot?.equipmentReq?.entries
                ?.forEach { equipment -> bankTaskBuilder.addInvItem(equipment.key, Amount.of(equipment.value)) }
            scriptTask.scriptFishingData?.fishSpot?.baitReq?.entries
                ?.forEach { bait -> bankTaskBuilder.addInvItem(bait.key, Amount.fill(bait.value)) }
        }

        ScriptBehavior.MINING -> {
            scriptTask.scriptMiningData?.pickaxe
                ?.let {
                    if (scriptTask.scriptMiningData.wieldPickaxe)
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

        ScriptBehavior.WOODCUTTING -> {
            scriptTask.scriptWoodcuttingData?.axe
                ?.let {
                    if (scriptTask.scriptWoodcuttingData.wieldAxe) {
                        bankTaskBuilder.addEquipmentItem(
                            EquipmentReq.slot(Equipment.Slot.WEAPON)
                                .item(it.id, Amount.of(1))
                        )
                    } else {
                        bankTaskBuilder.addInvItem(it.id, Amount.of(1))
                    }
                }
        }

        else ->
        {
            throw IllegalArgumentException("Behavior must be supported.")
        }
    }

    scriptTask.scriptBankTask = bankTaskBuilder.build()
}

private fun getInventoryMap(
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