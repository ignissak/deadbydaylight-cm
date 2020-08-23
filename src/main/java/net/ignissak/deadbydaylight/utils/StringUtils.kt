package net.ignissak.deadbydaylight.utils

import net.md_5.bungee.api.ChatColor

object StringUtils {
    /**
     * Returns centered string.
     * @param message String to be centered.
     * @return Centered message.
     */
    fun getCenteredMessage(message: String?): String? {
        var message = message
        if (message == null || message == "") return null
        message = ChatColor.translateAlternateColorCodes('&', message)
        val CENTER_PX = 154
        var messagePxSize = 0
        var previousCode = false
        var isBold = false
        for (c in message.toCharArray()) {
            if (c == '§') {
                previousCode = true
            } else if (previousCode) {
                previousCode = false
                isBold = c == 'l' || c == 'L'
            } else {
                val dFI: DefaultFontInfo = DefaultFontInfo.Companion.getDefaultFontInfo(c)
                messagePxSize += if (isBold) dFI.boldLength else dFI.length
                messagePxSize++
            }
        }
        val halvedMessageSize = messagePxSize / 2
        val toCompensate = CENTER_PX - halvedMessageSize
        val spaceLength = DefaultFontInfo.SPACE.length + 1
        var compensated = 0
        val sb = StringBuilder()
        while (compensated < toCompensate) {
            sb.append(" ")
            compensated += spaceLength
        }
        return sb.toString() + message
    }
}