package bot.avalon.kord

import bot.avalon.data.GameState
import bot.avalon.data.STATES
import bot.avalon.kord.message.StatefulMessageType.Companion.disableComponents
import bot.avalon.kord.message.messageType
import bot.avalon.kord.message.sendInChannel
import bot.avalon.kord.message.tryShowRole
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import kotlinx.coroutines.launch

data class Command(
    val name: String,
    val description: String,
    val builder: ChatInputCreateBuilder.() -> Unit = {},
    val execute: suspend ChatInputCommandInteractionCreateEvent.() -> Unit,
)

val commands = listOf(
    Command(
        "start",
        "Start a game of Avalon",
    ) {
        if (interaction.channelId in STATES) {
            interaction.respondEphemeral {
                content = "A game is running. You must cancel it first."
            }
            return@Command
        }

        println("Starting new game")

        interaction.kord.launch {
            interaction.deferEphemeralResponse().delete()
        }

        with (GameState.Join()) {
            STATES[interaction.channelId] = this
            sendInChannel(interaction)
        }
    },
    Command(
        "cancel",
        "Cancel the current game of Avalon",
    ) {
        val state = STATES[interaction.channelId]
        if (state == null) {
            interaction.respondEphemeral {
                content = "This channel does not currently have a game running"
            }
            return@Command
        }

        state.message?.let { message ->
            interaction.kord.launch {
                state.messageType.disableComponents(
                    message,
                    state,
                )
            }
        }

        STATES.remove(interaction.channelId)

        interaction.respondPublic {
            content = "Canceled the current game"
        }
    },
    Command(
        "resend",
        "Resend the message for the current phase",
    ) {
        val state = STATES[interaction.channelId]
        if (state == null) {
            interaction.respondEphemeral {
                content = "This channel does not currently have a game running"
            }
            return@Command
        }

        val message = state.message ?: return@Command

        interaction.kord.launch {
            state.messageType.disableComponents(message, state)
        }

        interaction.kord.launch {
            interaction.deferEphemeralResponse().delete()
        }

        state.sendInChannel(interaction)
    },
    Command(
        "viewrole",
        "Show your role and the secret information you know",
    ) {
        interaction.tryShowRole()
    }
)
