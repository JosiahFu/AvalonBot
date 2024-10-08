package bot.avalon.kord.message

import bot.avalon.data.GameState
import bot.avalon.data.Team
import bot.avalon.kord.Emojis
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.reply
import dev.kord.core.entity.interaction.GuildComponentInteraction
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.message.actionRow
import dev.kord.rest.builder.message.embed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object ProposalMessage : GameMessageType<GameState.Proposal>() {
    private const val APPROVE = "approve_quest"
    private const val DENY = "deny_quest"

    override suspend fun MessageBuilder.embeds(state: GameState.Proposal, kord: Kord) {
        embed {
            title = "Proposed Quest"
            description = state.proposedTeam.joinToString("\n") { it.mention }
            field("Votes in") {
                "${state.votes.size}/${state.players.size}"
            }
        }
    }

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
        interaction: GuildComponentInteraction,
        state: GameState.Proposal,
        componentId: String,
        setState: (GameState?) -> Unit
    ) {
        if (interaction.user !in state.players) {
            interaction.respondNotInGame()
            return
        }

        when (componentId) {
            APPROVE -> {
                state.votes[interaction.user] = true
                interaction.kord.launch {
                    interaction.respondEphemeral {
                        content = "Your vote: ${Emojis.THUMBS_UP} APPROVE"
                    }
                }
            }
            DENY -> {
                state.votes[interaction.user] = false
                interaction.kord.launch {
                    interaction.respondEphemeral {
                        content = "Your vote: ${Emojis.THUMBS_DOWN} DENY"
                    }
                }
            }
        }

        interaction.updateEmbeds(false)

        val message = state.message

        if (!state.allVotesIn || message == null) return

        interaction.disableComponents()

        message.reply {
            embed {
                title = "Vote Results"
                description =
                    state.votes.map { (player, vote) -> "${if (vote) Emojis.THUMBS_UP else Emojis.THUMBS_DOWN} ${player.mention}" }.joinToString("\n")
            }
        }

        delay(2000)

        if (state.outOfAttempts) {
            interaction.channel.sendWinMessage(Team.EVIL, state.players)
            setState(null)
            return
        }

        with(
            if (state.votePassed) GameState.Questing(state) else GameState.Discussion.fromFailed(state)
        ) {
            setState(this)
            sendInChannel(interaction)
        }
    }

    override val ids: Collection<String> = listOf(APPROVE, DENY)
}
