package bot.avalon

import bot.avalon.data.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun main() {
    val state: GameState = GameState.Discussion(assignRoles(setOf(
        SerializableUser("P1"),
        SerializableUser("P2"),
        SerializableUser("P3"),
        SerializableUser("P4"),
        SerializableUser("P5"),
        SerializableUser("P6"),
        SerializableUser("P7"),
    ), setOf(
        Role.MORDRED
    )), getQuests(players = 7))

    val json = Json.Default.encodeToString(state)
    println(json)
    Json.Default.decodeFromString<GameState>(json).also {
        println(it::class)
        when (it) {
            is GameState.Start -> {}
            is GameState.Discussion -> println(it.players)
            is GameState.Proposal -> {}
            is GameState.Quest -> {}
            is GameState.Assassin -> {}
        }
    }
}
