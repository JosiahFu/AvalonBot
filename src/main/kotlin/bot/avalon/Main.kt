@file:JvmName("Main")

package bot.avalon

import bot.avalon.kord.message.InteractiveMessage
import bot.avalon.kord.StartMessage
import bot.avalon.kord.commands
import dev.kord.core.Kord
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import io.github.cdimascio.dotenv.Dotenv

lateinit var kord: Kord

suspend fun main() {
    val dotenv = Dotenv.load()

    // TODO find better way to register all message types
    StartMessage

    Kord(dotenv["BOT_TOKEN"]).apply {
        for (command in commands) {
            createGlobalChatInputCommand(command.name, command.description, command.builder)
        }

        on<ChatInputCommandInteractionCreateEvent> {
            commands.find { it.name == interaction.invokedCommandName }!!.run { execute() }
        }

        on<ButtonInteractionCreateEvent> {
            InteractiveMessage.of(interaction.componentId).onInteract(interaction, interaction.componentId)
        }

        kord = this

        login {
            println("Logged in!")
            intents += Intent.GuildMessages + Intent.Guilds
        }
    }
}
