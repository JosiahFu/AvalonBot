package bot.avalon.data

import bot.avalon.kord.StartMessage
import bot.avalon.kord.message.DiscussionMessage
import dev.kord.core.entity.interaction.ActionInteraction
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import bot.avalon.data.Quest as QuestData

var STATE: GameState? = null

// TODO
var ActionInteraction.gameState: GameState? by ::STATE

@Serializable
sealed interface GameState {
    @Serializable
    @SerialName("start")
    data class Start(
        val players: MutableSet<UserId> = mutableSetOf(),
        val optionalRoles: MutableSet<Role> = mutableSetOf()
    ) : GameState

    @Serializable
    sealed interface PlayState : GameState {
        val players: Map<UserId, Role>
        val quests: List<QuestData>
        var leader: UserId

        fun getVisibleTo(role: Role): Set<UserId> = players.filterValues { it in role.visible }.keys
        fun getVisibleTo(player: UserId): Set<UserId> = getVisibleTo(players[player]!!)

        val winner: Team?
            get() = when {
                quests.count { it.winner == Team.GOOD } >= 3 -> Team.GOOD
                quests.count { it.winner == Team.EVIL } >= 3 -> Team.EVIL
                else -> null
            }

        val nextLeader: UserId
            get() = players.keys.toList().let {
                it[(it.indexOf(leader) + 1).mod(it.size)]
            }
    }

    @Serializable
    @SerialName("discussion")
    data class Discussion(
        override val players: Map<UserId, Role>,
        override val quests: List<QuestData>,
        override var leader: UserId,
        var fails: Int = 0,
    ) : PlayState {
        constructor(prevState: Start): this(assignRoles(prevState.players, prevState.optionalRoles), getQuests(prevState.players.size), prevState.players.random())
        constructor(prevState: PlayState): this(prevState.players, prevState.quests, prevState.nextLeader)

        override val winner: Team?
            get() = if (fails > 5) Team.EVIL else super.winner

        companion object {
            fun fromFailed(prevState: Proposal) = Discussion(prevState.players, prevState.quests, prevState.nextLeader, prevState.fails + 1)
        }
    }

    @Serializable
    @SerialName("proposal")
    data class Proposal(
        override val players: Map<UserId, Role>,
        override val quests: List<QuestData>,
        override var leader: UserId,
        var fails: Int,
        val proposedTeam: Collection<UserId>,
        val votes: MutableMap<UserId, Boolean> = mutableMapOf(),
    ) : PlayState {
        constructor(
            prevState: Discussion,
            proposedTeam: Collection<UserId>,
        ): this(prevState.players, prevState.quests, prevState.leader, prevState.fails, proposedTeam)
    }

    @Serializable
    @SerialName("quest")
    data class Quest(
        override val players: Map<UserId, Role>,
        override val quests: List<QuestData>,
        override var leader: UserId,
        val team: Collection<UserId>,
        val votes: MutableMap<UserId, Boolean> = mutableMapOf(),
    ) : PlayState {
        constructor(prevState: Proposal): this(prevState.players, prevState.quests, prevState.leader, prevState.proposedTeam)
    }

    @Serializable
    @SerialName("assassin")
    data class Assassin(
        val players: Map<UserId, Role>,
    ) : GameState {
        constructor(prevState: PlayState): this(prevState.players)

        fun getWinner(guess: UserId) = if (players[guess] == Role.MERLIN) Team.EVIL else Team.GOOD
    }

    val message
        get() = when(this) {
            is Start -> StartMessage
            is Discussion -> DiscussionMessage
            else -> TODO()
        }
}

