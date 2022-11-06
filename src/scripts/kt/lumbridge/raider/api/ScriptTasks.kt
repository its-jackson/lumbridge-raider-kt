package scripts.kt.lumbridge.raider.api

import org.tribot.script.sdk.Log
import org.tribot.script.sdk.frameworks.behaviortree.BehaviorTreeStatus
import org.tribot.script.sdk.frameworks.behaviortree.IBehaviorNode
import org.tribot.script.sdk.tasks.BankTask
import scripts.kt.lumbridge.raider.api.behaviors.scriptLogicBehaviorTree
import java.lang.System.currentTimeMillis
import java.util.concurrent.TimeUnit

//inline fun <reified T> deepCopy(ob: T): T {
//    val gson = GsonBuilder()
//        .registerTypeAdapterFactory(StateTypeAdapterFactory())
//        .create()
//
//    return gson.fromJson(gson.toJson(ob), T::class.java)
//}

data class ScriptTask(
    val stop: StopCondition = TimeStopCondition(days = 31),
    val behavior: Behavior? = null,
    val disposal: Disposal? = null,
    val npc: Npc? = null,
    val fishSpot: FishSpot? = null,
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

enum class Behavior(val characterBehaviour: String) {
    COMBAT_MELEE("Combat melee"),
    COMBAT_RANGED("Combat ranged"),
    COMBAT_MAGIC("Combat magic"),
    FISHING("Fishing"),
    WOODCUTTING("Woodcutting"),
    COOKING("Cooking"),
    FIREMAKING("Firemaking"),
    MINING("Mining"),
    SMITHING("Smithing"),
}

enum class Disposal(val disposalMethod: String) {
    BANK("Bank"),
    DROP("Drop"),
    COOK_THEN_BANK("Cook then bank"),
    COOK_THEN_DROP("Cook then drop")
}

interface Satisfiable {
    fun isSatisfied(): Boolean
}

abstract class StopCondition : Satisfiable {
    enum class ConditionType(val con: String) {
        TIME("Time condition"),
        RESOURCE_GAINED("Resource gained condition")
        ;
    }
}

class TimeStopCondition(
    days: Long = 0,
    hours: Long = 0,
    minutes: Long = 0,
    seconds: Long = 0
) : StopCondition() {
    private var startTime: Long = -1

    private val toMillis = TimeUnit.DAYS.toMillis(days)
        .plus(TimeUnit.HOURS.toMillis(hours))
        .plus(TimeUnit.MINUTES.toMillis(minutes))
        .plus(TimeUnit.SECONDS.toMillis(seconds))

    // true if the time has surpassed
    override fun isSatisfied(): Boolean {
        if (startTime == -1L) startTime = currentTimeMillis()
        return currentTimeMillis() - startTime >= toMillis
    }

    override fun toString(): String {
        if (startTime == -1L) return "(hours:00:minutes:00:seconds:00)"
        val remain = toMillis - (currentTimeMillis() - startTime)
        val hours = remain / 1000 / 60 / 60
        val minutes = remain / 1000 / 60 % 60
        val seconds = remain / 1000 % 60
        return "Time (hours:$hours:minutes:$minutes:seconds:$seconds)"
    }
}

class ResourceGainedCondition(
    val id: Int,
    val amount: Int = -1 // default value is infinity
) : StopCondition() {
    val remainder: Int
        get() = amount - sum

    private var sum = 0

    /**
     * It is expected that anyone who creates an instance must
     * invoke this function manually to update the remainder and sum.
     * Or else the resource amount won't ever satisfy.
     */
    fun updateSum(amt: Int) {
        if (amount < 1) return
        sum = sum.plus(amt)
    }

    override fun isSatisfied(): Boolean =
        amount > 0 && (remainder < 1 && sum >= amount)

    override fun toString(): String =
        "Resource gained (id=$id, amount=$amount, remainder=$remainder, sum=$sum)"
}

class ScriptTaskRunner : Satisfiable {
    private val taskStack: ArrayDeque<ScriptTask> = ArrayDeque()

    private var mainBehaviorTree: IBehaviorNode? = null
    private var mainBehaviorTreeState: BehaviorTreeStatus? = null

    var activeScriptTask: ScriptTask? = ScriptTask()

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
        onStart.invoke() // code that should run before running the script

        while (!isRunnerComplete()) {
            if (breakOut()) break

            if (mainBehaviorTreeState == BehaviorTreeStatus.KILL) {
                Log.error(
                    "[ScriptTaskRunner] [${activeScriptTask?.behavior?.characterBehaviour}] " +
                            "Kill task session, too many consecutive failures!"
                )
                setNextAndComposeMainBehaviorTree()
                continue
            }

            if (isSatisfied()) {
                Log.debug(
                    "[ScriptTaskRunner] [${activeScriptTask?.behavior?.characterBehaviour}] " +
                            "Task session has satisfied"
                )
                setNextAndComposeMainBehaviorTree()
                continue
            }

            mainBehaviorTreeState = mainBehaviorTree?.tick()
            Log.debug("[ScriptTaskRunner] ${mainBehaviorTree?.name} ?: [$mainBehaviorTreeState]")
        }

        onEnd.invoke() // code that should run right before exiting the script
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