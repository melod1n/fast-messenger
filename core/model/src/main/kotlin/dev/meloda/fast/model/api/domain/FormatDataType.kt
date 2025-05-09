package dev.meloda.fast.model.api.domain

enum class FormatDataType {
    BOLD, ITALIC, UNDERLINE, URL;

    override fun toString(): String {
        return super.toString().lowercase()
    }

    companion object {
        fun parse(value: String): FormatDataType? =
            entries.firstOrNull { it.name.lowercase() == value }
    }
}
