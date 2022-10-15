package scripts.kt.lumbridge.raider.api

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
    val behaviour: Behaviour = Behaviour.COMBAT_MELEE,
    val npc: Npc = Npc.CHICKENS_LUMBRIDGE_EAST,
    val stop: StopCondition = TimeStopCondition(days = Long.MAX_VALUE),
    val buryBones: Boolean = false,
    val lootGroundItems: Boolean = false,
    val bankDisposal: Boolean = false,
    val cookThenBankDisposal: Boolean = false
)

enum class Behaviour(val characterBehaviour: String) {
    COMBAT_MELEE("Combat melee"),
    COMBAT_RANGED("Combat ranged"),
    COMBAT_MAGIC("Combat magic"),
    FISHING("Fishing"),
    WOODCUTTING("Woodcutting"),
    COOKING("Cooking"),
    FIREMAKING("Firemaking")
}

interface Satisfiable {
    fun isSatisfied(): Boolean
}

abstract class StopCondition : Satisfiable {
    enum class ConditionType(val con: String) {
        TIME("Time condition");
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
        return "Until time >= (hours:$hours:minutes:$minutes:seconds:$seconds)"
    }
}

object ScriptTaskRunner : Satisfiable {
    private val taskStack: ArrayDeque<ScriptTask> = ArrayDeque()
    var activeTask: ScriptTask? = ScriptTask()

    fun configure(scriptTasks: Array<ScriptTask>) {
        taskStack.clear()
        taskStack.addAll(scriptTasks)
        setNext()
    }

    fun remaining(): Int = taskStack.size

    fun isRunnerComplete(): Boolean = activeTask == null && taskStack.isEmpty()

    fun setNext() {
        activeTask = getNext()
    }

    private fun getNext(): ScriptTask? = taskStack.removeFirstOrNull()

    // true when the task is satisfied/done
    override fun isSatisfied(): Boolean = activeTask?.stop?.isSatisfied() == true
}