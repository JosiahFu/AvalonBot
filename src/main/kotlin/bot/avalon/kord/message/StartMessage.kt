package bot.avalon.kord.message

import bot.avalon.data.GameState
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.interaction.ActionInteraction
import dev.kord.core.entity.interaction.ComponentInteraction
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
        interaction: ComponentInteraction,
        state: GameState.Start,
        componentId: String,
        setState: (GameState?) -> Unit
    ) {
        if (interaction.user.id !in state.players) {
            interaction.respondNotInGame()
            return
        }

        interaction.showRole(state)

        if (componentId == VIEW_ROLE) return

        state.seenRoles.add(interaction.user.id)

        interaction.updateContent(false)

        if (state.allSeen) {
            interaction.disableComponents()
            with(GameState.Discussion(state)) {
                setState(this)
                sendInChannel(interaction)
            }
        }
    }

    override suspend fun interact(interaction: ComponentInteraction, componentId: String) {
        if (componentId == VIEW_ROLE) {
            showRole(interaction)
            return
        }
        super.interact(interaction, componentId)
    }

    override val ids: Collection<String> = listOf(START_VIEW_ROLE, VIEW_ROLE)

    suspend fun showRole(interaction: ActionInteraction) {
        val state = interaction.state as? GameState.RoledState

        if (state == null) {
            interaction.respondEphemeral {
                content = "There is no game running"
            }
            return
        }

        if (interaction.user.id !in state.players) {
            interaction.respondNotInGame()
            return
        }

        interaction.showRole(state)
    }
}
