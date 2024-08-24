package bot.avalon.kord.message

import bot.avalon.data.GameState
import bot.avalon.data.Role
import bot.avalon.kord.Emojis
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.core.Kord
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.interaction.GuildComponentInteraction
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.message.actionRow
import dev.kord.rest.builder.message.embed
import kotlinx.coroutines.delay

object JoinMessage : GameMessageType<GameState.Join>() {
    private const val START = "start_game"
    private const val JOIN = "join"
    private const val CANCEL = "cancel_game"
    private const val LEAVE = "leave"

    override val ids: Collection<String> = listOf(START, JOIN, CANCEL, LEAVE) + Role.entries.map(Role::name)

    override suspend fun MessageBuilder.embeds(state: GameState.Join, kord: Kord) {
        embed {
            title = "New Game"

            description = if (state.players.isEmpty()) "None joined yet" else state.players.joinToString("\n") { it.mention }
        }
    }

    override suspend fun MessageBuilder.components(state: GameState.Join, kord: Kord, disable: Boolean) {

        actionRow {
            for (role in Role.entries) if (role.isOptional) {
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
                if (!state.isRolesValid) {
                    interaction.respondPublic {
                        content = "Not enough players to fill all selected roles, disable some or add more players"
                    }
                    return
                }
                interaction.disableComponents(defer = true)
                with (GameState.Start(state)) {
                    interaction.state = this
                    sendInChannel(interaction)
                }
            }
            CANCEL -> {
                val message = state.message!!
                interaction.disableComponents(defer = true)
                setState(null)
                delay(5000)
                message.delete()
            }
            JOIN -> {
                if (state.players.add(interaction.user)) {
                    interaction.deferPublicMessageUpdate()
                    interaction.message.edit {
                        embeds(state, interaction.kord)
                        components(state, interaction.kord)
                    }
                } else
                    interaction.respondEphemeral { content = "Cannot join: You already joined this game" }
            }
            LEAVE -> {
                if (state.players.remove(interaction.user)) {
                    interaction.updateEmbeds()
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
