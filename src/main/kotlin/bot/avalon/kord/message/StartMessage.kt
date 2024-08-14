package bot.avalon.kord

import bot.avalon.data.*
import bot.avalon.kord
import bot.avalon.kord.message.InteractiveMessage
import bot.avalon.kord.message.respondTo
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.interaction.ActionInteraction
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.message.actionRow
import kotlin.enums.enumEntries

object StartMessage : InteractiveMessage() {
    const val START = "start"
    const val JOIN = "join"
    const val CANCEL = "cancel"
    const val LEAVE = "leave"

    override val ids: Collection<String> = listOf(START, JOIN, CANCEL, LEAVE) + enumEntries<Role>().map(Role::name)

    override suspend fun content(interaction: ActionInteraction): String {
        val state = interaction.gameState
        return if (state is GameState.Start) """
            # Avalon
            Players joined:${if (state.players.isEmpty()) " None" else ""}
        """.trimIndent() + state.players.map { "\n${kord.getUser(it)?.mention}" }.joinToString("") + "\n"
        else """
            # Avalon
            Canceled
        """.trimIndent()
    }

    override suspend fun MessageBuilder.components(interaction: ActionInteraction, disable: Boolean) {
        val state = interaction.gameState
        val valid = !disable && state is GameState.Start

        actionRow {
            for (role in enumEntries<Role>()) if (role.isOptional) {
                val enabled = state is GameState.Start && role in state.optionalRoles

                interactionButton(if (enabled) ButtonStyle.Success else ButtonStyle.Secondary, role.name) {
                    label = role.toString()
                    emoji = DiscordPartialEmoji(name = if (enabled) "\u2705" else "\u274C")
                    if (!valid) disabled = true
                }
            }
        }

        actionRow {
            interactionButton(ButtonStyle.Primary, JOIN) {
                label = "Join"
                if (!valid) disabled = true
            }

            interactionButton(ButtonStyle.Danger, LEAVE) {
                label = "Leave"
                if (!valid) disabled = true
            }
        }

        actionRow {
            interactionButton(ButtonStyle.Primary, START) {
                label = "Start"
                if (!valid) disabled = true
            }

            interactionButton(ButtonStyle.Danger, CANCEL) {
                label = "Cancel"
                if (!valid) disabled = true
            }
        }
    }

    override suspend fun onInteract(interaction: ComponentInteraction, componentId: String) {
        val state = interaction.gameState as GameState.Start

        when (componentId) {
            START -> {
                interaction.disableComponents()
                with (GameState.Discussion(state.players.associateWith { Role.ARTHUR_SERVANT }, listOf(Quest(2)), state.players.random())) {
//                with (GameState.Discussion(state)) { // TODO
                    interaction.gameState = this
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
