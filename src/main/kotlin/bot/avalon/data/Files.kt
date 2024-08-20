package bot.avalon.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

val GAME_STATES_FILE = File("state.json")

fun loadGameStates(): GameStates {
    return Json.Default.decodeFromString<GameStates>(GAME_STATES_FILE.readText())
}

fun saveGameStates(states: GameStates = STATES) {
    GAME_STATES_FILE.writeText(Json.Default.encodeToString(states))
}
