package bot.avalon.kord

object Emojis {
    /** First symbol of most surrogate pairs */
    private const val S = "\uD83D"
    /** First symbol of some surrogate pairs */
    private const val S2 = "\uD83E"
    /** Zero Width Joiner */
    private const val J = "\u200D"
    private const val MAN = "$J\u2642\uFE0F"


    const val CHECK = "\u2705"
    const val X = "\u274C"

    const val THUMBS_UP = "$S\uDC4D"
    const val THUMBS_DOWN = "$S\uDC4E"

    const val TROPHY = "\uD83C\uDFC6"
    const val DAGGER = "$S\uDDE1"

    const val QUESTION = "\u2754"

    const val PERSON_POUTING = "$S\uDE4E"
    const val MAN_POUTING = "$PERSON_POUTING$MAN"
    const val MAGE = "$S2\uDDD9"
    const val MAN_MAGE = "$MAGE$MAN"
    const val MAN_SUPERVILLAIN = "$S2\uDDB9$MAN"
    const val KNIFE = "$S\uDD2A"
    const val MAN_ASTRONAUT = "$S\uDC68$J$S\uDE80"
    const val FAIRY = "$S2\uDDDA"

    /** Combining enclosing keycap */
    private const val N = "\uFE0F\u20E3"

    val NUMBER = listOf("0$N", "1$N", "2$N", "3$N", "4$N", "5$N")
}
