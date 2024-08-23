package bot.avalon.kord.message

import bot.avalon.data.GameState
import bot.avalon.data.STATES
import bot.avalon.data.saveGameStates
import dev.kord.core.behavior.edit
import dev.kord.core.entity.interaction.ActionInteraction
import dev.kord.core.entity.interaction.GuildComponentInteraction

abstract class GameMessageType<T: GameState> : StatefulMessageType<T, GameState?>() {
    override var ActionInteraction.state: GameState?
        get() = STATES[this.channelId]
        set(value) {
            if (value == null)
                STATES.remove(this.channelId)
            else
                STATES[this.channelId] = value
        }

    override suspend fun GuildComponentInteraction.disableComponents(defer: Boolean) {
        // Can't use super here because it's a member extension function
        // just gotta copy-paste the code...
        this.state!!.message = null
        if (defer) deferPublicMessageUpdate()
        this.message.edit {
            @Suppress("UNCHECKED_CAST")
            components(state as T, kord, disable = true)
        }
    }

    companion object {
        fun of(componentId: String) = messageTypes.find { componentId in it.ids }
    }
}

suspend fun GameState.respondTo(interaction: ActionInteraction) {
    messageType.respondTo(interaction)
    this.message = interaction.getOriginalInteractionResponse()
    saveGameStates()
}

suspend fun GameState.sendInChannel(interaction: ActionInteraction) {
    messageType.sendInChannel(interaction).also {
        this.message = it
    }
    saveGameStates()
}

val messageTypes: List<GameMessageType<out GameState>> = listOf(
    JoinMessage,
    StartMessage,
    DiscussionMessage,
    ProposalMessage,
    QuestingMessage,
    AssassinMessage,
)

val <T: GameState> T.messageType : GameMessageType<out GameState>
    get() = when(this) {
        is GameState.Join -> JoinMessage
        is GameState.Start -> StartMessage
        is GameState.Discussion -> DiscussionMessage
        is GameState.Proposal -> ProposalMessage
        is GameState.Questing -> QuestingMessage
        is GameState.Assassin -> AssassinMessage
        else -> throw IllegalArgumentException()
    }
