package bot.avalon.kord.message

import bot.avalon.data.GameState
import bot.avalon.data.Role
import bot.avalon.data.STATES
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.Member
import dev.kord.core.entity.interaction.ActionInteraction
import dev.kord.rest.builder.message.embed

suspend fun ActionInteraction.tryShowRole() {
    val state = STATES[this.channelId]

    if (state == null || state !is GameState.RoledState) {
        respondEphemeral {
            content = "This channel does not currently have a game running"
        }
        return
    }

    if (user as Member !in state.players) {
        respondNotInGame()
        return
    }

    showRole(state)
}

suspend fun ActionInteraction.showRole(state: GameState.RoledState) {
    val user = user as Member
    val role = state.players[user]!!
    val visiblePlayers = state.getVisibleTo(user).map { it.mention }.joinFormatted("no one")

    val visibleMessage = when (role) {
        Role.MERLIN -> "The Minions of Mordred are $visiblePlayers.${if (Role.MORDRED in state.players.values) " Mordred remains unknown." else ""}"
        Role.MORDRED_MINION, Role.MORDRED, Role.ASSASSIN, Role.MORGANA -> "The other Minions of Mordred are $visiblePlayers.${if (Role.OBERON in state.players.values) " Oberon remains unknown." else ""}"
        Role.PERCIVAL -> if (Role.MORGANA in state.players.values) "One of $visiblePlayers is Merlin and the other is Morgana." else "$visiblePlayers is Merlin."
        else -> ""
    }

    val info = when (role) {
        in Role.mordredMinions -> "-# All Minions of Mordred know each other. Merlin knows all Minions of Mordred${if (Role.MORDRED in state.players.values) " except Mordred" else ""}.${if (Role.OBERON in state.players.values) " Oberon does not know the Minions of Mordred and the Minions do not know him." else ""}${if (role == Role.MORGANA) " Morgana appears to be Merlin to Percival." else ""}${if (role == Role.ASSASSIN) " Find out who Merlin is." else ""}"
        Role.OBERON -> "-# Oberon does not know the Minions of Mordred and the Minions do not know him."
        else -> ""
    }

    respondEphemeral {
        embed {
            title = "${role.emoji} You are **$role**!"

            description = """
                $visibleMessage
                $info
            """.trimIndent()
        }
    }
}
