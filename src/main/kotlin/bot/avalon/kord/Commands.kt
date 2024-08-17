package bot.avalon.kord

import bot.avalon.data.GameState
import bot.avalon.data.STATE
import bot.avalon.kord.message.respondTo
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder

data class Command(
    val name: String,
    val description: String,
    val builder: ChatInputCreateBuilder.() -> Unit = {},
    val execute: suspend ChatInputCommandInteractionCreateEvent.() -> Unit,
)

val commands = listOf(
    Command(
        "ping",
        "ping the bot",
    ) {
        interaction.respondPublic { content = "pong" }
    },
    Command(
        "start",
        "Start a game of Avalon",
    ) {
        println("Starting new game")

        with (GameState.Join()) {
            STATE = this
            respondTo(interaction)
        }
    }
)
