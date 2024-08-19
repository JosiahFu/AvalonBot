package bot.avalon.kord.message

import bot.avalon.data.GameState
import bot.avalon.kord.Emojis
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.message.actionRow

object ProposalMessage : GameMessageType<GameState.Proposal>() {
    private const val APPROVE = "approve_quest"
    private const val DENY = "deny_quest"

    override suspend fun content(state: GameState.Proposal, kord: Kord) = """
        |### Proposed quest:
        |${state.proposedTeam.map { kord.getUser(it)!!.mention }.joinToString("\n")}
        |${state.votes.size}/${state.players.size} votes in
        """.trimMargin()

    override suspend fun MessageBuilder.components(state: GameState.Proposal, kord: Kord, disable: Boolean) {
        actionRow {
            interactionButton(ButtonStyle.Success, APPROVE) {
                label = "Approve"
                emoji = DiscordPartialEmoji(name = Emojis.THUMBS_UP)
                if (disable) disabled = true
            }

            interactionButton(ButtonStyle.Danger, DENY) {
                label = "Deny"
                emoji = DiscordPartialEmoji(name = Emojis.THUMBS_DOWN)
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
        if (interaction.user.id !in state.players) {
            interaction.respondNotInGame()
            return
        }

        when (componentId) {
            APPROVE -> {
                state.votes[interaction.user.id] = true
                interaction.respondEphemeral {
                    content = "Your vote: ${Emojis.THUMBS_UP} APPROVE"
                }
            }
            DENY -> {
                state.votes[interaction.user.id] = false
                interaction.respondEphemeral {
                    content = "Your vote: ${Emojis.THUMBS_DOWN} DENY"
                }
            }
        }

        interaction.updateContent(false)

        if (state.allVotesIn) {
            interaction.disableComponents()
            interaction.channel.createMessage {
                content =
                    "### Vote Results\n" +
                    state.votes.map { (player, vote) -> "${if (vote) Emojis.THUMBS_UP else Emojis.THUMBS_DOWN} ${interaction.kord.getUser(player)!!.mention}" }.joinToString("\n")
            }

            with (
                if (state.votePassed) GameState.Questing(state) else GameState.Discussion.fromFailed(state)
            ) {
                setState(this)
                sendInChannel(interaction)
            }
        }
    }

    override val ids: Collection<String> = listOf(APPROVE, DENY)
}
