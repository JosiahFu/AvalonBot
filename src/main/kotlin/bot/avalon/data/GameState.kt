package bot.avalon.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import bot.avalon.data.SerializableUser as User

@Serializable
sealed interface GameState {
    @Serializable
    @SerialName("start")
    class Start(
        val players: MutableSet<User> = mutableSetOf(),
        val roles: MutableMap<Role, Boolean> = mutableMapOf(),
    ) : GameState

    @Serializable
    sealed interface PlayState : GameState {
        val players: Map<User, Role>
        val quests: List<bot.avalon.data.Quest>

        fun getVisibleTo(role: Role): Set<User> = players.filterValues { it in role.visible }.keys
        fun getVisibleTo(player: User): Set<User> = getVisibleTo(players[player]!!)
    }

    @Serializable
    @SerialName("discussion")
    class Discussion(
        override val players: Map<User, Role>,
        override val quests: List<bot.avalon.data.Quest>,
        var fails: Int = 0,
    ) : PlayState {
        constructor(prevState: PlayState): this(prevState.players, prevState.quests)
    }

    @Serializable
    @SerialName("proposal")
    class Proposal(
        override val players: Map<User, Role>,
        override val quests: List<bot.avalon.data.Quest>,
        var fails: Int,
        val proposedTeam: Set<User>,
        val votes: MutableMap<User, Boolean> = mutableMapOf(),
    ) : PlayState {
        constructor(
            prevState: Discussion,
            proposedTeam: Set<User>,
        ): this(prevState.players, prevState.quests, prevState.fails, proposedTeam)
    }

    @Serializable
    @SerialName("quest")
    class Quest(
        override val players: Map<User, Role>,
        override val quests: List<bot.avalon.data.Quest>,
        val team: Set<User>,
        val votes: MutableMap<User, Boolean> = mutableMapOf(),
    ) : PlayState {
        constructor(prevState: Proposal): this(prevState.players, prevState.quests, prevState.proposedTeam)
    }

    @Serializable
    @SerialName("assassin")
    class Assassin(
        override val players: Map<User, Role>,
        override val quests: List<bot.avalon.data.Quest>
    ) : PlayState {
        constructor(prevState: PlayState): this(prevState.players, prevState.quests)
    }
}

