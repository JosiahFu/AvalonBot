package bot.avalon.kord.message

import bot.avalon.data.GameState
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.Kord
import dev.kord.core.entity.interaction.GuildComponentInteraction
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.message.actionRow

object StartMessage : GameMessageType<GameState.Start>() {
    private const val START_VIEW_ROLE = "start_view_role"
    const val VIEW_ROLE = "view_role"

    override suspend fun content(state: GameState.Start, kord: Kord) = """
        Game has began
        *Everyone must view their role*
        ${state.seenRoles.size}/${state.players.size} complete
    """.trimIndent()

    override suspend fun MessageBuilder.components(state: GameState.Start, kord: Kord, disable: Boolean) {
        actionRow {
            interactionButton(ButtonStyle.Primary, if (disable) VIEW_ROLE else START_VIEW_ROLE) {
                label = "View your role"
            }
        }
    }

    override suspend fun onInteract(
        interaction: GuildComponentInteraction,
        state: GameState.Start,
        componentId: String,
        setState: (GameState?) -> Unit
    ) {
        if (interaction.user !in state.players) {
            interaction.respondNotInGame()
            return
        }

        interaction.showRole(state)

        if (componentId == VIEW_ROLE) return

        state.seenRoles.add(interaction.user)

        interaction.updateContent(false)

        if (state.allSeen) {
            interaction.disableComponents()
            with(GameState.Discussion(state)) {
                setState(this)
                sendInChannel(interaction)
            }
        }
    }

    override val ids: Collection<String> = listOf(START_VIEW_ROLE)
}
