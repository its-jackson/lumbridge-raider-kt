package scripts.kt.lumbridge.raider.api.behaviors.combat.magic

import org.tribot.script.sdk.Combat
import org.tribot.script.sdk.frameworks.behaviortree.IParentNode
import org.tribot.script.sdk.frameworks.behaviortree.condition
import org.tribot.script.sdk.frameworks.behaviortree.selector
import org.tribot.script.sdk.frameworks.behaviortree.sequence
import scripts.kt.lumbridge.raider.api.Behavior
import scripts.kt.lumbridge.raider.api.ScriptTask
import scripts.kt.lumbridge.raider.api.behaviors.banking.initializeBankTask
import scripts.kt.lumbridge.raider.api.behaviors.combat.completeCombatAction

fun IParentNode.combatMagicBehavior(scriptTask: ScriptTask?) = sequence {
    // ensure sequence is combat magic
    condition { scriptTask?.behavior == Behavior.COMBAT_MAGIC }

    // ensure the bank task is initialized,
    // and the character has the correct items/equipment
    initializeBankTask(scriptTask)

    // ensure spell is auto selected
    selector {
        condition {
            scriptTask?.combatMagicData?.autoCastableSpell
                ?.let {
                    Combat.getAutocastSpell()
                        .map { spell -> spell == it }
                        .orElse(false)
                }
        }
        condition { scriptTask?.combatMagicData?.autoCastableSpell?.let { Combat.setAutocastSpell(it) } }
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