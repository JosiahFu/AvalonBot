package bot.avalon.data

import kotlinx.serialization.Serializable

@Serializable
data class Quest(
    val size: Int,
    val requiredFails: Int = 1,
    var winner: Team? = null,
) {
    val isComplete: Boolean
        get() = winner != null
}

private fun checkFourthQuest(index: Int, size: Int) = Quest(size, requiredFails = if (index == 3) 2 else 1)

fun getQuests(players: Int): List<Quest> = when(players) {
    1 -> listOf(1, 1, 1, 1, 1).map(::Quest)
    2, 3, 4 -> listOf(2, 2, 2, 2, 2).map(::Quest) // TODO Remove this, it's for testing
    5 -> listOf(2, 3, 2, 3, 3).map(::Quest)
    6 -> listOf(2, 3, 4, 3, 4).map(::Quest)
    7 -> listOf(2, 3, 3, 4, 4).mapIndexed(::checkFourthQuest)
    8, 9, 10 -> listOf(3, 4, 4, 5, 5).mapIndexed(::checkFourthQuest)
    else -> throw IllegalArgumentException("Number of players must be between 5 and 10")
}
