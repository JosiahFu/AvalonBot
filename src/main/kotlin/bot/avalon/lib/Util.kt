package bot.avalon.lib

import kotlin.random.Random

fun <T> MutableList<T>.removeRandom(): T = removeRandom(Random)
fun <T> MutableList<T>.removeRandom(random: Random): T {
    val index = random.nextInt(size)
    val element = this[index]
    this.removeAt(index)
    return element
}

inline fun <reified R: T, T : Any> Iterable<T>.firstOf(): R {
    for (e in this) {
        if (e is R) {
            return e
        }
    }
    throw NoSuchElementException("Collection is empty.")
}
