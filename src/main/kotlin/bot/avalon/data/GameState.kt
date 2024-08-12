package bot.avalon.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

var STATE: GameState? = null

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
        val quests: List<Quest>

        fun getVisibleTo(role: Role): Set<UserId> = players.filterValues { it in role.visible }.keys
        fun getVisibleTo(player: UserId): Set<UserId> = getVisibleTo(players[player]!!)

        val winner: Team?
            get() = when {
                quests.count { it.winner == Team.GOOD } >= 3 -> Team.GOOD
                quests.count { it.winner == Team.EVIL } >= 3 -> Team.EVIL
                else -> null
            }
    }

    @Serializable
    @SerialName("discussion")
    data class Discussion(
        override val players: Map<UserId, Role>,
        override val quests: List<Quest>,
        var fails: Int = 0,
    ) : PlayState {
        constructor(prevState: PlayState): this(prevState.players, prevState.quests)

        override val winner: Team?
            get() = if (fails > 5) Team.EVIL else super.winner

        companion object {
            fun fromFailed(prevState: Proposal) = Discussion(prevState.players, prevState.quests, prevState.fails + 1)
        }
    }

    @Serializable
    @SerialName("proposal")
    data class Proposal(
        override val players: Map<UserId, Role>,
        override val quests: List<Quest>,
        var fails: Int,
        val proposedTeam: Collection<UserId>,
        val votes: MutableMap<UserId, Boolean> = mutableMapOf(),
    ) : PlayState {
        constructor(
            prevState: Discussion,
            proposedTeam: Collection<UserId>,
        ): this(prevState.players, prevState.quests, prevState.fails, proposedTeam)
    }

    @Serializable
    @SerialName("quest")
    data class Quest(
        override val players: Map<UserId, Role>,
        override val quests: List<Quest>,
        val team: Collection<UserId>,
        val votes: MutableMap<UserId, Boolean> = mutableMapOf(),
    ) : PlayState {
        constructor(prevState: Proposal): this(prevState.players, prevState.quests, prevState.proposedTeam)
    }

    @Serializable
    @SerialName("assassin")
    data class Assassin(
        val players: Map<UserId, Role>,
    ) : GameState {
        constructor(prevState: PlayState): this(prevState.players)

        fun getWinner(guess: UserId) = if (players[guess] == Role.MERLIN) Team.EVIL else Team.GOOD
    }
}

