package bot.avalon.kord.message

import bot.avalon.data.GameState
import bot.avalon.data.Team
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.entity.interaction.SelectMenuInteraction
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.message.actionRow
import kotlinx.coroutines.delay

object AssassinMessage : GameMessageType<GameState.Assassin>() {
    private const val ASSASSIN_TARGET = "assassin_target"

    override suspend fun content(state: GameState.Assassin, kord: Kord): String = """
        |## GOOD succeeds
        |
        |### EVIL Roles
        |${state.players.filterValues { it.team == Team.EVIL }.map { (user, role) -> "${kord.getUser(user)!!.mention}: $role" }.joinToString("\n")}
        |### Assassin
        |${kord.getUser(state.assassin)!!.mention}
    """.trimMargin()

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
        interaction: ComponentInteraction,
        state: GameState.Assassin,
        componentId: String,
        setState: (GameState?) -> Unit
    ) {
        if (interaction.user.id !in state.players) {
            interaction.respondNotInGame()
            return
        }

        if (interaction.user.id != state.assassin) {
            interaction.respondEphemeral {
                content = "You are not the assassin"
            }
            return
        }

        val (target, targetUser) = (interaction as SelectMenuInteraction).resolvedObjects!!.users!!.iterator().next()

        if (state.players[target]?.team != Team.GOOD) {
            interaction.respondEphemeral {
                content = "You must select a player on the GOOD team"
            }
            return
        }

        interaction.disableComponents()

        val message = interaction.respondPublic {
            content = """
                ### Assassin Target
                ${targetUser.mention}
            """.trimIndent()
        }

        delay(5000)

        val winner = state.getWinner(target)

        delay(2000)

        message.edit {
            content = """
                ### Assassin Target
                ${targetUser.mention} was${if (winner == Team.GOOD) " not" else ""} Merlin
            """.trimIndent()
        }

        interaction.channel.sendWinMessage(winner, state.players)

        setState(null)
    }

    override val ids: Collection<String> = listOf(ASSASSIN_TARGET)
}
