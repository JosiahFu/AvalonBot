@file:JvmName("Main")

package bot.avalon

import bot.avalon.data.loadGameState
import bot.avalon.kord.commands
import bot.avalon.kord.message.GameMessageType
import bot.avalon.kord.message.StartMessage
import bot.avalon.kord.message.tryShowRole
import dev.kord.core.Kord
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.GuildComponentInteractionCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import io.github.cdimascio.dotenv.Dotenv
import kotlinx.coroutines.launch
import sun.misc.Signal
import sun.misc.SignalHandler
import kotlin.system.exitProcess

suspend fun main() {
    val dotenv = Dotenv.load()

    with (Kord(dotenv["BOT_TOKEN"])) {
        for (command in commands) {
            createGlobalChatInputCommand(command.name, command.description, command.builder)
        }

        on<ChatInputCommandInteractionCreateEvent> {
            commands.find { it.name == interaction.invokedCommandName }!!.run { execute() }
        }

        on<GuildComponentInteractionCreateEvent> {
            if (interaction.componentId == StartMessage.VIEW_ROLE) {
                interaction.tryShowRole()
                return@on
            }

            GameMessageType.of(interaction.componentId)?.interact(interaction, interaction.componentId)
                ?: println("Could not find message for ${interaction.componentId}")
        }

        on<ReadyEvent> {
            println("Logged in!")
            loadGameState(this@with)

            val logout = SignalHandler { launch {
                println("\nLogging out...")
                logout()
                exitProcess(0)
            } }

            for (signal in listOf("INT", "TERM"))
                Signal.handle(Signal(signal), logout)

            editPresence {
                playing("/start")
            }
        }

        login {
            intents += Intent.GuildMessages + Intent.Guilds
        }
    }
}
