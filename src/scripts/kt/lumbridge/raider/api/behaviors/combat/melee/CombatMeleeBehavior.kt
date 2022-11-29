package scripts.kt.lumbridge.raider.api.behaviors.combat.melee

import org.tribot.script.sdk.Combat
import org.tribot.script.sdk.frameworks.behaviortree.*
import scripts.kt.lumbridge.raider.api.ScriptTask
import scripts.kt.lumbridge.raider.api.behaviors.banking.initializeBankTask
import scripts.kt.lumbridge.raider.api.behaviors.combat.completeCombatAction

fun IParentNode.combatMeleeBehavior(scriptTask: ScriptTask?) = sequence {
    // ensure the bank task is initialized,
    // and the character has the correct items/equipment
    initializeBankTask(scriptTask)

    // ensure the attack style is selected
    selector {
        condition { scriptTask?.scriptCombatMeleeData?.attackStyle?.let { Combat.isAttackStyleSet(it) } }
        sequence {
            condition { scriptTask?.scriptCombatMeleeData?.attackStyle?.let { Combat.isAttackStyleAvailable(it) } }
            condition { scriptTask?.scriptCombatMeleeData?.attackStyle?.let { Combat.setAttackStyle(it) } }
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
