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
import scripts.kt.lumbridge.raider.api.behaviors.firemaking.TINDERBOX_ID

fun IParentNode.executeBankTask(
    scriptTask: ScriptTask?,
    bankingConditions: List<() -> Boolean> = listOf { scriptTask?.bankTask?.isSatisfied() == false }
) = selector {
    condition { bankingConditions.all { !it() } }
    sequence {
        walkToAndOpenBank()
        condition { scriptTask?.bankTask?.execute()?.isEmpty }
    }
}

fun IParentNode.normalBankingDisposal(scriptTask: ScriptTask?): SelectorNode = selector {
    condition { scriptTask?.disposal != ScriptDisposal.BANK }
    executeBankTask(
        scriptTask = scriptTask,
        bankingConditions = listOf { Inventory.isFull() }
    )
}

/**
 * Ensure the bank task is initialized and the inventory is in good state.
 */
fun IParentNode.initializeBankTask(scriptTask: ScriptTask?): SelectorNode = selector {
    condition { scriptTask?.bankTask != null }
    sequence {
        perform { initBankTask(scriptTask) }
        executeBankTask(scriptTask)
    }
}

/**
 * The necessary items for the active task.
 */
private fun initBankTask(scriptTask: ScriptTask?) {
    val bankTaskBuilder = BankTask.builder()

    when (scriptTask?.behavior) {
        ScriptBehavior.COMBAT_MELEE,
        ScriptBehavior.COMBAT_MAGIC,
        ScriptBehavior.COMBAT_RANGED -> {
            scriptTask.combatData?.inventoryItems
                ?.let { inventoryItems ->
                    scriptTask.combatData.inventoryMap = getInventoryMap(inventoryItems)
                    scriptTask.combatData.inventoryMap!!
                        .forEach {
                            getAddInvItem(
                                bankTaskBuilder = bankTaskBuilder,
                                id = it.key,
                                amount = it.value
                            )
                        }
                } ?: run {
                    scriptTask.combatData?.inventoryMap = getInventoryMap()
                    scriptTask.combatData?.inventoryMap!!
                        .forEach {
                            getAddInvItem(
                                bankTaskBuilder = bankTaskBuilder,
                                id = it.key,
                                amount = it.value
                            )
                        }
                }

            scriptTask.combatData?.equipmentItems
                ?.forEach {
                    getAddEquipItem(
                        bankTaskBuilder = bankTaskBuilder,
                        slot = it.slot,
                        id = it.id
                    )
                } ?: run {
                scriptTask.combatData?.equipmentItems = Equipment.getAll()
                scriptTask.combatData?.equipmentItems!!
                    .forEach {
                        getAddEquipItem(
                            bankTaskBuilder = bankTaskBuilder,
                            slot = it.slot,
                            id = it.id
                        )
                    }
            }
        }

        ScriptBehavior.FISHING -> {
            scriptTask.fishingData?.fishSpot?.equipmentReq?.entries
                ?.forEach { equipment -> bankTaskBuilder.addInvItem(equipment.key, Amount.of(equipment.value)) }
            scriptTask.fishingData?.fishSpot?.baitReq?.entries
                ?.forEach { bait -> bankTaskBuilder.addInvItem(bait.key, Amount.fill(bait.value)) }
        }

        ScriptBehavior.MINING -> {
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

        ScriptBehavior.WOODCUTTING -> {
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
            scriptTask.disposal?.let {
                if (it == ScriptDisposal.CHOP_THEN_BURN) {
                    bankTaskBuilder.addInvItem(TINDERBOX_ID, Amount.of(1))
                }
            }
        }

        else -> {
            throw IllegalArgumentException("Behavior must be supported.")
        }
    }

    scriptTask.bankTask = bankTaskBuilder.build()
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
    id: Int
) {
    bankTaskBuilder.addEquipmentItem(
        EquipmentReq.slot(slot)
            .item(
                id = id,
                amount = Amount.fill(1)
            )
    )
}