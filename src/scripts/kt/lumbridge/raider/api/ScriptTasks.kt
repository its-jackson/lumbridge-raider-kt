package scripts.kt.lumbridge.raider.api

import com.allatori.annotations.DoNotRename
import com.google.gson.GsonBuilder
import org.tribot.script.sdk.Combat
import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.Log
import org.tribot.script.sdk.frameworks.behaviortree.BehaviorTreeStatus
import org.tribot.script.sdk.frameworks.behaviortree.IBehaviorNode
import org.tribot.script.sdk.tasks.BankTask
import org.tribot.script.sdk.types.EquipmentItem
import org.tribot.script.sdk.types.InventoryItem
import org.tribot.script.sdk.util.serialization.StateTypeAdapterFactory
import scripts.kotlin.api.*
import scripts.kt.lumbridge.raider.api.behaviors.combat.Monster
import scripts.kt.lumbridge.raider.api.behaviors.combat.combatBehavior
import scripts.kt.lumbridge.raider.api.behaviors.combat.magic.combatMagicBehavior
import scripts.kt.lumbridge.raider.api.behaviors.cooking.cookingBehavior
import scripts.kt.lumbridge.raider.api.behaviors.fishing.FishSpot
import scripts.kt.lumbridge.raider.api.behaviors.fishing.fishingBehavior
import scripts.kt.lumbridge.raider.api.behaviors.mining.Pickaxe
import scripts.kt.lumbridge.raider.api.behaviors.mining.Rock
import scripts.kt.lumbridge.raider.api.behaviors.mining.miningBehavior
import scripts.kt.lumbridge.raider.api.behaviors.prayer.prayerBehavior
import scripts.kt.lumbridge.raider.api.behaviors.questing.Quest
import scripts.kt.lumbridge.raider.api.behaviors.questing.cooks.assistant.cooksAssistantBehavior
import scripts.kt.lumbridge.raider.api.behaviors.woodcutting.Axe
import scripts.kt.lumbridge.raider.api.behaviors.woodcutting.Tree
import scripts.kt.lumbridge.raider.api.behaviors.woodcutting.woodcuttingBehavior

/**
 * @author Nullable
 */
inline fun <reified T> deepCopy(ob: T): T {
    val gson = GsonBuilder()
        .registerTypeAdapterFactory(StateTypeAdapterFactory())
        .create()

    return gson.fromJson(gson.toJson(ob), T::class.java)
}

data class ScriptMiningData(
    @DoNotRename
    val rocks: List<Rock>? = null,
    @DoNotRename
    val pickaxe: Pickaxe? = null,
    @DoNotRename
    val wieldPickaxe: Boolean = false
)

data class ScriptWoodcuttingData(
    @DoNotRename
    val trees: List<Tree>? = null,
    @DoNotRename
    val axe: Axe? = null,
    @DoNotRename
    val wieldAxe: Boolean = false
)

data class ScriptFishingData(
    @DoNotRename
    val fishSpot: FishSpot? = null
)

data class ScriptPrayerData(
    @DoNotRename
    val buryPattern: Inventory.DropPattern? = null,
)

data class ScriptQuestingData(
    @DoNotRename
    val quest: Quest? = null,
)

data class ScriptCombatMagicData(
    @DoNotRename
    val autoCastableSpell: Combat.AutocastableSpell? = null,
)

data class ScriptCombatData(
    @DoNotRename
    var equipmentItems: List<EquipmentItem>? = null,
    @DoNotRename
    var inventoryItems: List<InventoryItem>? = null,
    @DoNotRename
    var inventoryMap: Map<Int, Int>? = null,
    @DoNotRename
    val attackStyle: Combat.AttackStyle?,
    @DoNotRename
    val monsters: List<Monster>? = null,
    @DoNotRename
    val lootGroundItems: Boolean = false
) {
    class Builder {
        private var equipmentItems: List<EquipmentItem>? = null
        private var inventoryItems: List<InventoryItem>? = null
        private var attackStyle: Combat.AttackStyle? = null
        private var monsters: List<Monster>? = null
        private var lootGroundItems: Boolean = false

        fun equipmentItems(equipmentItems: List<EquipmentItem>?) = apply { this.equipmentItems = equipmentItems }
        fun inventoryItems(inventoryItems: List<InventoryItem>?) = apply { this.inventoryItems = inventoryItems }
        fun attackStyle(attackStyle: Combat.AttackStyle?) = apply { this.attackStyle = attackStyle }
        fun monsters(monsters: List<Monster>?) = apply { this.monsters = monsters }
        fun lootGroundItems(lootGroundItems: Boolean) = apply { this.lootGroundItems = lootGroundItems }

        fun build() = ScriptCombatData(
            attackStyle = this.attackStyle,
            monsters = this.monsters,
            lootGroundItems = this.lootGroundItems,
            equipmentItems = this.equipmentItems,
            inventoryItems = this.inventoryItems
        )
    }
}

