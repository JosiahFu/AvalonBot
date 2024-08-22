package bot.avalon.kord.message

import bot.avalon.data.GameState
import bot.avalon.data.Team
import bot.avalon.kord.Emojis
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.core.Kord
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.reply
import dev.kord.core.entity.interaction.GuildComponentInteraction
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.message.actionRow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object QuestingMessage : GameMessageType<GameState.Questing>() {
    private const val SUCCESS = "quest_success"
    private const val FAIL = "quest_fail"

    override suspend fun content(state: GameState.Questing, kord: Kord) = """
        |## Quest
        |${state.team.joinToString("\n") { it.mention }}
        |${state.votes.size}/${state.team.size} votes in
        """.trimMargin()

    override suspend fun MessageBuilder.components(state: GameState.Questing, kord: Kord, disable: Boolean) {
        actionRow {
            interactionButton(ButtonStyle.Success, SUCCESS) {
                label = "Success"
                emoji = DiscordPartialEmoji(name = Emojis.TROPHY)
                if (disable) disabled = true
            }

            interactionButton(ButtonStyle.Danger, FAIL) {
                label = "Fail"
                emoji = DiscordPartialEmoji(name = Emojis.DAGGER)
                if (disable) disabled = true
            }
        }
    }

    override suspend fun onInteract(
        interaction: GuildComponentInteraction,
        state: GameState.Questing,
        componentId: String,
        setState: (GameState?) -> Unit
    ) {
        if (interaction.user !in state.players) {
            interaction.respondNotInGame()
            return
        }

        if (interaction.user !in state.team) {
            interaction.respondEphemeral {
                content = "You are not on this quest's team"
            }
            return
        }

        when (componentId) {
            SUCCESS -> {
                state.votes[interaction.user] = true
                interaction.kord.launch {
                    interaction.respondEphemeral {
                        content = "Your vote: ${Emojis.TROPHY} SUCCESS"
                    }
                }
            }
            FAIL -> {
                if (state.players[interaction.user]!!.team != Team.EVIL) {
                    interaction.respondEphemeral {
                        content = "Only the evil team may fail quests"
                    }
                    return
                }

                state.votes[interaction.user] = false
                interaction.kord.launch {
                    interaction.respondEphemeral {
                        content = "Your vote: ${Emojis.DAGGER} FAIL"
                    }
                }
            }
        }

        interaction.kord.launch {
            interaction.updateContent(false)
        }

        val message = state.message

        if (!state.allVotesIn || message == null) return

        interaction.disableComponents()

        val resultMessage = message.reply {
            content = """
                ### Quest Results
                # ${List(state.votes.size) { Emojis.QUESTION }.joinToString(" ")}
            """.trimIndent()
        }

        val votes = state.votes.values.shuffled()

        for (revealIndex in votes.indices) {
            delay(2000)
            resultMessage.edit {
                content = """
                ### Quest Results
                # ${votes.mapIndexed { index, vote -> if (index > revealIndex) Emojis.QUESTION else if (vote) Emojis.TROPHY else Emojis.DAGGER }.joinToString(" ")}
            """.trimIndent()
            }
        }

        state.currentQuest.winner = state.questResult

        delay(2000)

        when (state.winner) {
            Team.GOOD -> GameState.Assassin(state)
            null -> GameState.Discussion(state)
            Team.EVIL -> {
                interaction.channel.sendWinMessage(Team.EVIL, state.players)
                setState(null)
                return
            }
        }.apply {
            setState(this)
            sendInChannel(interaction)
        }
    }

    override val ids: Collection<String> = listOf(SUCCESS, FAIL)
}
