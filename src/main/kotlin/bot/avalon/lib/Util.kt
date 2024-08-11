package bot.avalon.lib

import kotlin.random.Random

fun <T> MutableList<T>.removeRandom(): T = removeRandom(Random)
fun <T> MutableList<T>.removeRandom(random: Random): T {
    val index = random.nextInt(size)
    val element = this[index]
    this.removeAt(index)
    return element
}
