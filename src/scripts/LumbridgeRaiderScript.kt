package scripts

import org.tribot.script.sdk.Inventory
import org.tribot.script.sdk.Log
import org.tribot.script.sdk.Login
import org.tribot.script.sdk.frameworks.behaviortree.*
import org.tribot.script.sdk.painting.Painting
import org.tribot.script.sdk.painting.template.basic.BasicPaintTemplate
import org.tribot.script.sdk.painting.template.basic.PaintRows
import org.tribot.script.sdk.painting.template.basic.PaintTextRow
import org.tribot.script.sdk.script.TribotScript
import org.tribot.script.sdk.script.TribotScriptManifest
import scripts.kt.lumbridge.raider.api.*
import java.awt.Font

var initBehaviourTree: IBehaviorNode? = null
var logicBehaviourTree: IBehaviorNode? = null

val paintTemplate = PaintTextRow.builder()
    .background(java.awt.Color(66, 66, 66, 180))
    .font(Font("Segoe UI", 0, 12))
    .noBorder()
    .build()

val mainPaint = BasicPaintTemplate.builder()
    .row(PaintRows.scriptName(paintTemplate.toBuilder()))
    .row(PaintRows.runtime(paintTemplate.toBuilder()))
    .row(paintTemplate.toBuilder().label("Behaviour").value { ScriptTaskRunner.activeTask?.behaviour?.characterBehaviour }.build())
    .row(paintTemplate.toBuilder().label("Stop").value { ScriptTaskRunner.activeTask?.stop }.build())
    .row(paintTemplate.toBuilder().label("Npc").value { ScriptTaskRunner.activeTask?.npc }.build())
    .row(paintTemplate.toBuilder().label("Remaining tasks").value { ScriptTaskRunner.remaining() }.build())
    .build()

@TribotScriptManifest(
    name = "LumbridgeRaider.kt",
    author = "Polymorphic",
    category = "Combat",
    description = "Local"
)
class LumbridgeRaiderKt : TribotScript {
    init {
        val scriptTasks = arrayOf(
            ScriptTask(
                npc = Npc.CHICKENS_LUMBRIDGE_WEST,
                behaviour = Behaviour.COMBAT_MELEE,
                stop = TimeStopCondition(minutes = 10, hours = 2, seconds = 45),
                cookThenBankDisposal = true,
                lootGroundItems = true
            )
        )
        ScriptTaskRunner.configure(scriptTasks)
    }

    init {
        Painting.addPaint(mainPaint::render)
    }

    // this behaviour tree ensures the user is logged in first.
    // then it will ensure the inventory is empty
    // before entering the main script logic.
    init {
        initBehaviourTree = behaviorTree {
            sequence {
                selector {
                    inverter { condition { !Login.isLoggedIn() } }
                    repeatUntil({ Login.isLoggedIn() }) { condition { Login.login() } }
                }
                selector {
                    inverter { condition { !Inventory.isEmpty() } }
                    repeatUntil({ Inventory.isEmpty() }) { walkToAndDepositInvBank() }
                }
            }
        }
    }

    // this behaviour tree is the main logic tree for the script.
    // it decides the behaviour of the character.
    init {
        logicBehaviourTree = behaviorTree {
            repeatUntil({ ScriptTaskRunner.isRunnerComplete() }) {
                sequence {
                    selector { genericBehaviour() }
                    selector { specificBehaviour() }
                }
            }
        }
    }

    override fun execute(args: String) {
        val initState = initBehaviourTree?.tick()
        Log.debug("Initialize ${initBehaviourTree?.name} ?: [$initState]")
        if (initState != BehaviorTreeStatus.SUCCESS) return

        val logicState = logicBehaviourTree?.tick()
        Log.debug("LumbridgeRaider.kt ${logicBehaviourTree?.name} ?: [$logicState]")
    }
}