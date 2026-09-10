package dev.meloda.fast.model.api.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Ответ auth.getAuthCodeStatus. Как в ориг. апк (QrWebToApp):
 * status 2 = подтвержден, сразу лежат access_token + user_id.
 * status 0/1 = ожидание (продолжать поллить), 3/4 = ошибка/истек.
 */
@JsonClass(generateAdapter = true)
data class GetAuthCodeStatusResponse(
    @Json(name = "status") val status: Int?,
    @Json(name = "access_token") val accessToken: String?,
    @Json(name = "expires_in") val expiresIn: Int?,
    @Json(name = "user_id") val userId: Long?,
    @Json(name = "user_session") val userSession: List<UserSession>?
) {

    @JsonClass(generateAdapter = true)
    data class UserSession(
        @Json(name = "target_key") val targetKey: String,
        @Json(name = "token") val token: String
    )
}

/**
 * Ответ auth.setAuthCodeStatus. polling_delay/expires_in задают цикл опроса,
 * как в CheckSignInFeature ориг. апк.
 */
@JsonClass(generateAdapter = true)
data class SetAuthCodeStatusResponse(
    @Json(name = "status") val status: Int?,
    @Json(name = "expires_in") val expiresIn: Int?,
    @Json(name = "polling_delay") val pollingDelay: Int?,
    @Json(name = "domain") val domain: String?,
    @Json(name = "faq_url") val faqUrl: String?
)
