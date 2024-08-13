package bot.avalon.kord

import bot.avalon.data.GameState
import bot.avalon.data.Role
import bot.avalon.data.STATE
import bot.avalon.data.gameState
import bot.avalon.kord
import bot.avalon.kord.message.InteractiveMessage
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.interaction.ActionInteraction
import dev.kord.core.entity.interaction.ButtonInteraction
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

    override suspend fun MessageBuilder.components(interaction: ActionInteraction) {
        val state = interaction.gameState
        val valid = state is GameState.Start

        if (valid) actionRow {
            for (role in enumEntries<Role>()) if (role.isOptional) {
                val enabled = state is GameState.Start && role in state.optionalRoles

                interactionButton(if (enabled) ButtonStyle.Success else ButtonStyle.Secondary, role.name) {
                    label = role.toString()
                    emoji = DiscordPartialEmoji(name = if (enabled) "\u2705" else "\u274C")
                }
            }
        }

        if (valid) actionRow {
            interactionButton(ButtonStyle.Primary, JOIN) {
                label = "Join"
            }

            interactionButton(ButtonStyle.Danger, LEAVE) {
                label = "Leave"
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

    override suspend fun onInteract(interaction: ButtonInteraction, componentId: String) {
        val state = interaction.gameState as GameState.Start

        when (componentId) {
            START -> {
                interaction.gameState = GameState.Discussion(state.players.associateWith { Role.ARTHUR_SERVANT }, listOf(), Snowflake(0))
//                interaction.gameState = GameState.Discussion(state) // TODO
                interaction.respondPublic {
                    using(interaction.gameState!!.message, interaction)
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
                    interaction.deferPublicMessageUpdate()
                } else
                    interaction.respondEphemeral { content = "Cannot join: You already joined this game" }
            }
            LEAVE -> {
                if (state.players.remove(interaction.user.id)) {
                    interaction.updateContent()
                    interaction.deferPublicMessageUpdate()
                } else
                    interaction.respondEphemeral { content = "Cannot leave: You are not in this game" }
            }
            else -> {
                val role = Role.valueOf(interaction.componentId)
                if (role in state.optionalRoles) {
                    state.optionalRoles.remove(role)
                } else {
                    state.optionalRoles.add(role)
                }
                interaction.updateComponents()
                interaction.deferPublicMessageUpdate()
            }
        }
    }

}
