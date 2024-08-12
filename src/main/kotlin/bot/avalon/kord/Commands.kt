package bot.avalon.kord

import bot.avalon.data.Role
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import dev.kord.rest.builder.message.actionRow

val enabledRole = mutableSetOf<Role>()

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
        {
//            integer("Potato", "cat") {
//                minValue = 0
//                maxValue = 10
//            }
        }
    ) {
        interaction.respondPublic { content = "pong" }
    },
    Command(
        "start",
        "Start a game of Avalon",
    ) {
        interaction.respondPublic {
            content = "Starting Avalon game"
            actionRow {
                optionalRoleButtons(enabledRole)
            }
        }
    }
)
