package bot.avalon.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

var STATE: GameState? = null

@Serializable
sealed interface GameState {
    var message: MessageId?

    @Serializable
    @SerialName("join")
    data class Join(
        val players: MutableSet<UserId> = mutableSetOf(),
        val optionalRoles: MutableSet<Role> = mutableSetOf(),
        override var message: MessageId? = null,
    ) : GameState {
        val canStart: Boolean
            get() = players.size in 5..10 || players.size in 1..5 // TODO Remove this it's for testing
    }

    // TODO change this name
    sealed interface RoledState : GameState {
        val players: Map<UserId, Role>

        fun getVisibleTo(role: Role): Set<UserId> = players.filterValues { it in role.visible }.keys
        fun getVisibleTo(player: UserId): List<UserId> = getVisibleTo(players[player]!!).filter { it != player }
    }

    @Serializable
    @SerialName("start")
    data class Start(
        override val players: Map<UserId, Role>,
        override var message: MessageId? = null,
        val seenRoles: MutableSet<UserId> = mutableSetOf()
    ): RoledState {
        constructor(prevState: Join) : this(assignRoles(prevState.players, prevState.optionalRoles))

        val allSeen: Boolean
            get() = players.keys.all { it in seenRoles }
    }

    @Serializable
    sealed interface PlayState : RoledState {
        val quests: List<Quest>
        val leader: UserId

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

        val currentQuest: Quest
            get() = quests.first { !it.isComplete }
    }

    @Serializable
    @SerialName("discussion")
    data class Discussion(
        override val players: Map<UserId, Role>,
        override val quests: List<Quest>,
        override var leader: UserId,
        val attempt: Int = 1,
        override var message: MessageId? = null,
    ) : PlayState {
        constructor(prevState: Start): this(prevState.players, getQuests(prevState.players.size), prevState.players.keys.random())
        constructor(prevState: PlayState): this(prevState.players, prevState.quests, prevState.nextLeader)

        companion object {
            fun fromFailed(prevState: Proposal) = Discussion(prevState.players, prevState.quests, prevState.nextLeader, prevState.attempt + 1)
        }
    }

    @Serializable
    @SerialName("proposal")
    data class Proposal(
        override val players: Map<UserId, Role>,
        override val quests: List<Quest>,
        override var leader: UserId,
        val attempt: Int,
        val proposedTeam: Set<UserId>,
        val votes: MutableMap<UserId, Boolean> = mutableMapOf(),
        override var message: MessageId? = null,
    ) : PlayState {
        constructor(
            prevState: Discussion,
            proposedTeam: Collection<UserId>,
        ): this(prevState.players, prevState.quests, prevState.leader, prevState.attempt, proposedTeam.toSet())

        val allVotesIn: Boolean
            get() = players.keys.all { it in votes }

        val votePassed: Boolean
            get() = 2 * votes.values.count { it } > players.size
            // The calculation works

        val outOfAttempts: Boolean
            get() = attempt >= 5

    }

    @Serializable
    @SerialName("quest")
    data class Questing(
        override val players: Map<UserId, Role>,
        override val quests: List<Quest>,
        override var leader: UserId,
        val team: Set<UserId>,
        val votes: MutableMap<UserId, Boolean> = mutableMapOf(),
        override var message: MessageId? = null,
    ) : PlayState {
        constructor(prevState: Proposal): this(prevState.players, prevState.quests, prevState.leader, prevState.proposedTeam)

        val allVotesIn: Boolean
            get() = team.all { it in votes }

        val questResult: Team
            get() = if (votes.values.count { !it } >= currentQuest.requiredFails) Team.EVIL else Team.GOOD
    }

    @Serializable
    @SerialName("assassin")
    data class Assassin(
        override val players: Map<UserId, Role>,
        override var message: MessageId? = null,
    ) : RoledState {
        constructor(prevState: PlayState): this(prevState.players)

        val assassin: UserId
            get() = players.firstNotNullOf { if (it.value == Role.ASSASSIN) it.key else null }

        fun getWinner(guess: UserId) = if (players[guess] == Role.MERLIN) Team.EVIL else Team.GOOD
    }

}

