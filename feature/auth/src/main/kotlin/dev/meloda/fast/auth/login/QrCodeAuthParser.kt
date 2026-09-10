package dev.meloda.fast.auth.login

/**
 * Единая точка парсинга QR для входа.
 *
 * Сканер принимает только то, что здесь распознается:
 *  - QR с готовым access_token (кейт-подобный),
 *  - QR с сайта (web2app, qr.vk.com/w2a?q=...) — как в ориг. апк.
 * Мусорные QR (ссылки, wifi, визитки) молча игнорируются
 * и не закрывают экран с ошибкой.
 * Финальную проверку делает LoginViewModel.onQrCodeScanned.
 */
object QrCodeAuthParser {

    private val tokenParamRegex = Regex("""[?&#]?access_token=([a-zA-Z0-9._\-]+)""")
    private val jsonTokenRegex = Regex(""""access_token"\s*:\s*"([^"]+)"""")
    private val userIdParamRegex = Regex("""[?&#]?user_id=([0-9]+)""")
    private val jsonUserIdRegex = Regex(""""user_id"\s*:\s*([0-9]+)"""")

    // Хост офиц. QR с сайта, как в ориг. апк (b380 case 10).
    private val web2appHostRegex = Regex("qr[.]vk[.](com|ru)", RegexOption.IGNORE_CASE)

    fun decode(rawQrText: String): String {
        return try {
            java.net.URLDecoder.decode(rawQrText.trim(), "UTF-8")
        } catch (_: Exception) {
            rawQrText.trim()
        }
    }

    fun extractToken(rawQrText: String): String? {
        val qrText = decode(rawQrText)
        if (qrText.isEmpty()) return null

        tokenParamRegex.find(qrText)?.let { return it.groupValues[1] }
        jsonTokenRegex.find(qrText)?.let { return it.groupValues[1] }

        if (qrText.startsWith("vk1.")) {
            return qrText.split('&', ' ', '\n', '\r', '?', '#').firstOrNull()?.trim()
        }

        if (!qrText.contains(" ") && !qrText.contains("/") &&
            !qrText.contains("?") && !qrText.contains("#") && qrText.length >= 20
        ) {
            return qrText
        }

        return null
    }

    fun extractUserId(rawQrText: String): Long? {
        val qrText = decode(rawQrText)
        userIdParamRegex.find(qrText)?.let { return it.groupValues[1].toLongOrNull() }
        jsonUserIdRegex.find(qrText)?.let { return it.groupValues[1].toLongOrNull() }
        return null
    }

    /** QR подходит для входа — только тогда сканер его принимает и закрывается. */
    fun isAuthQr(rawQrText: String): Boolean = !extractToken(rawQrText).isNullOrBlank()

    /**
     * Код web2app (q) из QR с сайта, как в ориг. апк:
     * строгий вариант (s44) — хост qr.vk.com|qr.vk.ru, путь /w2a, параметр q;
     * мягкий (ModalAuthHostActivity, разлогиненный экран) — любой URI с непустым q.
     */
    fun extractWeb2AppCode(rawQrText: String): String? {
        val text = rawQrText.trim()
        if (text.isEmpty()) return null
        val uri = runCatching { android.net.Uri.parse(text) }.getOrNull() ?: return null
        val q = uri.getQueryParameter("q")
        if (q.isNullOrBlank()) return null
        val host = uri.host.orEmpty()
        if (host.isNotBlank() && web2appHostRegex.matches(host)) {
            return if (uri.path == "/w2a") q else null
        }
        // Мягкий вариант разлогиненного экрана ориг. апк: любой URI с q.
        return when (uri.scheme.orEmpty().lowercase()) {
            "http", "https", "vk", "vklink", "vkontakte" -> q
            else -> null
        }
    }

    /** Сканер принимает QR с токеном ИЛИ QR с сайта (web2app) — как ориг. апк. */
    fun isScannable(rawQrText: String): Boolean =
        isAuthQr(rawQrText) || !extractWeb2AppCode(rawQrText).isNullOrBlank()

    /**
     * QR в центре рамки? boundingBox от MLKit в координатах кадра.
     * Отсекает коды по краям/вдали — раньше рамка была декорацией
     * и сканилось все подряд.
     */
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
