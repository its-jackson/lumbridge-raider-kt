package scripts

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
                .value { taskRunner.activeTask?.behavior?.characterBehaviour }.build()
        )
        .row(paintTemplate.toBuilder().label("Stop").value { taskRunner.activeTask?.stop }.build())
        .row(paintTemplate.toBuilder().label("Npc").value { taskRunner.activeTask?.npc }.build())
        .row(paintTemplate.toBuilder().label("Fishing spot").value { taskRunner.activeTask?.fishSpot }.build())
        .row(paintTemplate.toBuilder().label("Remaining tasks").value { taskRunner.remaining() }.build())
        .build()

    init {
        Painting.addPaint(mainPaint::render)
    }

    override fun execute(args: String): Unit = script(args)

    private fun script(args: String) {
        if (args.equals("/combat/melee/test", true))
            combatTest()
        else if (args.equals("/fishing/test", true))
            fishingTest()
        else {
            // TODO
        }
    }

    private fun combatTest() {
        val scriptTasks = arrayOf(

            ScriptTask(
                npc = Npc.COWS_LUMBRIDGE_WEST,
                behavior = Behavior.COMBAT_MELEE,
                stop = TimeStopCondition(seconds = 30),
                cookThenBankDisposal = true,
                lootGroundItems = true
            ),
            ScriptTask(
                npc = Npc.CHICKENS_LUMBRIDGE_WEST,
                behavior = Behavior.COMBAT_MELEE,
                stop = TimeStopCondition(seconds = 10),
                cookThenBankDisposal = true,
                lootGroundItems = true
            ),


            ScriptTask(
                npc = Npc.CHICKENS_LUMBRIDGE_EAST,
                behavior = Behavior.COMBAT_MELEE,
                stop = TimeStopCondition(minutes = 5, seconds = 5),
                cookThenBankDisposal = true,
                lootGroundItems = true
            ),
            ScriptTask(
                npc = Npc.COWS_LUMBRIDGE_EAST,
                behavior = Behavior.COMBAT_MELEE,
                stop = TimeStopCondition(minutes = 4, seconds = 20),
                cookThenBankDisposal = true,
                lootGroundItems = true
            )

        )

        taskRunner.configure(scriptTasks)
        taskRunner.run { false }
    }

    private fun fishingTest() {
        val scriptTasks = arrayOf(

            ScriptTask(
                behavior = Behavior.FISHING,
                fishSpot = FishSpot.SALMON_TROUT_LUMBRIDGE_CASTLE,
                cookThenBankDisposal = true,
                npc = null
            )
        )

        taskRunner.configure(scriptTasks)
        taskRunner.run { false }
    }
}

