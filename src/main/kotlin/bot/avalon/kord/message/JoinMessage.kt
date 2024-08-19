package bot.avalon.kord.message

import bot.avalon.data.GameState
import bot.avalon.data.Role
import bot.avalon.kord.Emojis
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.core.Kord
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.message.actionRow
import kotlin.enums.enumEntries

object JoinMessage : GameMessageType<GameState.Join>() {
    private const val START = "start_game"
    private const val JOIN = "join"
    private const val CANCEL = "cancel_game"
    private const val LEAVE = "leave"

    override val ids: Collection<String> = listOf(START, JOIN, CANCEL, LEAVE) + enumEntries<Role>().map(Role::name)

    override suspend fun content(state: GameState.Join, kord: Kord): String {
        return """
            # Avalon
            Players joined:${if (state.players.isEmpty()) " None" else ""}
        """.trimIndent() + state.players.map { "\n${kord.getUser(it)?.mention}" }.joinToString("") + "\n"
    }

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
                                                                                // TODO testing case
                if (disable || (state.players.size !in 5..10 && state.players.size != 2)) disabled = true
            }

            interactionButton(ButtonStyle.Danger, CANCEL) {
                label = "Cancel"
                if (disable) disabled = true
            }
        }
    }

    override suspend fun onInteract(
        interaction: ComponentInteraction,
        state: GameState.Join,
        componentId: String,
        setState: (GameState?) -> Unit
    ) {

        when (componentId) {
            START -> {
                interaction.disableComponents()
                with (GameState.Start(state)) {
                    interaction.state = this
                    respondTo(interaction)
                }
            }
            CANCEL -> {
                interaction.disableComponents(defer = true)
                setState(null)
            }
            JOIN -> {
                if (state.players.add(interaction.user.id)) {
                    interaction.deferPublicMessageUpdate()
                    interaction.message.edit {
                        content = content(state, interaction.kord)
                        components(state, interaction.kord)
                    }
                } else
                    interaction.respondEphemeral { content = "Cannot join: You already joined this game" }
            }
            LEAVE -> {
                if (state.players.remove(interaction.user.id)) {
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
