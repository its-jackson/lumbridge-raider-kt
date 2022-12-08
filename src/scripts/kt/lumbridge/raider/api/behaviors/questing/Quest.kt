package scripts.kt.lumbridge.raider.api.behaviors.questing

import org.tribot.script.sdk.ChatScreen
import org.tribot.script.sdk.Waiting
import org.tribot.script.sdk.query.Query
import org.tribot.script.sdk.types.WorldTile
import scripts.kotlin.api.canReach
import scripts.kotlin.api.walkTo

enum class Quest(
    x: Int, y: Int, z: Int,
    val questName: String,
    private val questHandInNpc: String,
    private val chatScreenDialog: Array<String>,
    private val chatScreenConfig: ChatScreen.Config
) {
    COOKS_ASSISTANT(
        3208, 3215, 0,
        "Cook's Assistant",
        "Cook",
        arrayOf(
            "You don't look very happy.", "What's wrong?",
            "Yes", "Actually, I know where to find this stuff.",
            "I'll get right on it."
        ),
        ChatScreen.Config.builder()
            .holdSpaceForContinue(true)
            .build()
    );

    private val position = WorldTile(x, y, z)

    fun isQuestNpcPositionNearby() = this@Quest.position.distance() < 10

    fun canReachQuestNpcPosition() = canReach(this@Quest.position)

    fun walkToQuestNpcPosition() = walkTo(this@Quest.position)

    fun handleQuestNpcDialog(): Boolean {
        if (!openQuestNpcDialog()) return false
        ChatScreen.setConfig(this@Quest.chatScreenConfig)
        return ChatScreen.handle(*this@Quest.chatScreenDialog)
    }

    private fun openQuestNpcDialog(): Boolean = Query.npcs()
        .isReachable
        .nameEquals(this@Quest.questHandInNpc)
        .findBestInteractable()
        .map {
            it.interact("Talk-to") &&
                    Waiting.waitUntil { ChatScreen.isOpen() }
        }
        .orElse(false)
}