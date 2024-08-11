package bot.avalon.data

import bot.avalon.lib.removeRandom
import kotlin.enums.enumEntries
import bot.avalon.data.SerializableUser as User

enum class Team { GOOD, EVIL }

enum class Role(val team: Team, val isOptional: Boolean = false) {
    ARTHUR_SERVANT(Team.GOOD),
    MERLIN(Team.GOOD),
    PERCIVAL(Team.GOOD, isOptional = true),
    MORDRED_MINION(Team.EVIL),
    MORDRED(Team.EVIL, isOptional = true),
    ASSASSIN(Team.EVIL),
    OBERON(Team.EVIL, isOptional = true),
    MORGANA(Team.EVIL, isOptional = true);

    lateinit var visible: Set<Role>
        private set

    val isDefault: Boolean
        get() = this == defaultGood || this == defaultEvil

    companion object {
        init {
            val evilTeam = enumEntries<Role>().filter { it.team == Team.EVIL }.toSet()
            val mordredMinions = evilTeam - OBERON

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
    5 -> TeamSizes(good = 3, evil = 2)
    6 -> TeamSizes(good = 4, evil = 2)
    7 -> TeamSizes(good = 4, evil = 3)
    8 -> TeamSizes(good = 5, evil = 3)
    9 -> TeamSizes(good = 6, evil = 3)
    10 -> TeamSizes(good = 6, evil = 4)
    else -> throw IllegalArgumentException("Number of players must be between 5 and 10")
}

fun getRoles(players: Int, enabled: Collection<Role>): List<Role> {
    val roles = enumEntries<Role>()
    val (good, evil) = getTeamSizes(players)
    val specialRoles = roles.filter { !it.isDefault && (!it.isOptional || it in enabled) }

    return specialRoles +
        List(good - specialRoles.count { it.team == Team.GOOD }) { Role.defaultGood } +
        List(evil - specialRoles.count { it.team == Team.EVIL }) { Role.defaultEvil }
}

fun assignRoles(players: Collection<User>, optionalsEnabled: Collection<Role>): Map<User, Role> {
    val remainingRoles = getRoles(players.size, optionalsEnabled).toMutableList()

    return players.associateWith { remainingRoles.removeRandom() }
}
