package bot.avalon.kord.message

import bot.avalon.data.GameState
import bot.avalon.data.Role
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.Member
import dev.kord.core.entity.interaction.ActionInteraction

suspend fun ActionInteraction.showRole(state: GameState.RoledState) {
    val user = user as Member
    val role = state.players[user]!!
    val visiblePlayers = state.getVisibleTo(user).map { it.mention }.joinFormatted("no one")

    val visibleMessage = when (role) {
        Role.MERLIN -> "The minions of Mordred are $visiblePlayers.${if (Role.MORDRED in state.players.values) " Mordred remains unknown." else ""}"
        Role.MORDRED_MINION, Role.MORDRED, Role.ASSASSIN, Role.MORGANA -> "The other minions of Mordred are $visiblePlayers.${if (Role.OBERON in state.players.values) " Oberon remains unknown." else ""}"
        Role.PERCIVAL -> if (Role.MORGANA in state.players.values) "One of $visiblePlayers is Merlin and the other is Morgana." else "$visiblePlayers is Merlin."
        else -> ""
    }

    respondEphemeral {
        content = """
            You are **$role**!
            $visibleMessage
        """.trimIndent()
    }
}
