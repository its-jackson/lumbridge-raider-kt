package scripts

import com.google.gson.GsonBuilder
import org.tribot.script.sdk.*
import org.tribot.script.sdk.frameworks.behaviortree.*
import org.tribot.script.sdk.painting.Painting
import org.tribot.script.sdk.painting.template.basic.BasicPaintTemplate
import org.tribot.script.sdk.painting.template.basic.PaintRows
import org.tribot.script.sdk.painting.template.basic.PaintTextRow
import org.tribot.script.sdk.script.ScriptConfig
import org.tribot.script.sdk.script.TribotScript
import org.tribot.script.sdk.script.TribotScriptManifest
import org.tribot.script.sdk.util.ScriptSettings
import org.tribot.script.sdk.util.serialization.RuntimeTypeAdapterFactory
import scripts.kotlin.api.*
import scripts.kt.lumbridge.raider.api.*
import scripts.kt.lumbridge.raider.api.behaviors.combat.Monster
import scripts.kt.lumbridge.raider.api.behaviors.fishing.FishSpot
import scripts.kt.lumbridge.raider.api.behaviors.mining.Pickaxe
import scripts.kt.lumbridge.raider.api.behaviors.mining.Rock
import scripts.kt.lumbridge.raider.api.behaviors.questing.Quest
import scripts.kt.lumbridge.raider.api.behaviors.woodcutting.Axe
import scripts.kt.lumbridge.raider.api.behaviors.woodcutting.Tree
import scripts.kt.lumbridge.raider.api.ui.ScriptTaskGui
import scripts.kt.lumbridge.raider.api.ui.SwingGuiState
import java.awt.Font

@TribotScriptManifest(
    name = "LumbridgeRaider.kt",
    author = "Polymorphic",
    category = "Skilling",
    description = "Local"
)
class LumbridgeRaiderKt : TribotScript {
    private val scriptTaskRunner = ScriptTaskRunner()

    private var scriptTaskGui: ScriptTaskGui? = ScriptTaskGui()

    private val paintTemplate = PaintTextRow.builder()
        .background(java.awt.Color(66, 66, 66, 180))
        .font(Font("Segoe UI", 0, 12))
        .noBorder()
        .build()

    private val mainPaint = BasicPaintTemplate.builder()
        .row(PaintRows.versionedScriptName(paintTemplate.toBuilder()))
        .row(PaintRows.runtime(paintTemplate.toBuilder()))
        .row(
            paintTemplate.toBuilder().label("Stop")
                .value { scriptTaskRunner.activeScriptTask?.stopCondition }
                .build()
        )
        .row(
            paintTemplate.toBuilder().label("Disposal")
                .value { scriptTaskRunner.activeScriptTask?.disposal?.disposal }
                .build()
        )
        .row(
            paintTemplate.toBuilder().label("Monsters")
                .value { scriptTaskRunner.activeScriptTask?.combatData?.monsters?.map { it.monsterName } }
                .build()
        )
        .row(
            paintTemplate.toBuilder()
                .label("Rocks")
                .value { scriptTaskRunner.activeScriptTask?.miningData?.rocks?.map { it.oreSpriteName } }
                .build()
        )
        .row(
            paintTemplate.toBuilder()
                .label("Trees")
                .value { scriptTaskRunner.activeScriptTask?.woodcuttingData?.trees?.map { it.treeName } }
                .build()
        )
        .row(
            paintTemplate.toBuilder()
                .label("Fish spot")
                .value { scriptTaskRunner.activeScriptTask?.fishingData?.fishSpot?.spriteNames }
                .build()
        )
        .row(
            paintTemplate.toBuilder()
                .label("Quest")
                .value { scriptTaskRunner.activeScriptTask?.questingData?.quest?.questName }
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
        if (args.equals("/combat/melee/test", true))
            combatMeleeTest()
        else if (args.equals("/combat/ranged/test", true))
            combatRangedTest()
        else if (args.equals("/combat/magic/test", true))
            combatMagicTest()
        else if (args.equals("/fishing/test", true))
            fishingTest()
        else if (args.equals("/prayer/test", true))
            prayerTest()
        else if (args.equals("/cooking/test", true))
            cookingTest()
        else if (args.equals("/mining/test", true))
            miningTest()
        else if (args.equals("/woodcutting/test", true))
            woodcuttingTest()
        else if (args.equals("/questing/cooks/assistant/test", true))
            cooksAssistantQuestTest()
        else
        {
            scriptTaskGui?.isVisible = true

            val adapter = RuntimeTypeAdapterFactory.of(AbstractStopCondition::class.java)
                .registerSubtype(TimeStopCondition::class.java)
                .registerSubtype(SkillLevelsReachedCondition::class.java)
                .registerSubtype(ResourceGainedCondition::class.java)

            val gson = GsonBuilder()
                .registerTypeAdapterFactory(adapter)
                .setPrettyPrinting()
                .create()

            val handler = ScriptSettings.builder()
                .gson(gson)
                .build()

            scriptTaskGui?.setDefaultSettingsHandler(handler)

            val behaviorTreeGuiState = behaviorTree {
                repeatUntil({ scriptTaskGui?.isVisible == false }) {
                    sequence {
                        loginAction()
                        perform { Waiting.wait(1000) }
                    }
                }
            }.tick()

            if (behaviorTreeGuiState != BehaviorTreeStatus.SUCCESS) return
            if (scriptTaskGui?.scriptTaskGuiState != SwingGuiState.STARTED) return

            val breakData = scriptTaskGui?.scriptBreakControlData
            val model = scriptTaskGui?.list1?.model
            val scriptTaskList: MutableList<ScriptTask> = mutableListOf()

            for (i in 0 until model?.size!!) {
                scriptTaskList.add(model.getElementAt(i))
            }

            scriptTaskGui = null // de-reference the script gui (assist garbage collection early)
            scriptTaskRunner.configure(scriptTaskList.toTypedArray(), breakData)
            scriptTaskRunner.run(
                onStart = { initializeScriptBehaviorTree().tick() },
                onEnd = { Login.logout() }
            )
        }
    }

