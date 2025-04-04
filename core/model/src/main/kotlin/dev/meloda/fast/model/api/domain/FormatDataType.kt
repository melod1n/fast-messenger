package dev.meloda.fast.model.api.domain

enum class FormatDataType {
    BOLD, ITALIC, UNDERLINE, URL;

    companion object {
        fun parse(value: String): FormatDataType? =
            entries.firstOrNull { it.name.lowercase() == value }
    }
}
