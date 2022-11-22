package scripts.kt.lumbridge.raider.api

import com.google.gson.GsonBuilder
import org.tribot.script.sdk.Combat
import org.tribot.script.sdk.Log
import org.tribot.script.sdk.frameworks.behaviortree.BehaviorTreeStatus
import org.tribot.script.sdk.frameworks.behaviortree.IBehaviorNode
import org.tribot.script.sdk.tasks.BankTask
import org.tribot.script.sdk.types.EquipmentItem
import org.tribot.script.sdk.types.InventoryItem
import org.tribot.script.sdk.util.serialization.StateTypeAdapterFactory
import scripts.kotlin.api.ResourceGainedCondition
import scripts.kotlin.api.Satisfiable
import scripts.kotlin.api.StopCondition
import scripts.kotlin.api.TimeStopCondition
import scripts.kt.lumbridge.raider.api.behaviors.combat.Monster
import scripts.kt.lumbridge.raider.api.behaviors.fishing.FishSpot
import scripts.kt.lumbridge.raider.api.behaviors.mining.Pickaxe
import scripts.kt.lumbridge.raider.api.behaviors.mining.Rock
import scripts.kt.lumbridge.raider.api.behaviors.questing.Quest
import scripts.kt.lumbridge.raider.api.behaviors.scriptLogicBehaviorTree
import scripts.kt.lumbridge.raider.api.behaviors.woodcutting.Axe
import scripts.kt.lumbridge.raider.api.behaviors.woodcutting.Tree

/**
 * @author Nullable
 */
inline fun <reified T> deepCopy(ob: T): T {
    val gson = GsonBuilder()
        .registerTypeAdapterFactory(StateTypeAdapterFactory())
        .create()

    return gson.fromJson(gson.toJson(ob), T::class.java)
}

data class CombatData(
    val monsters: List<Monster>? = null,
    val equipmentItems: List<EquipmentItem>? = null,
    val inventoryItems: List<InventoryItem>? = null,
    val buryLootedBones: Boolean = false,
    val lootGroundItems: Boolean = false
)

data class CombatMeleeData(
    val attackStyle: Combat.AttackStyle? = null,
)

data class CombatMagicData(
    val autoCastableSpell: Combat.AutocastableSpell? = null,
    val spellName: String? = null,
)

data class MiningData(
    val rocks: List<Rock>? = null,
    val pickaxe: Pickaxe? = null,
    val wieldPickaxe: Boolean = false
)

data class WoodcuttingData(
    val trees: List<Tree>? = null,
    val axe: Axe? = null,
    val wieldAxe: Boolean = false
)

data class FishingData(
    val fishSpot: FishSpot? = null
)

data class QuestingData(
    val quest: Quest? = null,
)

data class ScriptTask(
    val stop: StopCondition = TimeStopCondition(days = 28),
    val behavior: Behavior? = null,
    val disposal: Disposal? = null,
    val combatData: CombatData? = null,
    val combatMeleeData: CombatMeleeData? = null,
    val combatMagicData: CombatMagicData? = null,
    val miningData: MiningData? = null,
    val woodcuttingData: WoodcuttingData? = null,
    val fishingData: FishingData? = null,
    val questingData: QuestingData? = null,
    var bankTask: BankTask? = null,
) {
    val resourceGainedCondition: ResourceGainedCondition?
        get() {
            return if (this.stop !is ResourceGainedCondition)
                null
            else
                this.stop
        }
}

enum class Behavior(val characterBehavior: String) {
    COMBAT_MELEE("Combat melee"),
    COMBAT_MAGIC("Combat magic"),
    COMBAT_RANGED("Combat ranged"),
    FISHING("Fishing"),
    WOODCUTTING("Woodcutting"),
    COOKING("Cooking"),
    MINING("Mining"),
    PRAYER("Prayer"),
    QUESTING("Questing")
}

enum class Disposal(val disposalMethod: String) {
    BANK("Bank"),
    DROP("Drop"),
    COOK_THEN_BANK("Cook then bank"),
    COOK_THEN_DROP("Cook then drop"),
    M1D1("M1D1")
}

class ScriptTaskRunner : Satisfiable {
    private val taskStack: ArrayDeque<ScriptTask> = ArrayDeque()

    private var mainBehaviorTree: IBehaviorNode? = null
    private var mainBehaviorTreeState: BehaviorTreeStatus? = null

    var activeScriptTask: ScriptTask? = null

    fun configure(scriptTasks: Array<ScriptTask>) {
        taskStack.clear()
        taskStack.addAll(scriptTasks)
        setNextAndComposeMainBehaviorTree()
    }

    fun run(
        breakOut: () -> Boolean = { false },
        onStart: () -> Unit = { },
        onEnd: () -> Unit = { },
    ) {
        onStart()

        while (!isRunnerComplete()) {
            if (breakOut()) break

            if (mainBehaviorTreeState == BehaviorTreeStatus.KILL) {
                Log.debug(
                    "[ScriptTaskRunner] [${activeScriptTask?.behavior?.characterBehavior}] " +
                            "Killing task session"
                )
                setNextAndComposeMainBehaviorTree()
                continue
            }

            if (isSatisfied()) {
                Log.debug(
                    "[ScriptTaskRunner] [${activeScriptTask?.behavior?.characterBehavior}] " +
                            "Task session has satisfied"
                )
                setNextAndComposeMainBehaviorTree()
                continue
            }

            mainBehaviorTreeState = mainBehaviorTree?.tick()
            Log.debug("[ScriptTaskRunner] ${mainBehaviorTree?.name} ?: [$mainBehaviorTreeState]")
        }

        onEnd()
    }

    fun remaining() = taskStack.size

    private fun isRunnerComplete() = activeScriptTask == null && taskStack.isEmpty()

    private fun setNext() {
        activeScriptTask = taskStack.removeFirstOrNull()
    }

    private fun composeMainBehaviorTree() {
        mainBehaviorTree = scriptLogicBehaviorTree(activeScriptTask)
    }

    private fun setNextAndComposeMainBehaviorTree() {
        setNext()
        composeMainBehaviorTree()
    }

    override fun isSatisfied() = activeScriptTask?.stop?.isSatisfied() == true
}