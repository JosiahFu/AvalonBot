package bot.avalon.kord.message

import bot.avalon.data.Role
import bot.avalon.data.Team
import bot.avalon.data.UserId
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.ActionInteractionBehavior
import dev.kord.core.behavior.interaction.respondEphemeral

suspend fun MessageChannelBehavior.sendWinMessage(team: Team, players: Map<UserId, Role>) {
    createMessage {
        content = """
            |## ${team.name} Team Wins!
            |
            |### Roles
            |${players.map { (user, role) -> "${kord.getUser(user)!!.mention}: $role" }.joinToString("\n")}
        """.trimMargin()
    }
}

suspend fun ActionInteractionBehavior.respondNotInGame() {
    respondEphemeral {
        content = "You are not part of this game"
    }
}
