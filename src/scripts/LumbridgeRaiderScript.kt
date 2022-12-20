package scripts

import com.google.gson.GsonBuilder
import org.tribot.script.sdk.*
import org.tribot.script.sdk.frameworks.behaviortree.*
import org.tribot.script.sdk.painting.MousePaint
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
import java.awt.Color
import java.awt.Font

@TribotScriptManifest(
    name = "LumbridgeRaider.kt",
    author = "Polymorphic",
    category = "Skilling",
    description = "Local"
)
class LumbridgeRaiderKt : TribotScript {
    private var scriptTaskRunner: ScriptTaskRunner? = null
    private var scriptTaskGui: ScriptTaskGui? = null
    private var scriptPaintTemplate: PaintTextRow? =  null
    private var scriptMainPaint: BasicPaintTemplate? = null

    override fun configure(config: ScriptConfig) {
        config.isBreakHandlerEnabled = false
        config.isRandomsAndLoginHandlerEnabled = false
    }

    override fun execute(args: String) {
        preScript()
        script(args)
    }

    private fun preScript() {
        scriptTaskRunner = ScriptTaskRunner()

        scriptPaintTemplate = PaintTextRow.builder()
            .background(Color(66, 66, 66, 180))
            .font(Font(Font.SANS_SERIF, 0, 10))
            .noBorder()
            .build()

        scriptMainPaint = BasicPaintTemplate.builder()
            .row(PaintRows.scriptName(scriptPaintTemplate!!.toBuilder()))
            .row(PaintRows.runtime(scriptPaintTemplate!!.toBuilder()))
            .row(
                scriptPaintTemplate!!.toBuilder().label("Stop")
                    .value { scriptTaskRunner!!.activeScriptTask?.stopCondition }
                    .build()
            )
            .row(
                scriptPaintTemplate!!.toBuilder().label("Behavior")
                    .value { scriptTaskRunner!!.activeScriptTask?.behavior?.behavior }
                    .build()
            )
            .row(
                scriptPaintTemplate!!.toBuilder().label("Disposal")
                    .value { scriptTaskRunner!!.activeScriptTask?.disposal?.disposal }
                    .build()
            )
            .row(
                scriptPaintTemplate!!.toBuilder().label("Monsters")
                    .value { scriptTaskRunner!!.activeScriptTask?.combatData?.monsters?.map { it.monsterName } }
                    .build()
            )
            .row(
                scriptPaintTemplate!!.toBuilder()
                    .label("Rocks")
                    .value { scriptTaskRunner!!.activeScriptTask?.miningData?.rocks?.map { it.oreSpriteName } }
                    .build()
            )
            .row(
                scriptPaintTemplate!!.toBuilder()
                    .label("Trees")
                    .value { scriptTaskRunner!!.activeScriptTask?.woodcuttingData?.trees?.map { it.treeName } }
                    .build()
            )
            .row(
                scriptPaintTemplate!!.toBuilder()
                    .label("Fishing")
                    .value { scriptTaskRunner!!.activeScriptTask?.fishingData?.fishSpot?.spriteNames?.toList() }
                    .build()
            )
            .row(
                scriptPaintTemplate!!.toBuilder()
                    .label("Quest")
                    .value { scriptTaskRunner!!.activeScriptTask?.questingData?.quest?.questName }
                    .build()
            )
            .row(
                scriptPaintTemplate!!.toBuilder().label("Remaining tasks")
                    .value { scriptTaskRunner!!.remaining() }
                    .build()
            )
            .build()

        //scriptMainPaint?.let { paint -> Painting.addPaint{ paint.render(it) } }
        Painting.setMousePaint { _, _, _ -> }
        Painting.setMouseSplinePaint { _, _ -> }
    }

    private fun script(args: String) {
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

        if (args.isNotBlank() && args.isNotEmpty()) {
            if (args.equals("/combat/melee/test", true))
                combatMeleeTest()
            else if (args.equals("/account/config/test", true))
                accountConfigTest()
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
            else if (args.equals("/death/test", true)) {

            }
            else {
                handler.load(args, Array<ScriptTask>::class.java)
                    .ifPresentOrElse({
                        scriptTaskRunner?.configure(it)
                        scriptTaskRunner?.run(onEnd = { Login.logout() })
                    }) { throw RuntimeException("Unable to load profile settings [$args]") }
            }
        }
        else {
            scriptTaskGui = ScriptTaskGui()
            scriptTaskGui?.isVisible = true
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

            val scriptBreakData = scriptTaskGui?.scriptBreakControlData
            val scriptTaskListModel = scriptTaskGui?.list1?.model
            val scriptTaskList: MutableList<ScriptTask> = mutableListOf()

            for (i in 0 until scriptTaskListModel?.size!!) {
                scriptTaskList.add(scriptTaskListModel.getElementAt(i))
            }

            scriptTaskGui = null

            scriptTaskRunner?.configure(
                scriptTasks = scriptTaskList.toTypedArray(),
                scriptBreakData = scriptBreakData
            )

            scriptTaskRunner?.run(onEnd = { Login.logout() })
        }
    }

    private fun accountConfigTest() {
        val accountConfigData = ScriptTask(
            behavior = ScriptBehavior.ACCOUNT_CONFIG,
            accountConfigData = ScriptAccountConfigData(
                solveNewCharacterBankAccGuide = false,
                cameraZoomPercent = 44.47,
                enableRoofs = false,
                enableShiftClick = true
            )
        )

        scriptTaskRunner?.configure(arrayOf(accountConfigData))
        scriptTaskRunner?.run()
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

        scriptTaskRunner?.configure(scriptTasks)
        scriptTaskRunner?.run()
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

        scriptTaskRunner?.configure(arrayOf(combatMagicTask))
        scriptTaskRunner?.run()
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

        scriptTaskRunner?.configure(arrayOf(combatRangedTask))
        scriptTaskRunner?.run()
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

        scriptTaskRunner?.configure(scriptTasks)
        scriptTaskRunner?.run(onStart = { initializeScriptBehaviorTree().tick() })
    }

    private fun cookingTest() {
        val scriptTasks = arrayOf(
            ScriptTask(
                behavior = ScriptBehavior.COOKING,
                stopCondition = ResourceGainedCondition(2132, 100)
            )
        )

        scriptTaskRunner?.configure(scriptTasks)
        scriptTaskRunner?.run()
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

        scriptTaskRunner?.configure(scriptTasks)
        scriptTaskRunner?.run()
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

        scriptTaskRunner?.configure(scriptTasks = scriptTasks)
        scriptTaskRunner?.run()
    }

    private fun cooksAssistantQuestTest() {
        val questTask = ScriptTask(
            questingData = ScriptQuestingData(quest = Quest.COOKS_ASSISTANT),
            behavior = ScriptBehavior.QUESTING
        )

        scriptTaskRunner?.configure(arrayOf(questTask))
        scriptTaskRunner?.run()
    }

    private fun prayerTest() {
        val prayerTask = ScriptTask(
            behavior = ScriptBehavior.PRAYER,
            stopCondition = ResourceGainedCondition(526, 56),
            prayerData = ScriptPrayerData(Inventory.DropPattern.ZIGZAG)
        )

        scriptTaskRunner?.configure(arrayOf(prayerTask))
        scriptTaskRunner?.run()
    }
}
