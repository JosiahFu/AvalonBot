package bot.avalon

import bot.avalon.data.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import bot.avalon.data.SerializableUser as User

fun main() {
    val state: GameState = GameState.Discussion(assignRoles(setOf(
        User("P1"),
        User("P2"),
        User("P3"),
        User("P4"),
        User("P5"),
        User("P6"),
        User("P7"),
    ), setOf(
        Role.MORDRED
    )), getQuests(players = 7))

    val json = Json.Default.encodeToString(state)
    println(json)
    Json.Default.decodeFromString<GameState>(json).also {
        println(it)
        when (it) {
            is GameState.Start -> {}
            is GameState.Discussion -> println(it.players)
            is GameState.Proposal -> {}
            is GameState.Quest -> {}
            is GameState.Assassin -> {}
        }
    }
}
