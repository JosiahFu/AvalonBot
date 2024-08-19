@file:JvmName("Main")

package bot.avalon

import bot.avalon.kord.commands
import bot.avalon.kord.message.GameMessageType
import dev.kord.core.Kord
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import io.github.cdimascio.dotenv.Dotenv

suspend fun main() {
    val dotenv = Dotenv.load()

    with (Kord(dotenv["BOT_TOKEN"])) {
        for (command in commands) {
            createGlobalChatInputCommand(command.name, command.description, command.builder)
        }

        on<ChatInputCommandInteractionCreateEvent> {
            commands.find { it.name == interaction.invokedCommandName }!!.run { execute() }
        }

        on<ComponentInteractionCreateEvent> {
            GameMessageType.of(interaction.componentId).interact(interaction, interaction.componentId)
        }

        on<ReadyEvent> {
            println("Logged in!")
        }

        login {
            intents += Intent.GuildMessages + Intent.Guilds
        }
    }
}
