package bot.avalon.kord.message

import bot.avalon.data.GameState
import bot.avalon.data.Team
import bot.avalon.data.getMemberBehavior
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.interaction.GuildComponentInteraction
import dev.kord.core.entity.interaction.SelectMenuInteraction
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.message.actionRow
import kotlinx.coroutines.delay

object AssassinMessage : GameMessageType<GameState.Assassin>() {
    private const val ASSASSIN_TARGET = "assassin_target"

    override suspend fun content(state: GameState.Assassin, kord: Kord): String = """
        ## GOOD succeeds
        
        ${state.assassin.mention} is the assassin
    """.trimIndent()

    override suspend fun MessageBuilder.components(state: GameState.Assassin, kord: Kord, disable: Boolean) {
        actionRow {
            userSelect(ASSASSIN_TARGET) {
                placeholder = "Assassin Target"
                allowedValues = 1..1
                if (disable) disabled = true
            }
        }
    }

    override suspend fun onInteract(
        interaction: GuildComponentInteraction,
        state: GameState.Assassin,
        componentId: String,
        setState: (GameState?) -> Unit
    ) {
        if (interaction.user !in state.players) {
            interaction.respondNotInGame()
            return
        }

        if (interaction.user != state.assassin) {
            interaction.respondEphemeral {
                content = "You are not the assassin"
            }
            return
        }

        val target = interaction.guild.getMemberBehavior((interaction as SelectMenuInteraction).resolvedObjects!!.users!!.values.first())

        if (state.players[target]?.team != Team.GOOD) {
            interaction.respondEphemeral {
                content = "You must select a player on the GOOD team"
            }
            return
        }

        interaction.disableComponents(defer = true)

        val message = interaction.channel.createMessage {
            content = """
                ### Assassin Target
                ${target.mention}
            """.trimIndent()
        }

        delay(5000)

        val winner = state.getWinner(target)

        delay(2000)

        message.edit {
            content = """
                ### Assassin Target
                ${target.mention} was${if (winner == Team.GOOD) " not" else ""} Merlin
            """.trimIndent()
        }

        delay(2000)

        interaction.channel.sendWinMessage(winner, state.players)

        setState(null)
    }

    override val ids: Collection<String> = listOf(ASSASSIN_TARGET)
}
