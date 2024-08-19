package bot.avalon.kord

object Emojis {
    /** First symbol of most surrogate pairs */
    private const val S = "\uD83D"

    const val CHECK = "\u2705"
    const val X = "\u274C"

    const val THUMBS_UP = "$S\uDC4D"
    const val THUMBS_DOWN = "$S\uDC4E"

    const val DIAMOND = "$S\uDC8E"
    const val HOLE = "$S\uDD73"

    const val TROPHY = "\uD83C\uDFC6"
    const val KNIFE = "$S\uDDE1"

    const val QUESTION = "\u2754"

    /** Combining enclosing keycap */
    private const val N = "\uFE0F\u20E3"

    val NUMBER = listOf("0$N", "1$N", "2$N", "3$N", "4$N", "5$N")
}
