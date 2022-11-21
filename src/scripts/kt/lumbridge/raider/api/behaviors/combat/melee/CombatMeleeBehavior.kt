package scripts.kt.lumbridge.raider.api.behaviors.combat.melee

import org.tribot.script.sdk.Combat
import org.tribot.script.sdk.frameworks.behaviortree.IParentNode
import org.tribot.script.sdk.frameworks.behaviortree.condition
import org.tribot.script.sdk.frameworks.behaviortree.selector
import org.tribot.script.sdk.frameworks.behaviortree.sequence
import scripts.kt.lumbridge.raider.api.Behavior
import scripts.kt.lumbridge.raider.api.ScriptTask
import scripts.kt.lumbridge.raider.api.behaviors.banking.initializeBankTask
import scripts.kt.lumbridge.raider.api.behaviors.combat.completeCombatAction

fun IParentNode.combatMeleeBehavior(scriptTask: ScriptTask?) = sequence {
    // ensure sequence is melee combat
    condition { scriptTask?.behavior == Behavior.COMBAT_MELEE }

    // ensure the bank task is initialized,
    // and the character has the correct items/equipment
    initializeBankTask(scriptTask)

    // ensure the attack style is selected
    selector {
        condition { scriptTask?.combatMeleeData?.attackStyle?.let { Combat.isAttackStyleSet(it) } }
        sequence {
            condition { scriptTask?.combatMeleeData?.attackStyle?.let { Combat.isAttackStyleAvailable(it) } }
            condition { scriptTask?.combatMeleeData?.attackStyle?.let { Combat.setAttackStyle(it) } }
        }
    }

    // execute the complete combat action {
    //  walking,
    //  waiting,
    //  attacking,
    //  banking,
    //  looting,
    //  get hyper,
    // }
    completeCombatAction(scriptTask)
}
