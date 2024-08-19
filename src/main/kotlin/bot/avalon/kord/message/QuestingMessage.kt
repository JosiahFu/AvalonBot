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

    override suspend fun content(state: GameState.Questing, kord: Kord): String {
        return "### Quest\n" + state.team.map { kord.getUser(it)!!.mention }.joinToString("\n")
    }

    override suspend fun MessageBuilder.components(state: GameState.Questing, kord: Kord, disable: Boolean) {
        actionRow {
            interactionButton(ButtonStyle.Success, SUCCESS) {
                label = "Success"
                emoji = DiscordPartialEmoji(name = Emojis.TROPHY)
            }

            interactionButton(ButtonStyle.Danger, FAIL) {
                label = "Fail"
                emoji = DiscordPartialEmoji(name = Emojis.KNIFE)
            }
        }
    }

    override suspend fun onInteract(
        interaction: ComponentInteraction,
        state: GameState.Questing,
        componentId: String,
        setState: (GameState?) -> Unit
    ) {
        when (componentId) {
            SUCCESS -> {
                interaction.deferPublicMessageUpdate()
                state.votes[interaction.user.id] = true
            }
            FAIL -> {
                if (state.players[interaction.user.id]!!.team != Team.EVIL) {
                    interaction.respondEphemeral {
                        content = "Only the evil team may fail quests"
                    }
                    return
                }

                interaction.deferPublicMessageUpdate()
                state.votes[interaction.user.id] = false
            }
        }

        if (state.allVotesIn) {
            interaction.channel.createMessage {
                content = """
                    ### Quest Results
                    ${state.votes.values.shuffled().joinToString("  ") { if (it) Emojis.TROPHY else Emojis.KNIFE }}
                """.trimIndent()
            }

            state.currentQuest.winner = state.questResult

            with (GameState.Discussion(state)) {
                setState(this)
                sendInChannel(interaction)
            }
        }
    }

    override val ids: Collection<String> = listOf(SUCCESS, FAIL)
}
