package bot.avalon.lib

import kotlin.random.Random

fun <T> MutableList<T>.removeRandom(): T = removeRandom(Random)
fun <T> MutableList<T>.removeRandom(random: Random): T {
    val index = random.nextInt(size)
    val element = this[index]
    this.removeAt(index)
    return element
}

inline fun <reified T> Iterable<*>.firstOf(): T {
    for (e in this) {
        if (e is T) {
            return e
        }
    }
    throw NoSuchElementException("Collection is empty.")
}

class ObservableMap<K, V>(private val base: MutableMap<K, V>, private val onchange: () -> Unit): MutableMap<K, V> by base {
    override fun put(key: K, value: V): V? {
        return base.put(key, value).also { onchange() }
    }

    override fun putAll(from: Map<out K, V>) {
        return base.putAll(from).also { onchange() }
    }

    override fun remove(key: K): V? {
        return base.remove(key).also { onchange() }
    }

    override fun remove(key: K, value: V): Boolean {
        return base.remove(key, value).also { onchange() }
    }

    override fun clear() {
        base.clear()
        onchange()
    }


}
