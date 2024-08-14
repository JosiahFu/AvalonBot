package bot.avalon.kord.message

import bot.avalon.data.GameState
import bot.avalon.data.gameState
import dev.kord.core.entity.interaction.ActionInteraction
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.entity.interaction.SelectMenuInteraction
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.message.actionRow

object DiscussionMessage : InteractiveMessage() {
    val USER_SELECT = "user_select"

    override suspend fun content(interaction: ActionInteraction): String {
        val state = interaction.gameState as GameState.Discussion

        return """
            ## Discussion
            Requires ${state.currentQuest.size} questers
            ${if (state.currentQuest.requiredFails > 1) "Requires ${state.currentQuest.requiredFails}" else ""}
            ${interaction.kord.getUser((interaction.gameState as GameState.Discussion).leader)?.mention} is quest leader
        """.trimIndent()
    }

    override suspend fun MessageBuilder.components(interaction: ActionInteraction, disable: Boolean) {
        val state = interaction.gameState as GameState.Discussion

        actionRow {
            userSelect(USER_SELECT) {
                if (state.currentTeam.isEmpty()) placeholder = "Choose Members"
                allowedValues = state.currentQuest.size.let{ it..it }
                if (disable) disabled = true
            }
        }
    }

    override suspend fun onInteract(interaction: ComponentInteraction, componentId: String) {
//        val state = interaction.gameState as GameState.Discussion

        // TODO validate (everyone is in the game, the user actually did that)
        // Maybe don't respond if wrong user?

//        interaction.disableComponents()
        interaction.deferPublicMessageUpdate()
        (interaction as SelectMenuInteraction).resolvedObjects?.users
    }

    override val ids: Collection<String> = listOf(USER_SELECT)
}
