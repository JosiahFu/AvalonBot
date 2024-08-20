package bot.avalon.kord

object Emojis {
    /** First symbol of most surrogate pairs */
    private const val S = "\uD83D"
    private const val S2 = "\uD83E"

    const val CHECK = "\u2705"
    const val X = "\u274C"

    const val THUMBS_UP = "$S\uDC4D"
    const val THUMBS_DOWN = "$S\uDC4E"

    const val TROPHY = "\uD83C\uDFC6"
    const val KNIFE = "$S\uDDE1"

    const val QUESTION = "\u2754"

    const val MAGE = "$S2\uDDD9"
    const val MAN_MAGE = "$MAGE\u200D\u2642\uFE0F"

    /** Combining enclosing keycap */
    private const val N = "\uFE0F\u20E3"

    val NUMBER = listOf("0$N", "1$N", "2$N", "3$N", "4$N", "5$N")
}
