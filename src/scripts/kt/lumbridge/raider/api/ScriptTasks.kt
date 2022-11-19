package scripts.kt.lumbridge.raider.api

import org.tribot.script.sdk.Log
import org.tribot.script.sdk.frameworks.behaviortree.BehaviorTreeStatus
import org.tribot.script.sdk.frameworks.behaviortree.IBehaviorNode
import org.tribot.script.sdk.tasks.BankTask
import scripts.kotlin.api.ResourceGainedCondition
import scripts.kotlin.api.Satisfiable
import scripts.kotlin.api.StopCondition
import scripts.kotlin.api.TimeStopCondition
import scripts.kt.lumbridge.raider.api.behaviors.combat.Npc
import scripts.kt.lumbridge.raider.api.behaviors.fishing.FishSpot
import scripts.kt.lumbridge.raider.api.behaviors.mining.Pickaxe
import scripts.kt.lumbridge.raider.api.behaviors.mining.Rock
import scripts.kt.lumbridge.raider.api.behaviors.scriptLogicBehaviorTree
import scripts.kt.lumbridge.raider.api.behaviors.woodcutting.Axe
import scripts.kt.lumbridge.raider.api.behaviors.woodcutting.Tree

//inline fun <reified T> deepCopy(ob: T): T {
//    val gson = GsonBuilder()
//        .registerTypeAdapterFactory(StateTypeAdapterFactory())
//        .create()
//
//    return gson.fromJson(gson.toJson(ob), T::class.java)
//}

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

data class ScriptTask(
    val stop: StopCondition = TimeStopCondition(days = 31),
    val behavior: Behavior? = null,
    val disposal: Disposal? = null,
    val npc: Npc? = null,
    val fishSpot: FishSpot? = null,
    val miningData: MiningData? = null,
    val woodcuttingData: WoodcuttingData? = null,
    val buryBones: Boolean = false,
    val lootGroundItems: Boolean = false,
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
    COMBAT_RANGED("Combat ranged"),
    COMBAT_MAGIC("Combat magic"),
    FISHING("Fishing"),
    WOODCUTTING("Woodcutting"),
    COOKING("Cooking"),
    FIREMAKING("Firemaking"),
    MINING("Mining"),
    SMITHING("Smithing"),
    SMELTING("Smelting"),
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
                Log.error(
                    "[ScriptTaskRunner] [${activeScriptTask?.behavior?.characterBehavior}] " +
                            "Kill task session, too many consecutive failures!"
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

    fun remaining(): Int = taskStack.size

    private fun isRunnerComplete(): Boolean = activeScriptTask == null && taskStack.isEmpty()

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

    override fun isSatisfied(): Boolean = activeScriptTask?.stop?.isSatisfied() == true
}