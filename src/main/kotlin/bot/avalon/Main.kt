package bot.avalon

import bot.avalon.data.Role
import bot.avalon.kord.commands
import bot.avalon.kord.enabledRole
import bot.avalon.kord.optionalRoleButtons
import dev.kord.core.Kord
import dev.kord.core.behavior.edit
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.rest.builder.message.actionRow
import io.github.cdimascio.dotenv.Dotenv

suspend fun main() {
    val dotenv = Dotenv.load()

    Kord(dotenv["BOT_TOKEN"]).apply {
        for (command in commands) {
            createGlobalChatInputCommand(command.name, command.description, command.builder)
        }

        on<ChatInputCommandInteractionCreateEvent> {
            commands.find { it.name == interaction.invokedCommandName }?.run { execute() }
        }

        on<ButtonInteractionCreateEvent> {
            val role = Role.valueOf(interaction.componentId)
            if (role in enabledRole) {
                enabledRole.remove(role)
            } else {
                enabledRole.add(role)
            }
            interaction.message.edit {
                actionRow {
                    optionalRoleButtons(enabledRole)
                }
            }
            interaction.deferPublicMessageUpdate()
        }

        login {
            println("Logged in!")
            intents += Intent.GuildMessages + Intent.Guilds
        }
    }
}
