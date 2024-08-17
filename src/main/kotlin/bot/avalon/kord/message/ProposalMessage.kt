package bot.avalon.kord.message

import bot.avalon.data.GameState
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.message.actionRow

object ProposalMessage : GameMessageType<GameState.Proposal>() {
    private const val APPROVE = "approve_quest"
    private const val DENY = "deny_quest"

    private const val THUMBS_UP_EMOJI = "\uD83D\uDC4D"
    private const val THUMBS_DOWN_EMOJI = "\uD83D\uDC4E"

    override suspend fun content(state: GameState.Proposal, kord: Kord): String =
        "### Proposed quest:\n" +
        state.proposedTeam.map { kord.getUser(it)!!.mention }.joinToString("\n")

    override suspend fun MessageBuilder.components(state: GameState.Proposal, kord: Kord, disable: Boolean) {
        actionRow {
            interactionButton(ButtonStyle.Success, APPROVE) {
                label = "Approve"
                emoji = DiscordPartialEmoji(name = THUMBS_UP_EMOJI)
                if (disable) disabled = true
            }

            interactionButton(ButtonStyle.Danger, DENY) {
                label = "Deny"
                emoji = DiscordPartialEmoji(name = THUMBS_DOWN_EMOJI)
                if (disable) disabled = true
            }
        }
    }

    override suspend fun onInteract(
        interaction: ComponentInteraction,
        state: GameState.Proposal,
        componentId: String,
        setState: (GameState?) -> Unit
    ) {
        interaction.deferPublicMessageUpdate()
        when (componentId) {
            APPROVE -> {
                state.votes[interaction.user.id] = true
            }
            DENY -> {
                state.votes[interaction.user.id] = false
            }
        }

        if (state.allVotesIn) {
            interaction.disableComponents()
            interaction.channel.createMessage {
                content =
                    "### Vote Results\n" +
                    state.votes.map { (player, vote) -> "${interaction.kord.getUser(player)!!.mention}: ${if (vote) THUMBS_UP_EMOJI else THUMBS_DOWN_EMOJI}" }.joinToString("\n")
            }
        }
    }

    override val ids: Collection<String> = listOf(APPROVE, DENY)
}
