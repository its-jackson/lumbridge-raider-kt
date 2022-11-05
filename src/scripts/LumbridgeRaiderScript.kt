package scripts

import org.tribot.script.sdk.Login
import org.tribot.script.sdk.painting.Painting
import org.tribot.script.sdk.painting.template.basic.BasicPaintTemplate
import org.tribot.script.sdk.painting.template.basic.PaintRows
import org.tribot.script.sdk.painting.template.basic.PaintTextRow
import org.tribot.script.sdk.script.ScriptConfig
import org.tribot.script.sdk.script.TribotScript
import org.tribot.script.sdk.script.TribotScriptManifest
import scripts.kt.lumbridge.raider.api.*
import scripts.kt.lumbridge.raider.api.behaviors.initScriptBehaviorTree
import java.awt.Font

@TribotScriptManifest(
    name = "LumbridgeRaider.kt",
    author = "Polymorphic",
    category = "Skilling",
    description = "Local"
)
class LumbridgeRaiderKt : TribotScript {
    private val taskRunner = ScriptTaskRunner()

    private val paintTemplate = PaintTextRow.builder()
        .background(java.awt.Color(66, 66, 66, 180))
        .font(Font("Segoe UI", 0, 12))
        .noBorder()
        .build()

    private val mainPaint = BasicPaintTemplate.builder()
        .row(PaintRows.scriptName(paintTemplate.toBuilder()))
        .row(PaintRows.runtime(paintTemplate.toBuilder()))
        .row(
            paintTemplate.toBuilder().label("Behavior")
                .value { taskRunner.activeScriptTask?.behavior?.characterBehaviour }.build()
        )
        .row(paintTemplate.toBuilder().label("Disposal").value { taskRunner.activeScriptTask?.disposal }.build())
        .row(paintTemplate.toBuilder().label("Stop").value { taskRunner.activeScriptTask?.stop }.build())
        .row(paintTemplate.toBuilder().label("Npc").value { taskRunner.activeScriptTask?.npc }.build())
        .row(paintTemplate.toBuilder().label("Fish spot").value { taskRunner.activeScriptTask?.fishSpot }.build())
        .row(paintTemplate.toBuilder().label("Remaining tasks").value { taskRunner.remaining() }.build())
        .build()

    init {
        Painting.addPaint(mainPaint::render)
    }

    override fun configure(config: ScriptConfig) {
        config.isBreakHandlerEnabled = false
        config.isRandomsAndLoginHandlerEnabled = false
    }

    override fun execute(args: String): Unit = script(args)

    private fun script(args: String) {
        if (args.equals("/combat/melee/test", true))
            combatTest()
        else if (args.equals("/fishing/test", true))
            fishingTest()
        else if (args.equals("/cooking/test", true))
            cookingTest()
        else {
            // TODO
        }
    }

    private fun combatTest() {}

    private fun fishingTest() {
        val scriptTasks = arrayOf(
            ScriptTask(
                behavior = Behavior.FISHING,
                fishSpot = FishSpot.SALMON_TROUT_LUMBRIDGE_CASTLE,
                stop = TimeStopCondition(minutes = 120),
                disposal = Disposal.COOK_THEN_BANK
            )
        )

        taskRunner.configure(scriptTasks)

        taskRunner.run(onStart = { initScriptBehaviorTree().tick() })
    }

    private fun cookingTest() {
        val scriptTasks = arrayOf(
            ScriptTask(
                behavior = Behavior.COOKING,
                stop = ResourceGainedCondition(377, 1000)
            )
        )

        taskRunner.configure(scriptTasks)

        taskRunner.run(
            onStart = { initScriptBehaviorTree().tick() },
            onEnd = { Login.logout() }
        )
    }
}

