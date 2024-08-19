package bot.avalon.kord.message

import bot.avalon.data.GameState
import bot.avalon.data.Team
import bot.avalon.kord.Emojis
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.message.actionRow

object QuestingMessage : GameMessageType<GameState.Questing>() {
    private const val SUCCESS = "quest_success"
    private const val FAIL = "quest_fail"

    override suspend fun content(state: GameState.Questing, kord: Kord) = """
        |## Quest
        |${state.team.map { kord.getUser(it)!!.mention }.joinToString("\n")}
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
                emoji = DiscordPartialEmoji(name = Emojis.KNIFE)
                if (disable) disabled = true
            }
        }
    }

    override suspend fun onInteract(
        interaction: ComponentInteraction,
        state: GameState.Questing,
        componentId: String,
        setState: (GameState?) -> Unit
    ) {
        if (interaction.user.id !in state.players) {
            interaction.respondNotInGame()
            return
        }

        if (interaction.user.id !in state.team) {
            interaction.respondEphemeral {
                content = "You are not on this quest's team"
            }
            return
        }

        when (componentId) {
            SUCCESS -> {
                interaction.respondEphemeral {
                    content = "Your vote: ${Emojis.TROPHY} SUCCESS"
                }
                state.votes[interaction.user.id] = true
            }
            FAIL -> {
                if (state.players[interaction.user.id]!!.team != Team.EVIL) {
                    interaction.respondEphemeral {
                        content = "Only the evil team may fail quests"
                    }
                    return
                }

                interaction.respondEphemeral {
                    content = "Your vote: ${Emojis.KNIFE} FAIL"
                }
                state.votes[interaction.user.id] = false
            }
        }

        interaction.updateContent(false)

        if (state.allVotesIn) {
            interaction.disableComponents()

            interaction.channel.createMessage {
                content = """
                    ### Quest Results
                    ${state.votes.values.shuffled().joinToString("  ") { if (it) Emojis.TROPHY else Emojis.KNIFE }}
                """.trimIndent()
            }

            state.currentQuest.winner = state.questResult

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
    }

    override val ids: Collection<String> = listOf(SUCCESS, FAIL)
}
