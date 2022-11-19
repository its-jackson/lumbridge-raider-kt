package scripts

import org.tribot.script.sdk.Login
import org.tribot.script.sdk.Skill
import org.tribot.script.sdk.Tribot
import org.tribot.script.sdk.painting.Painting
import org.tribot.script.sdk.painting.template.basic.BasicPaintTemplate
import org.tribot.script.sdk.painting.template.basic.PaintRows
import org.tribot.script.sdk.painting.template.basic.PaintTextRow
import org.tribot.script.sdk.script.ScriptConfig
import org.tribot.script.sdk.script.TribotScript
import org.tribot.script.sdk.script.TribotScriptManifest
import scripts.kotlin.api.ResourceGainedCondition
import scripts.kotlin.api.SkillLevelsReachedCondition
import scripts.kotlin.api.TimeStopCondition
import scripts.kt.lumbridge.raider.api.*
import scripts.kt.lumbridge.raider.api.behaviors.fishing.FishSpot
import scripts.kt.lumbridge.raider.api.behaviors.initializeScriptBehaviorTree
import scripts.kt.lumbridge.raider.api.behaviors.mining.Pickaxe
import scripts.kt.lumbridge.raider.api.behaviors.mining.Rock
import scripts.kt.lumbridge.raider.api.behaviors.woodcutting.Axe
import scripts.kt.lumbridge.raider.api.behaviors.woodcutting.Tree
import java.awt.Font

@TribotScriptManifest(
    name = "LumbridgeRaider.kt",
    author = "Polymorphic",
    category = "Skilling",
    description = "Local"
)
class LumbridgeRaiderKt : TribotScript {
    private val userWhiteList = listOf("Polymorphic")

    private val scriptTaskRunner = ScriptTaskRunner()

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
                .value { scriptTaskRunner.activeScriptTask?.behavior?.characterBehavior }
                .build()
        )
        .row(
            paintTemplate.toBuilder().label("Disposal")
                .value { scriptTaskRunner.activeScriptTask?.disposal?.disposalMethod }
                .build()
        )
        .row(
            paintTemplate.toBuilder().label("Stop")
                .value { scriptTaskRunner.activeScriptTask?.stop }
                .build()
        )
        .row(
            paintTemplate.toBuilder().label("Npc")
                .value { scriptTaskRunner.activeScriptTask?.npc }
                .build()
        )
        .row(
            paintTemplate.toBuilder()
                .label("Fish spot")
                .value { scriptTaskRunner.activeScriptTask?.fishSpot }
                .build()
        )
        .row(
            paintTemplate.toBuilder()
                .label("Rocks")
                .value { scriptTaskRunner.activeScriptTask?.miningData?.rocks }
                .build()
        )
        .row(
            paintTemplate.toBuilder()
                .label("Trees")
                .value { scriptTaskRunner.activeScriptTask?.woodcuttingData?.trees }
                .build()
        )
        .row(
            paintTemplate.toBuilder().label("Remaining tasks")
                .value { scriptTaskRunner.remaining() }.build()
        )
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
        //val adapter = DaxWalkerAdapter("", "")
        //adapter.blockTeleports(DaxWalkerAdapter.Teleport.LUMBRIDGE_TELEPORT)
        //GlobalWalking.setEngine(adapter)

        if (Tribot.getUsername() !in userWhiteList)
            throw RuntimeException("Username is not in whitelist: ${Tribot.getUsername()}")

        if (args.equals("/combat/melee/test", true))
            combatTest()
        else if (args.equals("/fishing/test", true))
            fishingTest()
        else if (args.equals("/cooking/test", true))
            cookingTest()
        else if (args.equals("/mining/test", true))
            miningTest()
        else if (args.equals("/woodcutting/test", true))
            woodcuttingTest()
        else
        {
            // TODO
        }
    }

    private fun combatTest() {}

    private fun fishingTest() {
        val scriptTasks = arrayOf(
            ScriptTask(
                stop = TimeStopCondition(hours = 6),
                behavior = Behavior.FISHING,
                fishSpot = FishSpot.SALMON_TROUT_LUMBRIDGE_CASTLE,
                disposal = Disposal.COOK_THEN_BANK
            )
        )

        scriptTaskRunner.configure(scriptTasks)
        scriptTaskRunner.run(onStart = { initializeScriptBehaviorTree().tick() })
    }

    private fun cookingTest() {
        val scriptTasks = arrayOf(
            ScriptTask(
                behavior = Behavior.COOKING,
                stop = ResourceGainedCondition(377, 500)
            )
        )

        scriptTaskRunner.configure(scriptTasks)
        scriptTaskRunner.run(
            onStart = { initializeScriptBehaviorTree().tick() },
            onEnd = { Login.logout() }
        )
    }

    private fun miningTest() {
        val scriptTasks = arrayOf(
            ScriptTask(
                behavior = Behavior.MINING,
                disposal = Disposal.BANK,
                stop = SkillLevelsReachedCondition(mapOf(Skill.MINING to 99)),
                miningData = MiningData(
                    listOf(Rock.COAL_LUMBRIDGE_SWAMP),
                    Pickaxe.RUNE,
                    false
                ),
            )
        )

        scriptTaskRunner.configure(scriptTasks)
        scriptTaskRunner.run()
    }

    private fun woodcuttingTest() {
        val scriptTasks = arrayOf(
            ScriptTask(
                behavior = Behavior.WOODCUTTING,
                disposal = Disposal.BANK,
                stop = SkillLevelsReachedCondition(mapOf(Skill.WOODCUTTING to 30)),
                woodcuttingData = WoodcuttingData(
                    trees = listOf(Tree.OAK_LUMBRIDGE_CASTLE),
                    Axe.BRONZE,
                    true
                ),
            ),
            ScriptTask(
                behavior = Behavior.WOODCUTTING,
                disposal = Disposal.BANK,
                stop = SkillLevelsReachedCondition(mapOf(Skill.WOODCUTTING to 60)),
                woodcuttingData = WoodcuttingData(
                    trees = listOf(Tree.WILLOW_LUMBRIDGE_CASTLE_BAR),
                    Axe.BRONZE,
                    true
                ),
            ),
            ScriptTask(
                behavior = Behavior.WOODCUTTING,
                disposal = Disposal.BANK,
                stop = SkillLevelsReachedCondition(mapOf(Skill.WOODCUTTING to 99)),
                woodcuttingData = WoodcuttingData(
                    trees = listOf(Tree.YEW_LUMBRIDGE_CASTLE),
                    Axe.BRONZE,
                    true
                ),
            )
        )

        scriptTaskRunner.configure(scriptTasks = scriptTasks)
        scriptTaskRunner.run(
            onStart = { initializeScriptBehaviorTree().tick() },
            onEnd = { Login.logout() }
        )
    }
}

