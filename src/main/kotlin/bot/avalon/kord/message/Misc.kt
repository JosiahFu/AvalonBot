package bot.avalon.kord.message

import bot.avalon.data.Role
import bot.avalon.data.Team
import bot.avalon.data.UserId
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.ActionInteractionBehavior
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.rest.builder.message.embed

suspend fun MessageChannelBehavior.sendWinMessage(team: Team, players: Map<UserId, Role>) {
    createMessage {
        embed {
            title = "${team.name} Team Wins!"
            for ((user, role) in players) {
                field(user.fetchMember().effectiveName) { "${role.emoji} ${role.name}" }
            }
        }
    }
}

suspend fun ActionInteractionBehavior.respondNotInGame() {
    respondEphemeral {
        content = "You are not part of this game"
    }
}

fun List<String>.joinFormatted(default: String? = null) = when(this.size) {
    0 -> default ?: throw IllegalArgumentException("Cannot format join collection of 0 arguments")
    1 -> this[0]
    2 -> "${this[0]} and ${this[1]}"
    else -> "${this.subList(0, this.size - 1).joinToString(", ")} and ${this[this.size - 1]}"
}
