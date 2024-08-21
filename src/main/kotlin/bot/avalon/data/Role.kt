package bot.avalon.data

import bot.avalon.lib.removeRandom

enum class Team { GOOD, EVIL }

enum class Role(
    private val formatName: String,
    val team: Team,
    val isOptional: Boolean = false,
) {
    ARTHUR_SERVANT("Servant of Arthur", Team.GOOD),
    MERLIN("Merlin", Team.GOOD),
    PERCIVAL("Percival", Team.GOOD, isOptional = true),
    MORDRED_MINION("Minion of Mordred", Team.EVIL),
    MORDRED("Mordred", Team.EVIL, isOptional = true),
    ASSASSIN("Assassin", Team.EVIL),
    OBERON("Oberon", Team.EVIL, isOptional = true),
    MORGANA("Morgana", Team.EVIL, isOptional = true);

    lateinit var visible: Set<Role>
        private set

    val isDefault: Boolean
        get() = this == defaultGood || this == defaultEvil

    override fun toString(): String = formatName

    companion object {
        val evilTeam = Role.entries.filter { it.team == Team.EVIL }.toSet()
        val mordredMinions = evilTeam - OBERON

        init {
            ARTHUR_SERVANT.visible = setOf()
            MERLIN.visible = evilTeam - MORDRED
            PERCIVAL.visible = setOf(MERLIN, MORGANA)

            for (role in mordredMinions)
                role.visible = mordredMinions

            OBERON.visible = setOf()
        }

        val defaultGood = ARTHUR_SERVANT
        val defaultEvil = MORDRED_MINION
    }
}

data class TeamSizes(
    val good: Int,
    val evil: Int,
)

fun getTeamSizes(players: Int): TeamSizes = when (players) {
    2 -> TeamSizes(good = 1, evil = 1) // TODO this is temporary for testing
    3 -> TeamSizes(good = 2, evil = 1) // TODO this is temporary for testing
    4 -> TeamSizes(good = 3, evil = 1) // TODO this is temporary for testing
    5 -> TeamSizes(good = 3, evil = 2)
    6 -> TeamSizes(good = 4, evil = 2)
    7 -> TeamSizes(good = 4, evil = 3)
    8 -> TeamSizes(good = 5, evil = 3)
    9 -> TeamSizes(good = 6, evil = 3)
    10 -> TeamSizes(good = 6, evil = 4)
    else -> throw IllegalArgumentException("Number of players must be between 5 and 10")
}

fun getRoles(players: Int, enabled: Collection<Role>): List<Role> {
    // TODO Remove this it's for testing
    if (players == 1) {
        return listOf(Role.entries.random())
    }

    val (good, evil) = getTeamSizes(players)
    val specialRoles = Role.entries.filter { !it.isDefault && (!it.isOptional || it in enabled) }

    return specialRoles +
            List(good - specialRoles.count { it.team == Team.GOOD }) { Role.defaultGood } +
            List(evil - specialRoles.count { it.team == Team.EVIL }) { Role.defaultEvil }
}

fun assignRoles(players: Set<UserId>, optionalsEnabled: Collection<Role>): Map<UserId, Role> {
    val remainingRoles = getRoles(players.size, optionalsEnabled).toMutableList()

    return players.associateWith { remainingRoles.removeRandom() }
}