    private fun combatMeleeTest() {
        val combatMeleeTask = ScriptTask(
            behavior = ScriptBehavior.COMBAT_MELEE,
            combatData = ScriptCombatData(
                attackStyle = Combat.AttackStyle.ACCURATE,
                monsters = listOf(Monster.GIANT_FROG_LUMBRIDGE_SWAMP),
                lootGroundItems = true
            )
        )

        val scriptTasks = arrayOf(combatMeleeTask)

        scriptTaskRunner.configure(scriptTasks)
        scriptTaskRunner.run()
    }

    private fun combatMagicTest() {
        val combatMagicTask = ScriptTask(
            behavior = ScriptBehavior.COMBAT_MAGIC,
            combatData = ScriptCombatData(
                monsters = listOf(Monster.CHICKEN_LUMBRIDGE_EAST),
                attackStyle = null
            ),
            combatMagicData = ScriptCombatMagicData(
                autoCastableSpell = Combat.AutocastableSpell.FIRE_STRIKE
            )
        )

        scriptTaskRunner.configure(arrayOf(combatMagicTask))
        scriptTaskRunner.run()
    }

    private fun combatRangedTest() {
        val combatRangedTask = ScriptTask(
            behavior = ScriptBehavior.COMBAT_RANGED,
            combatData = ScriptCombatData(
                monsters = listOf(Monster.CHICKEN_LUMBRIDGE_EAST),
                attackStyle = Combat.AttackStyle.RANGED_ACCURATE,
                lootGroundItems = true
            )
        )

        scriptTaskRunner.configure(arrayOf(combatRangedTask))
        scriptTaskRunner.run()
    }

    private fun fishingTest() {
        val scriptTasks = arrayOf(
            ScriptTask(
                stopCondition = TimeStopCondition(hours = 6),
                behavior = ScriptBehavior.FISHING,
                fishingData = ScriptFishingData(fishSpot = FishSpot.SHRIMPS_ANCHOVIES_LUMBRIDGE_SWAMP),
                disposal = ScriptDisposal.COOK_THEN_BANK
            )
        )

        scriptTaskRunner.configure(scriptTasks)
        scriptTaskRunner.run(onStart = { initializeScriptBehaviorTree().tick() })
    }

    private fun cookingTest() {
        val scriptTasks = arrayOf(
            ScriptTask(
                behavior = ScriptBehavior.COOKING,
                stopCondition = ResourceGainedCondition(2132, 100)
            )
        )

        scriptTaskRunner.configure(scriptTasks)
        scriptTaskRunner.run()
    }

    private fun miningTest() {
        val scriptTasks = arrayOf(
            ScriptTask(
                behavior = ScriptBehavior.MINING,
                disposal = ScriptDisposal.BANK,
                stopCondition = SkillLevelsReachedCondition(mapOf(Skill.MINING to 99)),
                miningData = ScriptMiningData(
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
                behavior = ScriptBehavior.WOODCUTTING,
                disposal = ScriptDisposal.CHOP_THEN_BURN,
                stopCondition = SkillLevelsReachedCondition(mapOf(Skill.WOODCUTTING to 99)),
                woodcuttingData = ScriptWoodcuttingData(
                    trees = listOf(Tree.NORMAL_LUMBRIDGE_CASTLE, Tree.NORMAL_DEAD_LUMBRIDGE_CASTLE),
                    Axe.RUNE,
                    true
                ),
            ),
        )

        scriptTaskRunner.configure(scriptTasks = scriptTasks)
        scriptTaskRunner.run()
    }

    private fun cooksAssistantQuestTest() {
        val questTask = ScriptTask(
            questingData = ScriptQuestingData(quest = Quest.COOKS_ASSISTANT),
            behavior = ScriptBehavior.QUESTING
        )

        scriptTaskRunner.configure(arrayOf(questTask))
        scriptTaskRunner.run()
    }

    private fun prayerTest() {
        val prayerTask = ScriptTask(
            behavior = ScriptBehavior.PRAYER,
            stopCondition = ResourceGainedCondition(526, 56),
            prayerData = ScriptPrayerData(Inventory.DropPattern.ZIGZAG)
        )

        scriptTaskRunner.configure(arrayOf(prayerTask))
        scriptTaskRunner.run()
    }
}

