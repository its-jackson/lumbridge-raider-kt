package scripts

import org.tribot.script.sdk.Log
import org.tribot.script.sdk.frameworks.behaviortree.*
import org.tribot.script.sdk.painting.Painting
import org.tribot.script.sdk.painting.template.basic.BasicPaintTemplate
import org.tribot.script.sdk.painting.template.basic.PaintRows
import org.tribot.script.sdk.painting.template.basic.PaintTextRow
import org.tribot.script.sdk.script.TribotScript
import org.tribot.script.sdk.script.TribotScriptManifest
import scripts.kt.lumbridge.raider.api.*
import java.awt.Font

@TribotScriptManifest(
    name = "LumbridgeRaider.kt",
    author = "Polymorphic",
    category = "Combat",
    description = "Local"
)
class LumbridgeRaiderKt : TribotScript {
    private val taskRunner: ScriptTaskRunner = ScriptTaskRunner()

    private val paintTemplate = PaintTextRow.builder()
        .background(java.awt.Color(66, 66, 66, 180))
        .font(Font("Segoe UI", 0, 12))
        .noBorder()
        .build()

    private val mainPaint = BasicPaintTemplate.builder()
        .row(PaintRows.scriptName(paintTemplate.toBuilder()))
        .row(PaintRows.runtime(paintTemplate.toBuilder()))
        .row(
            paintTemplate.toBuilder().label("Behaviour")
                .value { taskRunner.activeTask?.behaviour?.characterBehaviour }.build()
        )
        .row(paintTemplate.toBuilder().label("Stop").value { taskRunner.activeTask?.stop }.build())
        .row(paintTemplate.toBuilder().label("Npc").value { taskRunner.activeTask?.npc }.build())
        .row(paintTemplate.toBuilder().label("Remaining tasks").value { taskRunner.remaining() }.build())
        .build()

    init {
        Painting.addPaint(mainPaint::render)
    }

    init {
        val scriptTasks = arrayOf(
            ScriptTask(
                npc = Npc.CHICKENS_LUMBRIDGE_WEST,
                behaviour = Behaviour.COMBAT_MELEE,
                stop = TimeStopCondition(minutes = 10, seconds = 10),
                cookThenBankDisposal = true,
                lootGroundItems = true
            )
        )

        taskRunner.configure(scriptTasks)
    }

    override fun execute(args: String): Unit = taskRunner.run {
        val initBTree = initBehaviour(it)
        val initState = initBTree.tick()
        Log.debug("Initialize ${initBTree.name} ?: [$initState]")
        if (initState != BehaviorTreeStatus.SUCCESS) return@run

        val logicBTree = logicBehaviour(it)
        val logicState = logicBTree.tick()
        Log.debug("LumbridgeRaider.kt ${logicBTree.name} ?: [$logicState]")
    }
}

