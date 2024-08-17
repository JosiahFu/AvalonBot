package bot.avalon.kord.message

import bot.avalon.data.GameState
import bot.avalon.data.STATE
import dev.kord.core.entity.interaction.ActionInteraction

abstract class GameMessageType<T: GameState> : StatefulMessageType<T, GameState?>() {
    override var ActionInteraction.state by ::STATE // TODO

    companion object {
        fun of(componentId: String) = messageTypes.find { componentId in it.ids }!!
    }
}

suspend fun GameState.respondTo(interaction: ActionInteraction) {
    messageType.respondTo(interaction)
    this.message = interaction.getOriginalInteractionResponse().id
}

suspend fun GameState.sendInChannel(interaction: ActionInteraction) {
    messageType.sendInChannel(interaction).also {
        this.message = it.id
    }
}

val messageTypes: List<GameMessageType<out GameState>> = listOf(
    JoinMessage,
    StartMessage,
    DiscussionMessage,
    ProposalMessage,
)

val <T: GameState> T.messageType : GameMessageType<out GameState>
    get() = when(this) {
        is GameState.Join -> JoinMessage
        is GameState.Start -> StartMessage
        is GameState.Discussion -> DiscussionMessage
        is GameState.Proposal -> ProposalMessage
        else -> TODO()
    }
