package bot.avalon.kord.message

import bot.avalon.data.GameState
import dev.kord.core.Kord
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.entity.interaction.SelectMenuInteraction
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.message.actionRow

object DiscussionMessage : GameMessageType<GameState.Discussion>() {
    val USER_SELECT = "user_select"

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
//        val state = interaction.gameState as GameState.Discussion

        // TODO validate (everyone is in the game, the user actually did that)
        // Maybe don't respond if wrong user?

//        interaction.disableComponents()
        interaction.deferPublicMessageUpdate()
        (interaction as SelectMenuInteraction).resolvedObjects?.users
    }

    override val ids: Collection<String> = listOf(USER_SELECT)
}
