package dev.meloda.fast.common.model

enum class NetworkLogLevel(val value: Int) {
    NONE(0),
    BASIC(1),
    HEADERS(2),
    BODY(3);

    companion object {
        fun parse(value: Int): NetworkLogLevel = entries.firstOrNull { it.value == value }
            ?: throw IllegalArgumentException("Unknown log level with value: $value")
    }
}
