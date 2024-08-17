package bot.avalon.kord.message

import bot.avalon.data.GameState
import dev.kord.core.Kord
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.entity.interaction.SelectMenuInteraction
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.message.actionRow

object DiscussionMessage : GameMessageType<GameState.Discussion>() {
    private const val USER_SELECT = "user_select"

    override suspend fun content(state: GameState.Discussion, kord: Kord): String {
        return """
            ## Discussion
            Requires ${state.currentQuest.size} questers
            ${if (state.currentQuest.requiredFails > 1) "Requires ${state.currentQuest.requiredFails}" else ""}
            ${kord.getUser(state.leader)?.mention} is quest leader
        """.trimIndent()
    }

    override suspend fun MessageBuilder.components(state: GameState.Discussion, kord: Kord, disable: Boolean) {
        actionRow {
            userSelect(USER_SELECT) {
                placeholder = "Choose Members"
                allowedValues = state.currentQuest.size.let{ it..it }
                if (disable) disabled = true
            }
        }
    }

    override suspend fun onInteract(
        interaction: ComponentInteraction,
        state: GameState.Discussion,
        componentId: String,
        setState: (GameState?) -> Unit
    ) {
        if (interaction.user.id != state.leader) return // Let interaction fail
        // TODO validate that all selected users are in the game
        interaction.disableComponents()
        with (GameState.Proposal(state, (interaction as SelectMenuInteraction).resolvedObjects?.users!!.keys)) {
            setState(this)
            respondTo(interaction)
        }
    }

    override val ids: Collection<String> = listOf(USER_SELECT)
}
