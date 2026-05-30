package dev.meloda.fast.logger;

enum class FastLogLevel {
    VERBOSE,
    DEBUG,
    INFO,
    WARNING,
    ERROR,
    ASSERT;

    companion object {
        fun parse(value: Int): FastLogLevel {
            if (value !in 0..5) throw IllegalArgumentException("Unknown LogLevel value $value")
            return entries.first { it.ordinal == value }
        }
    }
}
