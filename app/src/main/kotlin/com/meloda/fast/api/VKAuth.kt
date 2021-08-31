package com.meloda.fast.api

import android.util.Log
import com.meloda.fast.BuildConfig
import com.meloda.fast.UserConfig
import java.net.URLEncoder

object VKAuth {

    private const val TAG = "VKM.VKAuth"

    object GrantType {
        const val PASSWORD = "password"
    }

    const val scope = "notify," +
            "friends," +
            "photos," +
            "audio," +
            "video," +
            "docs," +
            "status," +
            "notes," +
            "pages," +
            "wall," +
            "groups," +
            "messages," +
            "offline," +
            "notifications"

    private const val redirectUrl = "https://oauth.vk.com/blank.html"

    fun getDirectAuthUrl(
        login: String,
        password: String,
        twoFa: Boolean = false,
        twoFaCode: String = "",
        captcha: Pair<String, String>? = null
    ) = "https://oauth.vk.com/token?" +
            "grant_type=password&" +
            "client_id=${VKConstants.VK_APP_ID}&" +
            "client_secret=${VKConstants.VK_SECRET}&" +
            "username=$login&" +
            "password=$password&" +
            "scope=$scope&" +
            "2fa_supported=1&" +
            "force_sms=${if (twoFa) "1" else "0"}" +
            (if (twoFa) "code=$twoFaCode" else "") +
            (if (captcha == null) "" else "&captcha_sid=${captcha.first}&captcha_key=${captcha.second}") +
            "&v=${URLEncoder.encode(VKApi.API_VERSION, "utf-8")}"

    fun getSendSmsCodeUrl(sid: String) = "https://api.vk.com/method/auth.validatePhone?" +
            "sid=$sid&" +
            "&v=${URLEncoder.encode(VKApi.API_VERSION, "utf-8")}"

    fun getOAuthUrl(settings: String) = "https://oauth.vk.com/authorize?" +
            "client_id=${UserConfig.FAST_APP_ID}&" +
            "display=mobile&" +
            "scope=$settings&" +
            "redirect_uri=${
                URLEncoder.encode(
                    redirectUrl,
                    "utf-8"
                )
            }&" +
            "response_type=token&" +
            "v=${URLEncoder.encode(VKApi.API_VERSION, "utf-8")}"

    fun parseRedirectUrl(url: String): Pair<String, Int> {
        val accessToken = VKUtil.extractPattern(url, "access_token=(.*?)&") ?: ""
        val userId = VKUtil.extractPattern(url, "user_id=(\\d*)")?.toIntOrNull() ?: -1

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "access_token=$accessToken")
            Log.i(TAG, "user_id=$userId")
        }

        if (accessToken.isEmpty() || userId == -1) throw Exception(
            "Failed to parse redirect url: $url"
        )

        return accessToken to userId
    }
}