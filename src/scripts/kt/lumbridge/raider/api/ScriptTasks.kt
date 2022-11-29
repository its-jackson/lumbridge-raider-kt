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
import scripts.kt.lumbridge.raider.api.behaviors.combat.magic.combatMagicBehavior
import scripts.kt.lumbridge.raider.api.behaviors.combat.melee.combatMeleeBehavior
import scripts.kt.lumbridge.raider.api.behaviors.combat.ranged.combatRangedBehavior
import scripts.kt.lumbridge.raider.api.behaviors.cooking.cookingBehavior
import scripts.kt.lumbridge.raider.api.behaviors.fishing.FishSpot
import scripts.kt.lumbridge.raider.api.behaviors.fishing.fishingBehavior
import scripts.kt.lumbridge.raider.api.behaviors.mining.Pickaxe
import scripts.kt.lumbridge.raider.api.behaviors.mining.Rock
import scripts.kt.lumbridge.raider.api.behaviors.mining.miningBehavior
import scripts.kt.lumbridge.raider.api.behaviors.prayer.Bone
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
    val monsters: List<Monster>? = null,
    val equipmentItems: List<EquipmentItem>? = null,
    val inventoryItems: List<InventoryItem>? = null,
    val buryLootedBones: Boolean = false,
    val lootGroundItems: Boolean = false
)

data class ScriptCombatMeleeData(
    val attackStyle: Combat.AttackStyle? = null,
)

data class ScriptCombatMagicData(
    val autoCastableSpell: Combat.AutocastableSpell? = null,
    val spellName: String? = null,
)

data class ScriptCombatRangedData(
    val rangedStyle: Combat.AttackStyle? = null
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
    val bone: Bone? = null,
    val buryPattern: Inventory.DropPattern? = null,
)

data class ScriptQuestingData(
    val quest: Quest? = null,
)

data class ScriptTask(
    val scriptStopCondition: StopCondition = TimeStopCondition(days = 28),
    val scriptBehavior: ScriptBehavior? = null,
    val scriptDisposal: ScriptDisposal? = null,
    val scriptCombatData: ScriptCombatData? = null,
    val scriptCombatMeleeData: ScriptCombatMeleeData? = null,
    val scriptCombatMagicData: ScriptCombatMagicData? = null,
    val scriptCombatRangedData: ScriptCombatRangedData? = null,
    val scriptMiningData: ScriptMiningData? = null,
    val scriptWoodcuttingData: ScriptWoodcuttingData? = null,
    val scriptFishingData: ScriptFishingData? = null,
    val scriptPrayerData: ScriptPrayerData? = null,
    val scriptQuestingData: ScriptQuestingData? = null,
    var scriptBankTask: BankTask? = null,
) {
    val resourceGainedCondition: ResourceGainedCondition?
        get() {
            return if (this.scriptStopCondition !is ResourceGainedCondition)
                null
            else
                this.scriptStopCondition
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
        COMBAT_MELEE ->
            scriptLogicBehaviorTree { combatMeleeBehavior(activeScriptTask) }

        COMBAT_MAGIC ->
            scriptLogicBehaviorTree { combatMagicBehavior(activeScriptTask) }

        COMBAT_RANGED ->
            scriptLogicBehaviorTree { combatRangedBehavior(activeScriptTask) }

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

class ScriptTaskRunner : Satisfiable {
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

    override fun isSatisfied() = activeScriptTask?.scriptStopCondition?.isSatisfied() == true
}
