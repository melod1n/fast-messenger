package dev.meloda.fast.model.api.domain

internal fun String.sized(size: Int): String {
    if (isBlank()) return ""
    val sep = if (contains("?")) "&" else "?"
    return "$this${sep}cs=${size}x0"
}

internal fun resolvePhotoUrl(
    photoBase: String?,
    size: Int,
    preferred: String?,
    vararg fallbacks: String?
): String? {
    if (!photoBase.isNullOrBlank()) {
        return photoBase.sized(size)
    }
    return listOfNotNull(preferred, *fallbacks).firstOrNull { it.isNotBlank() }
}
