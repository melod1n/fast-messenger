package dev.meloda.fast.common.model

private const val MODE_NIGHT_NO = 1
private const val MODE_NIGHT_YES = 2
private const val MODE_NIGHT_FOLLOW_SYSTEM = -1
private const val MODE_NIGHT_AUTO_BATTERY = 3

enum class DarkMode(val value: Int) {
    DISABLED(MODE_NIGHT_NO),
    FOLLOW_SYSTEM(MODE_NIGHT_FOLLOW_SYSTEM),
    AUTO_BATTERY(MODE_NIGHT_AUTO_BATTERY),
    ENABLED(MODE_NIGHT_YES);

    companion object {

        fun parse(value: Int): DarkMode = entries.firstOrNull { it.value == value }
            ?: throw IllegalArgumentException("Unknown dark mode with value: $value")
    }
}
