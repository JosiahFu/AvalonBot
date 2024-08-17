package bot.avalon.kord.message

import bot.avalon.data.GameState
import bot.avalon.data.STATE
import bot.avalon.kord.StartMessage
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

val messageTypes: List<GameMessageType<out GameState>> = listOf(
    StartMessage,
    DiscussionMessage,
)

val <T: GameState> T.messageType : GameMessageType<out GameState>
    get() = when(this) {
        is GameState.Start -> StartMessage
        is GameState.Discussion -> DiscussionMessage
        else -> TODO()
    }
