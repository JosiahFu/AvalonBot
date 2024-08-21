package bot.avalon.kord.message

import bot.avalon.data.GameState
import bot.avalon.data.Role
import bot.avalon.kord.Emojis
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.core.Kord
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.interaction.GuildComponentInteraction
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.message.actionRow
import kotlin.enums.enumEntries

object JoinMessage : GameMessageType<GameState.Join>() {
    private const val START = "start_game"
    private const val JOIN = "join"
    private const val CANCEL = "cancel_game"
    private const val LEAVE = "leave"

    override val ids: Collection<String> = listOf(START, JOIN, CANCEL, LEAVE) + enumEntries<Role>().map(Role::name)

    override suspend fun content(state: GameState.Join, kord: Kord) = """
        |# New Game
        |Players joined:${if (state.players.isEmpty()) " None" else ""}
        |${state.players.map { it.mention }.joinToString("\n")}
    """.trimMargin()

    override suspend fun MessageBuilder.components(state: GameState.Join, kord: Kord, disable: Boolean) {

        actionRow {
            for (role in enumEntries<Role>()) if (role.isOptional) {
                val enabled = role in state.optionalRoles

                interactionButton(if (enabled) ButtonStyle.Success else ButtonStyle.Secondary, role.name) {
                    label = role.toString()
                    emoji = DiscordPartialEmoji(name = if (enabled) Emojis.CHECK else Emojis.X)
                    if (disable) disabled = true
                }
            }
        }

        actionRow {
            interactionButton(ButtonStyle.Primary, JOIN) {
                label = "Join"
                if (disable) disabled = true
            }

            interactionButton(ButtonStyle.Danger, LEAVE) {
                label = "Leave"
                if (disable) disabled = true
            }
        }

        actionRow {
            interactionButton(ButtonStyle.Primary, START) {
                label = "Start"
                if (disable || !state.canStart) disabled = true
            }

            interactionButton(ButtonStyle.Danger, CANCEL) {
                label = "Cancel"
                if (disable) disabled = true
            }
        }
    }

    override suspend fun onInteract(
        interaction: GuildComponentInteraction,
        state: GameState.Join,
        componentId: String,
        setState: (GameState?) -> Unit
    ) {

        when (componentId) {
            START -> {
                interaction.disableComponents(defer = true)
                with (GameState.Start(state)) {
                    interaction.state = this
                    sendInChannel(interaction)
                }
            }
            CANCEL -> {
                interaction.disableComponents(defer = true)
                setState(null)
            }
            JOIN -> {
                if (state.players.add(interaction.user)) {
                    interaction.deferPublicMessageUpdate()
                    interaction.message.edit {
                        content = content(state, interaction.kord)
                        components(state, interaction.kord)
                    }
                } else
                    interaction.respondEphemeral { content = "Cannot join: You already joined this game" }
            }
            LEAVE -> {
                if (state.players.remove(interaction.user)) {
                    interaction.updateContent()
                } else
                    interaction.respondEphemeral { content = "Cannot leave: You are not in this game" }
            }
            else -> { // is a role toggle
                val role = Role.valueOf(interaction.componentId)
                if (role in state.optionalRoles) {
                    state.optionalRoles.remove(role)
                } else {
                    state.optionalRoles.add(role)
                }
                interaction.updateComponents()
            }
        }
    }

}