data class ScriptTask(
    @DoNotRename
    val stopCondition: AbstractStopCondition = TimeStopCondition(days = 28),
    @DoNotRename
    val behavior: ScriptBehavior? = null,
    @DoNotRename
    val disposal: ScriptDisposal? = null,
    @DoNotRename
    val combatData: ScriptCombatData? = null,
    @DoNotRename
    val combatMagicData: ScriptCombatMagicData? = null,
    @DoNotRename
    val miningData: ScriptMiningData? = null,
    @DoNotRename
    val woodcuttingData: ScriptWoodcuttingData? = null,
    @DoNotRename
    val fishingData: ScriptFishingData? = null,
    @DoNotRename
    val prayerData: ScriptPrayerData? = null,
    @DoNotRename
    val questingData: ScriptQuestingData? = null,
    @DoNotRename
    var bankTask: BankTask? = null,
) {
    val resourceGainedCondition: ResourceGainedCondition?
        get() {
            return if (this.stopCondition !is ResourceGainedCondition)
                null
            else
                this.stopCondition
        }

    class Builder {
        private var stopCondition: AbstractStopCondition = TimeStopCondition(days = 28)
        private var behavior: ScriptBehavior? = null
        private var disposal: ScriptDisposal? = null
        private var combatData: ScriptCombatData? = null
        private var combatMagicData: ScriptCombatMagicData? = null
        private var miningData: ScriptMiningData? = null
        private var woodcuttingData: ScriptWoodcuttingData? = null
        private var fishingData: ScriptFishingData? = null
        private var prayerData: ScriptPrayerData? = null
        private var questingData: ScriptQuestingData? = null

        fun stopCondition(stop: AbstractStopCondition) = apply { this.stopCondition = stop }
        fun behavior(behavior: ScriptBehavior?) = apply { this.behavior = behavior }
        fun disposal(disposal: ScriptDisposal?) = apply { this.disposal = disposal }
        fun combatData(combatData: ScriptCombatData?) = apply { this.combatData = combatData }
        fun combatMagicData(combatMagicData: ScriptCombatMagicData?) = apply { this.combatMagicData = combatMagicData }
        fun miningData(miningData: ScriptMiningData?) = apply { this.miningData = miningData }
        fun woodcuttingData(woodcuttingData: ScriptWoodcuttingData?) = apply { this.woodcuttingData = woodcuttingData }
        fun fishingData(fishingData: ScriptFishingData?) = apply { this.fishingData = fishingData }
        fun prayerData(prayerData: ScriptPrayerData?) = apply { this.prayerData = prayerData }
        fun questingData(questingData: ScriptQuestingData?) = apply { this.questingData = questingData }

        fun build() = ScriptTask(
            stopCondition = this.stopCondition,
            behavior = this.behavior,
            disposal = this.disposal,
            combatData = this.combatData,
            combatMagicData = this.combatMagicData,
            miningData = this.miningData,
            woodcuttingData = this.woodcuttingData,
            fishingData = this.fishingData,
            prayerData = this.prayerData,
            questingData = this.questingData
        )
    }
}

enum class ScriptBehavior(val behavior: String) {
    COMBAT_MELEE("Combat melee"),
    COMBAT_MAGIC("Combat magic"),
    COMBAT_RANGED("Combat ranged"),
    FISHING("Fishing"),
    WOODCUTTING("Woodcutting"),
    COOKING("Cooking"),
    MINING("Mining"),
    QUESTING("Questing"),
    PRAYER("Prayer");

