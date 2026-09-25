package dev.meloda.fast.auth.login

class QrCodeAuthParser {

    private val allowedHosts = setOf("qr.vk.com", "qr.vk.ru")

    fun extractWeb2AppCode(rawQrText: String): String? {
        val text = rawQrText.trim()
        if (text.isEmpty()) return null

        val uri = runCatching { java.net.URI(text) }.getOrNull() ?: return null
        if (!uri.scheme.equals("https", ignoreCase = true)) return null
        if (uri.host?.lowercase() !in allowedHosts) return null
        if (uri.path != "/w2a") return null

        return uri.rawQuery
            ?.split('&')
            ?.asSequence()
            ?.mapNotNull { parameter ->
                val separator = parameter.indexOf('=')
                if (separator < 0) return@mapNotNull null

                val name = decodeQueryPart(parameter.substring(0, separator))
                if (name != "q") return@mapNotNull null

                decodeQueryPart(parameter.substring(separator + 1)).takeIf(String::isNotBlank)
            }
            ?.firstOrNull()
    }

    fun isScannable(rawQrText: String): Boolean =
        !extractWeb2AppCode(rawQrText).isNullOrBlank()

    private fun decodeQueryPart(value: String): String =
        runCatching { java.net.URLDecoder.decode(value, Charsets.UTF_8.name()) }
            .getOrDefault(value)

    fun isInCenter(
        boxLeft: Int,
        boxTop: Int,
        boxRight: Int,
        boxBottom: Int,
        imageWidth: Int,
        imageHeight: Int,
        centerFraction: Float = 0.64f,
        minSizeFraction: Float = 0.08f
    ): Boolean {
        if (imageWidth <= 0 || imageHeight <= 0) return true
        val centerX = (boxLeft + boxRight) / 2f / imageWidth
        val centerY = (boxTop + boxBottom) / 2f / imageHeight
        val margin = (1f - centerFraction) / 2f
        if (centerX < margin || centerX > 1f - margin) return false
        if (centerY < margin || centerY > 1f - margin) return false

        val wFraction = (boxRight - boxLeft).toFloat() / imageWidth
        val hFraction = (boxBottom - boxTop).toFloat() / imageHeight
        if (wFraction < minSizeFraction && hFraction < minSizeFraction) return false

        return true
    }
}
