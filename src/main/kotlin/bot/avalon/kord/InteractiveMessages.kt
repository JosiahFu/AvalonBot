package bot.avalon.kord

import bot.avalon.data.GameState
import bot.avalon.data.Role
import bot.avalon.data.STATE
import bot.avalon.kord
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.interaction.ButtonInteraction
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.message.actionRow
import kotlin.enums.enumEntries

val messageTypes = mutableListOf<InteractiveMessage>()

abstract class InteractiveMessage {
    abstract suspend fun content(state: GameState?): String
    abstract suspend fun MessageBuilder.components(state: GameState?)

    suspend fun ButtonInteraction.updateContent(state: GameState?) {
        this.message.edit {
            content = content(state)
        }
    }

    suspend fun ButtonInteraction.updateComponents(state: GameState?) {
        this.message.edit {
            components(state)
        }
    }

    suspend fun ButtonInteraction.updateBoth(state: GameState?) {
        this.message.edit {
            content = content(state)
            components(state)
        }
    }

    open suspend fun onInteract(interaction: ButtonInteraction, componentId: String) {}

    abstract val ids: Collection<String>

    init {
        @Suppress("LeakingThis")
        messageTypes.add(this)
    }
}

suspend fun MessageBuilder.using(message: InteractiveMessage, state: GameState?) {
    with(message) {
        content = message.content(state)
        components(state)
    }
}

object StartMessage : InteractiveMessage() {
    const val START = "start"
    const val JOIN = "join"
    const val CANCEL = "cancel"
    const val LEAVE = "leave"

    override val ids: Collection<String> = listOf(START, JOIN, CANCEL, LEAVE) + enumEntries<Role>().map(Role::name)

    override suspend fun content(state: GameState?): String {
        return if (state is GameState.Start) """
            # Avalon
            Players joined:${if (state.players.isEmpty()) " None" else ""}
        """.trimIndent() + state.players.map { "\n${kord.getUser(it)?.mention}" }.joinToString("") + "\n"
        else """
            # Avalon
            Canceled
        """.trimIndent()
    }

    override suspend fun MessageBuilder.components(state: GameState?) {
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
        val state = STATE as GameState.Start

        when (componentId) {
            START -> {
                // TODO
                interaction.respondEphemeral { content = "nuh uh" }
            }
            CANCEL -> {
                STATE = null
                interaction.updateBoth(null)
                interaction.deferPublicMessageUpdate()
            }
            JOIN -> {
                if (state.players.add(interaction.user.id)) {
                    interaction.updateContent(state)
                    interaction.deferPublicMessageUpdate()
                } else
                    interaction.respondEphemeral { content = "Cannot join: You already joined this game" }
            }
            LEAVE -> {
                if (state.players.remove(interaction.user.id)) {
                    interaction.updateContent(state)
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
                interaction.updateComponents(state)
                interaction.deferPublicMessageUpdate()
            }
        }
    }

}