    fun getScriptLogicBehaviorTree(
        activeScriptTask: ScriptTask?,
        breakControlData: ScriptBreakControlData?
    ) = when (this) {
        COMBAT_MELEE, COMBAT_RANGED ->
            scriptLogicBehaviorTree { scriptBreakControl(breakControlData) { combatBehavior(activeScriptTask) } }

        COMBAT_MAGIC ->
            scriptLogicBehaviorTree { scriptBreakControl(breakControlData) { combatMagicBehavior(activeScriptTask) } }

        COOKING ->
            scriptLogicBehaviorTree { scriptBreakControl(breakControlData) { cookingBehavior(activeScriptTask) } }

        FISHING ->
            scriptLogicBehaviorTree { scriptBreakControl(breakControlData) { fishingBehavior(activeScriptTask) } }

        PRAYER ->
            scriptLogicBehaviorTree { scriptBreakControl(breakControlData) { prayerBehavior(activeScriptTask) } }

        WOODCUTTING ->
            scriptLogicBehaviorTree { scriptBreakControl(breakControlData) { woodcuttingBehavior(activeScriptTask) } }

        MINING ->
            scriptLogicBehaviorTree { scriptBreakControl(breakControlData) { miningBehavior(activeScriptTask) } }

        QUESTING ->
            if (activeScriptTask?.questingData?.quest == Quest.COOKS_ASSISTANT)
                scriptLogicBehaviorTree { cooksAssistantBehavior(activeScriptTask) }
            else
                throw IllegalStateException("Quest must be supported.")
    }
}

enum class ScriptDisposal(val disposal: String) {
    BANK("Bank"),
    DROP("Drop"),
    COOK_THEN_BANK("Cook then bank"),
    COOK_THEN_DROP("Cook then drop"),
    M1D1("M1D1")
}

class ScriptTaskRunner : ISatisfiable {
    private val taskQueue: ArrayDeque<ScriptTask> = ArrayDeque()

    private var mainScriptBehaviorTree: IBehaviorNode? = null
    private var mainScriptBehaviorTreeState: BehaviorTreeStatus? = null
    private var scriptBreakControlData: ScriptBreakControlData? = null

    var activeScriptTask: ScriptTask? = null

    fun configure(
        scriptTasks: Array<ScriptTask>,
        breakControlData: ScriptBreakControlData? = null
    ) {
        taskQueue.clear()
        taskQueue.addAll(scriptTasks)
        scriptBreakControlData = breakControlData
        setNextAndComposeMainScriptBehaviorTree()
    }

    fun run(
        breakOut: () -> Boolean = { false },
        onStart: () -> Unit = { },
        onEnd: () -> Unit = { },
    ) {
        onStart()

        while (!isRunnerComplete()) {
            if (breakOut()) break

            if (mainScriptBehaviorTreeState == BehaviorTreeStatus.KILL) {
                Log.debug(
                    "[ScriptTaskRunner] [${activeScriptTask?.behavior?.behavior}] " +
                            "Killing task session"
                )
                setNextAndComposeMainScriptBehaviorTree()
                continue
            }

            if (isSatisfied()) {
                Log.debug(
                    "[ScriptTaskRunner] [${activeScriptTask?.behavior?.behavior}] " +
                            "Task session has satisfied"
                )
                setNextAndComposeMainScriptBehaviorTree()
                continue
            }

            mainScriptBehaviorTreeState = mainScriptBehaviorTree?.tick()
            Log.debug("[ScriptTaskRunner] ${mainScriptBehaviorTree?.name} ?: [$mainScriptBehaviorTreeState]")
        }

        onEnd()
    }

    fun remaining() = taskQueue.size

    private fun isRunnerComplete() = activeScriptTask == null && taskQueue.isEmpty()

    private fun setNext() {
        activeScriptTask = taskQueue.removeFirstOrNull()
    }

    private fun composeMainScriptBehaviorTree() {
        mainScriptBehaviorTree = activeScriptTask?.behavior
            ?.getScriptLogicBehaviorTree(activeScriptTask, scriptBreakControlData)
    }

    private fun setNextAndComposeMainScriptBehaviorTree() {
        setNext()
        composeMainScriptBehaviorTree()
    }

    override fun isSatisfied() = activeScriptTask?.stopCondition?.isSatisfied() == true
}
