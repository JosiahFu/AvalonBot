package bot.avalon.kord.message

import bot.avalon.data.GameState
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.message.actionRow

object StartMessage : GameMessageType<GameState.Start>() {
    private const val VIEW_ROLE = "start_view_role"

    override suspend fun content(state: GameState.Start, kord: Kord) = """
        Game has began
        *Everyone must view their role*
    """.trimIndent()

    override suspend fun MessageBuilder.components(state: GameState.Start, kord: Kord, disable: Boolean) {
        actionRow {
            interactionButton(ButtonStyle.Primary, VIEW_ROLE) {
                label = if (disable) "Use /viewrole to see your role again" else "View your role"
                if (disable) disabled = true
            }
        }
    }

    override suspend fun onInteract(
        interaction: ComponentInteraction,
        state: GameState.Start,
        componentId: String,
        setState: (GameState?) -> Unit
    ) {
        val role = state.players[interaction.user.id]!!
        val visible = state.getVisibleTo(role)

        interaction.respondEphemeral {
            content = """
                You are $role
                ${if (visible.isNotEmpty()) "${visible.map { interaction.kord.getUser(it)!!.mention }.joinToString(", ")} are ${role.visibleDescription}" else "'"}
            """.trimIndent()
        }

        state.seenRoles.add(interaction.user.id)

        if (state.allSeen) {
            interaction.disableComponents(false)
            with(GameState.Discussion(state)) {
                setState(this)
                sendInChannel(interaction)
            }
        }
    }

    override val ids: Collection<String> = listOf(VIEW_ROLE)
}
