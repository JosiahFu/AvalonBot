package bot.avalon.data

import bot.avalon.kord.Emojis
import bot.avalon.lib.removeRandom

enum class Team { GOOD, EVIL }

enum class Role(
    private val formatName: String,
    val emoji: String,
    val team: Team,
    val isOptional: Boolean = false,
) {
    ARTHUR_SERVANT("Servant of Arthur", Emojis.MAN_POUTING, Team.GOOD),
    MERLIN("Merlin", Emojis.MAN_MAGE, Team.GOOD),
    PERCIVAL("Percival", Emojis.MAN_ASTRONAUT, Team.GOOD, isOptional = true),
    MORDRED_MINION("Minion of Mordred", Emojis.PERSON_POUTING, Team.EVIL),
    MORDRED("Mordred", Emojis.MAN_SUPERVILLAIN, Team.EVIL, isOptional = true),
    ASSASSIN("Assassin", Emojis.KNIFE, Team.EVIL),
    OBERON("Oberon", Emojis.FAIRY, Team.EVIL, isOptional = true),
    MORGANA("Morgana", Emojis.MAGE, Team.EVIL, isOptional = true);

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
    5 -> TeamSizes(good = 3, evil = 2)
    6 -> TeamSizes(good = 4, evil = 2)
    7 -> TeamSizes(good = 4, evil = 3)
    8 -> TeamSizes(good = 5, evil = 3)
    9 -> TeamSizes(good = 6, evil = 3)
    10 -> TeamSizes(good = 6, evil = 4)
    else -> throw IllegalArgumentException("Number of players must be between 5 and 10")
}

fun isRolesValid(players: Int, enabled: Collection<Role>): Boolean {
    val (good, evil) = getTeamSizes(players)
    val specialRoles = Role.entries.filter { !it.isDefault && (it in enabled || !it.isOptional) }
    return good >= specialRoles.count { it.team == Team.GOOD } && evil >= specialRoles.count { it.team == Team.EVIL }
}

fun getRoles(players: Int, enabled: Collection<Role>): List<Role> {
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
