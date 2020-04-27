package net.ignissak.deadbydaylight.utils

object Log {
    fun send(message: String) {
        println("[DeadByDaylight] $message")
    }

    @JvmStatic
    fun info(message: String) {
        send(LogColor.BLUE.colorize("❯") + " " + LogColor.BLUE_HIGH_INTENSITY.colorize(message))
    }

    fun warning(message: String) {
        send(LogColor.YELLOW.colorize("⚠") + " " + LogColor.YELLOW_HIGH_INTENSITY.colorize(message))
    }

    fun debug(message: String) {
        send(LogColor.CYAN.colorize("…") + " " + LogColor.CYAN_HIGH_INTENSITY.colorize(message))
    }

    @JvmStatic
    fun success(message: String) {
        send(LogColor.GREEN.colorize("✔") + " " + LogColor.GREEN.colorize(message))
    }

    @JvmStatic
    fun error(message: String) {
        send(LogColor.RED.colorize("✖") + " " + LogColor.RED_HIGH_INTENSITY.colorize(message))
    }

    @JvmStatic
    fun fatal(message: String) {
        send(LogColor.RED.colorize("✖ FATAL") + " " + LogColor.RED_HIGH_INTENSITY.colorize(message))
    }

    enum class LogColor(private val code: String) {
        RESET("\u001B[0m"), BLACK("\u001B[30m"), RED("\u001B[31m"), GREEN("\u001B[32m"), YELLOW("\u001B[33m"), BLUE("\u001B[34m"), PURPLE("\u001B[35m"), CYAN("\u001B[36m"), WHITE("\u001B[37m"), BLACK_HIGH_INTENSITY("\u001B[0;90m"), RED_HIGH_INTENSITY("\u001B[0;91m"), GREEN_HIGH_INTENSITY("\u001B[0;92m"), YELLOW_HIGH_INTENSITY("\u001B[0;93m"), BLUE_HIGH_INTENSITY("\u001B[0;94m"), PURPLE_HIGH_INTENSITY("\u001B[0;95m"), CYAN_HIGH_INTENSITY("\u001B[0;96m"), WHITE_HIGH_INTENSITY("\u001B[0;97m");

        fun colorize(message: String): String {
            return code + message + code
        }

    }
}