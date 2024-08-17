package bot.avalon.kord

import bot.avalon.data.GameState
import bot.avalon.data.Quest
import bot.avalon.data.Role
import bot.avalon.data.STATE
import bot.avalon.kord.message.GameMessageType
import bot.avalon.kord.message.respondTo
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.message.actionRow
import kotlin.enums.enumEntries

object StartMessage : GameMessageType<GameState.Start>() {
    const val START = "start"
    const val JOIN = "join"
    const val CANCEL = "cancel"
    const val LEAVE = "leave"

    override val ids: Collection<String> = listOf(START, JOIN, CANCEL, LEAVE) + enumEntries<Role>().map(Role::name)

    override suspend fun content(state: GameState.Start, kord: Kord): String {
        return """
            # Avalon
            Players joined:${if (state.players.isEmpty()) " None" else ""}
        """.trimIndent() + state.players.map { "\n${kord.getUser(it)?.mention}" }.joinToString("") + "\n"
    }

    override suspend fun MessageBuilder.components(state: GameState.Start, kord: Kord, disable: Boolean) {

        actionRow {
            for (role in enumEntries<Role>()) if (role.isOptional) {
                val enabled = role in state.optionalRoles

                interactionButton(if (enabled) ButtonStyle.Success else ButtonStyle.Secondary, role.name) {
                    label = role.toString()
                    emoji = DiscordPartialEmoji(name = if (enabled) "\u2705" else "\u274C")
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
                if (disable) disabled = true
            }

            interactionButton(ButtonStyle.Danger, CANCEL) {
                label = "Cancel"
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

        when (componentId) {
            START -> {
                interaction.disableComponents()
                with (GameState.Discussion(state.players.associateWith { Role.ARTHUR_SERVANT }, listOf(Quest(2)), state.players.random())) {
//                with (GameState.Discussion(state)) { // TODO
                    interaction.state = this
                    respondTo(interaction)
                }
            }
            CANCEL -> {
                STATE = null
                interaction.updateAll()
                interaction.deferPublicMessageUpdate()
            }
            JOIN -> {
                if (state.players.add(interaction.user.id)) {
                    interaction.updateContent()
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
