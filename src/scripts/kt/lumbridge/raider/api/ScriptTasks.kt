package scripts.kt.lumbridge.raider.api

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

data class ScriptCombatData(
    var equipmentItems: List<EquipmentItem>? = null,
    var inventoryItems: List<InventoryItem>? = null,
    var inventoryMap: Map<Int, Int>? = null,
    val attackStyle: Combat.AttackStyle?,
    val monsters: List<Monster>? = null,
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

data class ScriptCombatMagicData(
    val autoCastableSpell: Combat.AutocastableSpell? = null,
)

data class ScriptMiningData(
    val rocks: List<Rock>? = null,
    val pickaxe: Pickaxe? = null,
    val wieldPickaxe: Boolean = false
)

data class ScriptWoodcuttingData(
    val trees: List<Tree>? = null,
    val axe: Axe? = null,
    val wieldAxe: Boolean = false
)

data class ScriptFishingData(
    val fishSpot: FishSpot? = null
)

data class ScriptPrayerData(
    val buryPattern: Inventory.DropPattern? = null,
)

data class ScriptQuestingData(
    val quest: Quest? = null,
)

data class ScriptTask (
    val scriptAbstractStopCondition: AbstractStopCondition = TimeStopCondition(days = 28),
    val scriptBehavior: ScriptBehavior? = null,
    val scriptDisposal: ScriptDisposal? = null,
    val scriptCombatData: ScriptCombatData? = null,
    val scriptCombatMagicData: ScriptCombatMagicData? = null,
    val scriptMiningData: ScriptMiningData? = null,
    val scriptWoodcuttingData: ScriptWoodcuttingData? = null,
    val scriptFishingData: ScriptFishingData? = null,
    val scriptPrayerData: ScriptPrayerData? = null,
    val scriptQuestingData: ScriptQuestingData? = null,
    var scriptBankTask: BankTask? = null,
)  {
    val resourceGainedCondition: ResourceGainedCondition?
        get() {
            return if (this.scriptAbstractStopCondition !is ResourceGainedCondition)
                null
            else
                this.scriptAbstractStopCondition
        }

    // For Java
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
        fun behavior(behavior: ScriptBehavior) = apply { this.behavior = behavior }

        fun combatData(combatData: ScriptCombatData) = apply { this.combatData = combatData }

        fun build() = ScriptTask(
            scriptAbstractStopCondition = this.stopCondition,
            scriptBehavior = this.behavior,
            scriptCombatData = this.combatData
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

    fun getScriptBehaviorTree(activeScriptTask: ScriptTask?) = when (this) {
        COMBAT_MELEE, COMBAT_RANGED ->
            scriptLogicBehaviorTree { combatBehavior(activeScriptTask) }

        COMBAT_MAGIC ->
            scriptLogicBehaviorTree { combatMagicBehavior(activeScriptTask) }

        COOKING ->
            scriptLogicBehaviorTree { cookingBehavior(activeScriptTask) }

        FISHING ->
            scriptLogicBehaviorTree { fishingBehavior(activeScriptTask) }

        PRAYER ->
            scriptLogicBehaviorTree { prayerBehavior(activeScriptTask) }

        WOODCUTTING ->
            scriptLogicBehaviorTree { woodcuttingBehavior(activeScriptTask) }

        MINING ->
            scriptLogicBehaviorTree { miningBehavior(activeScriptTask) }

        QUESTING ->
            if (activeScriptTask?.scriptQuestingData?.quest == Quest.COOKS_ASSISTANT)
                scriptLogicBehaviorTree { cooksAssistantBehavior(activeScriptTask) }
            else
                throw IllegalStateException("Quest must be supported.")
    }
}

// TODO - make sure all behaviors support the required disposal method
enum class ScriptDisposal(val disposal: String) {
    BANK("Bank"),
    DROP("Drop"),
    COOK_THEN_BANK("Cook then bank"),
    COOK_THEN_DROP("Cook then drop"),
    M1D1("M1D1")
}

class ScriptTaskRunner : ISatisfiable {
    private val taskStack: ArrayDeque<ScriptTask> = ArrayDeque()

    private var mainScriptBehaviorTree: IBehaviorNode? = null
    private var mainScriptBehaviorTreeState: BehaviorTreeStatus? = null

    var activeScriptTask: ScriptTask? = null

    fun configure(scriptTasks: Array<ScriptTask>) {
        taskStack.clear()
        taskStack.addAll(scriptTasks)
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
                    "[ScriptTaskRunner] [${activeScriptTask?.scriptBehavior?.behavior}] " +
                            "Killing task session"
                )
                setNextAndComposeMainScriptBehaviorTree()
                continue
            }

            if (isSatisfied()) {
                Log.debug(
                    "[ScriptTaskRunner] [${activeScriptTask?.scriptBehavior?.behavior}] " +
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

    fun remaining() = taskStack.size

    private fun isRunnerComplete() = activeScriptTask == null && taskStack.isEmpty()

    private fun setNext() {
        activeScriptTask = taskStack.removeFirstOrNull()
    }

    private fun composeMainScriptBehaviorTree() {
        mainScriptBehaviorTree = activeScriptTask?.scriptBehavior
            ?.getScriptBehaviorTree(activeScriptTask)
    }

    private fun setNextAndComposeMainScriptBehaviorTree() {
        setNext()
        composeMainScriptBehaviorTree()
    }

    override fun isSatisfied() = activeScriptTask?.scriptAbstractStopCondition?.isSatisfied() == true
}
