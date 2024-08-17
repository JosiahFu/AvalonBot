package bot.avalon.kord.message

import bot.avalon.data.GameState
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.interaction.ActionInteraction

suspend fun ActionInteraction.showRole(state: GameState.RoledState) {
    val role = state.players[user.id]!!
    val visible = state.getVisibleTo(role)

    respondEphemeral {
        content = """
            You are $role
            ${if (visible.isNotEmpty()) "${visible.map { kord.getUser(it)!!.mention }.joinToString(", ")} are ${role.visibleDescription}" else "'"}
        """.trimIndent()
    }
}
