package bot.avalon.kord.message

import bot.avalon.data.GameState
import bot.avalon.data.gameState
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.entity.interaction.ActionInteraction
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.message.actionRow

object DiscussionMessage : InteractiveMessage() {
    val CHOOSE = "choose_quest"

    override suspend fun content(interaction: ActionInteraction) = """
        ## Discussion
        ${interaction.kord.getUser((interaction.gameState as GameState.Discussion).leader)?.mention} is quest leader
    """.trimIndent()

    override suspend fun MessageBuilder.components(interaction: ActionInteraction) {
        actionRow {
            interactionButton(ButtonStyle.Primary, CHOOSE) {
                label = "Propose Quest"
            }
        }
    }

    override val ids: Collection<String> = listOf(CHOOSE)
}
