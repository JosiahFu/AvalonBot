package bot.avalon.kord.message

import bot.avalon.data.GameState
import bot.avalon.data.Team
import bot.avalon.data.getMemberBehavior
import bot.avalon.kord.Emojis
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.interaction.GuildComponentInteraction
import dev.kord.core.entity.interaction.SelectMenuInteraction
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.message.actionRow
import dev.kord.rest.builder.message.embed

object DiscussionMessage : GameMessageType<GameState.Discussion>() {
    private const val USER_SELECT = "user_select"

    override suspend fun MessageBuilder.embeds(state: GameState.Discussion, kord: Kord) {
        embed {
            title = "Discussion"

            description = """
                # ${state.quests.joinToString(" ") { if (it.isComplete) (if (it.winner == Team.GOOD) Emojis.TROPHY else Emojis.DAGGER) else Emojis.NUMBER[it.size] }}
            """.trimIndent()

            field("Questers", inline = true) { "${state.currentQuest.size}" }
            if (state.currentQuest.requiredFails > 1) field(
                "Fails required",
                inline = true
            ) { "${state.currentQuest.requiredFails}" }
            field("Attempt", inline = true) { "${state.attempt}/5" }
            field("Leader") { state.leader.mention }
        }
    }

    override suspend fun MessageBuilder.components(state: GameState.Discussion, kord: Kord, disable: Boolean) {
        actionRow {
            userSelect(USER_SELECT) {
                placeholder = "Choose Members"
                allowedValues = state.currentQuest.size.let{ it..it }
                if (disable) disabled = true
            }
        }
    }

    override suspend fun onInteract(
        interaction: GuildComponentInteraction,
        state: GameState.Discussion,
        componentId: String,
        setState: (GameState?) -> Unit
    ) {
        if (interaction.user !in state.players) {
            interaction.respondNotInGame()
            return
        }

        if (interaction.user != state.leader) {
            interaction.respondEphemeral {
                content = "You are not the leader of this quest"
            }
            return
        }

        val selectedUsers = (interaction as SelectMenuInteraction).resolvedObjects!!.users!!.values.map { interaction.guild.getMemberBehavior(it) }

        if (selectedUsers.any { it !in state.players }) {
            interaction.respondEphemeral {
                content = "Only select players in the game"
            }
            return
        }

        interaction.disableComponents(defer = true)
        with (GameState.Proposal(state, selectedUsers)) {
            setState(this)
            sendInChannel(interaction)
        }
    }

    override val ids: Collection<String> = listOf(USER_SELECT)
}
