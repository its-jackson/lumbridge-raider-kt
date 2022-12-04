package scripts

import org.tribot.script.sdk.*
import org.tribot.script.sdk.frameworks.behaviortree.BehaviorTreeStatus
import org.tribot.script.sdk.frameworks.behaviortree.behaviorTree
import org.tribot.script.sdk.frameworks.behaviortree.perform
import org.tribot.script.sdk.frameworks.behaviortree.repeatUntil
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
import scripts.kotlin.api.initializeScriptBehaviorTree
import scripts.kt.lumbridge.raider.api.*
import scripts.kt.lumbridge.raider.api.behaviors.combat.Monster
import scripts.kt.lumbridge.raider.api.behaviors.fishing.FishSpot
import scripts.kt.lumbridge.raider.api.behaviors.mining.Pickaxe
import scripts.kt.lumbridge.raider.api.behaviors.mining.Rock
import scripts.kt.lumbridge.raider.api.behaviors.questing.Quest
import scripts.kt.lumbridge.raider.api.behaviors.woodcutting.Axe
import scripts.kt.lumbridge.raider.api.behaviors.woodcutting.Tree
import scripts.kt.lumbridge.raider.api.ui.MainGui
import scripts.kt.lumbridge.raider.api.ui.SwingGuiState
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

    private val scriptTaskGui = MainGui()

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
                .value { scriptTaskRunner.activeScriptTask?.scriptBehavior?.behavior }
                .build()
        )
        .row(
            paintTemplate.toBuilder().label("Disposal")
                .value { scriptTaskRunner.activeScriptTask?.scriptDisposal?.disposal }
                .build()
        )
        .row(
            paintTemplate.toBuilder().label("Stop")
                .value { scriptTaskRunner.activeScriptTask?.scriptAbstractStopCondition }
                .build()
        )
        .row(
            paintTemplate.toBuilder().label("Monsters")
                .value { scriptTaskRunner.activeScriptTask?.scriptCombatData?.monsters?.map { it.monsterName } }
                .build()
        )
        .row(
            paintTemplate.toBuilder()
                .label("Rocks")
                .value { scriptTaskRunner.activeScriptTask?.scriptMiningData?.rocks }
                .build()
        )
        .row(
            paintTemplate.toBuilder()
                .label("Trees")
                .value { scriptTaskRunner.activeScriptTask?.scriptWoodcuttingData?.trees }
                .build()
        )
        .row(
            paintTemplate.toBuilder()
                .label("Fish spot")
                .value { scriptTaskRunner.activeScriptTask?.scriptFishingData?.fishSpot }
                .build()
        )
        .row(
            paintTemplate.toBuilder()
                .label("Quest")
                .value { scriptTaskRunner.activeScriptTask?.scriptQuestingData?.quest }
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
        if (Tribot.getUsername() !in userWhiteList)
            throw RuntimeException("Username is not in whitelist: ${Tribot.getUsername()}")

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
            scriptTaskGui.isVisible = true

            val behaviorTreeGuiState = behaviorTree {
                repeatUntil({ !scriptTaskGui.isVisible }) {
                    perform { Waiting.wait(1000) }
                }
            }.tick()

            if (behaviorTreeGuiState != BehaviorTreeStatus.SUCCESS) return
            if (scriptTaskGui.guiState != SwingGuiState.STARTED) return

            val model = scriptTaskGui.list1.model
            val scriptTaskList: MutableList<ScriptTask> = mutableListOf()

            for (i in 0 until model.size) {
                scriptTaskList.add(model.getElementAt(i))
            }

            scriptTaskRunner.configure(scriptTaskList.toTypedArray())
            scriptTaskRunner.run()
        }
    }

    private fun combatMeleeTest() {
        val combatMeleeTask = ScriptTask(
            scriptBehavior = ScriptBehavior.COMBAT_MELEE,
            scriptCombatData = ScriptCombatData(
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
            scriptBehavior = ScriptBehavior.COMBAT_MAGIC,
            scriptCombatData = ScriptCombatData(
                monsters = listOf(Monster.CHICKEN_LUMBRIDGE_EAST),
                attackStyle = null
            ),
            scriptCombatMagicData = ScriptCombatMagicData(
                autoCastableSpell = Combat.AutocastableSpell.FIRE_STRIKE
            )
        )

        scriptTaskRunner.configure(arrayOf(combatMagicTask))
        scriptTaskRunner.run()
    }

    private fun combatRangedTest() {
        val combatRangedTask = ScriptTask(
            scriptBehavior = ScriptBehavior.COMBAT_RANGED,
            scriptCombatData = ScriptCombatData(
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
                scriptAbstractStopCondition = TimeStopCondition(hours = 6),
                scriptBehavior = ScriptBehavior.FISHING,
                scriptFishingData = ScriptFishingData(fishSpot = FishSpot.SHRIMPS_ANCHOVIES_LUMBRIDGE_SWAMP),
                scriptDisposal = ScriptDisposal.COOK_THEN_BANK
            )
        )

        scriptTaskRunner.configure(scriptTasks)
        scriptTaskRunner.run(onStart = { initializeScriptBehaviorTree().tick() })
    }

    private fun cookingTest() {
        val scriptTasks = arrayOf(
            ScriptTask(
                scriptBehavior = ScriptBehavior.COOKING,
                scriptAbstractStopCondition = ResourceGainedCondition(2132, 100)
            )
        )

        scriptTaskRunner.configure(scriptTasks)
        scriptTaskRunner.run()
    }

    private fun miningTest() {
        val scriptTasks = arrayOf(
            ScriptTask(
                scriptBehavior = ScriptBehavior.MINING,
                scriptDisposal = ScriptDisposal.BANK,
                scriptAbstractStopCondition = SkillLevelsReachedCondition(mapOf(Skill.MINING to 99)),
                scriptMiningData = ScriptMiningData(
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
                scriptBehavior = ScriptBehavior.WOODCUTTING,
                scriptDisposal = ScriptDisposal.DROP,
                scriptAbstractStopCondition = SkillLevelsReachedCondition(mapOf(Skill.WOODCUTTING to 60)),
                scriptWoodcuttingData = ScriptWoodcuttingData(
                    trees = listOf(Tree.WILLOW_LUMBRIDGE_CASTLE_HOPS_PATCH),
                    Axe.BRONZE,
                    true
                ),
            ),
            ScriptTask(
                scriptBehavior = ScriptBehavior.WOODCUTTING,
                scriptDisposal = ScriptDisposal.BANK,
                scriptAbstractStopCondition = SkillLevelsReachedCondition(mapOf(Skill.WOODCUTTING to 99)),
                scriptWoodcuttingData = ScriptWoodcuttingData(
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

    private fun cooksAssistantQuestTest() {
        val questTask = ScriptTask(
            scriptQuestingData = ScriptQuestingData(quest = Quest.COOKS_ASSISTANT),
            scriptBehavior = ScriptBehavior.QUESTING
        )

        scriptTaskRunner.configure(arrayOf(questTask))
        scriptTaskRunner.run()
    }

    private fun prayerTest() {
        val prayerTask = ScriptTask(
            scriptBehavior = ScriptBehavior.PRAYER,
            scriptAbstractStopCondition = ResourceGainedCondition(526)
        )

        scriptTaskRunner.configure(arrayOf(prayerTask))
        scriptTaskRunner.run()
    }
}

