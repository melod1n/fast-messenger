package com.meloda.fast.screens.login

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.meloda.fast.UserConfig
import com.meloda.fast.api.VKAuth
import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.VKException
import com.meloda.fast.api.VKUtil
import com.meloda.fast.api.network.repo.AuthRepo
import com.meloda.fast.api.network.request.RequestAuthDirect
import com.meloda.fast.base.viewmodel.BaseViewModel
import com.meloda.fast.base.viewmodel.StartProgressEvent
import com.meloda.fast.base.viewmodel.StopProgressEvent
import com.meloda.fast.base.viewmodel.VKEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repo: AuthRepo
) : BaseViewModel() {

    suspend fun login(
        login: String,
        password: String,
        twoFa: Boolean = false,
        twoFaCode: String? = null,
        captcha: Pair<String, String>? = null
    ) {
        makeJob(
            {
                repo.auth(
                    RequestAuthDirect(
                        grantType = VKAuth.GrantType.PASSWORD,
                        clientId = VKConstants.VK_APP_ID,
                        clientSecret = VKConstants.VK_SECRET,
                        username = login,
                        password = password,
                        scope = VKAuth.scope,
                        twoFaForceSms = twoFa,
                        twoFaCode = twoFaCode,
                        captchaSid = captcha?.first,
                        captchaKey = captcha?.second
                    ).map
                )
            },
            onAnswer = {
                // TODO: 8/31/2021 do something
                if (it.userId == null || it.accessToken == null) {
                    return@makeJob
                }

                UserConfig.userId = it.userId
                UserConfig.accessToken = it.accessToken

                sendEvent(SuccessAuth(haveAuthorized = true))
            },
            onError = {
                checkErrors(it)

                if (it !is VKException) return@makeJob

                if (VKUtil.isValidationRequired(it)) {
                    sendEvent(ValidationRequired(validationSid = it.validationSid))
                } else if (VKUtil.isCaptchaRequired(it) && it.captcha != null) {
                    sendEvent(CaptchaRequired(it.captcha!!.first to it.captcha!!.second))
                }
            },
            onStart = { sendEvent(StartProgressEvent) },
            onEnd = { sendEvent(StopProgressEvent) }
        )
    }

    @Suppress("MoveVariableDeclarationIntoWhen")
    private fun checkResponse(response: JSONObject) {
        viewModelScope.launch(Dispatchers.Default) {
            if (response.has("error")) {
                sendEvent(StopProgressEvent)

                val errorString = response.optString("error")
                val errorDescription = response.optString("error_description")

                // TODO: 7/27/2021 use this with localized resources
//               val errorType = response.optString("error_type")

                when (errorString) {
                    "need_validation" -> {
                        val redirectUrl = response.optString("redirect_uri")

                        tasksEventChannel.send(ValidationRequired(redirectUrl))
                    }
                    "need_captcha" -> {
                        val captchaImage = response.optString("captcha_img")
                        val captchaSid = response.optString("captcha_sid")

                        Log.d("CAPTCHA", "captchaImage: $captchaImage")

                        tasksEventChannel.send(ShowCaptchaDialog(captchaImage, captchaSid))
                    }
                    else -> {
                        tasksEventChannel.send(ShowError(errorDescription))
                    }
                }
            } else {
                delay(1500)
                sendEvent(StopProgressEvent)

                val userId = response.optInt("user_id", -1)
                val accessToken = response.optString("access_token")

                UserConfig.accessToken = accessToken
                UserConfig.userId = userId

                tasksEventChannel.send(SuccessAuth())
            }
        }
    }

    suspend fun getValidatedData(bundle: Bundle) {
        val accessToken = bundle.getString("token") ?: ""
        val userId = bundle.getInt("userId")

        UserConfig.accessToken = accessToken
        UserConfig.userId = userId

        tasksEventChannel.send(SuccessAuth())
    }

}

data class ShowError(val errorDescription: String) : VKEvent()
data class ShowCaptchaDialog(val captchaImage: String, val captchaSid: String) : VKEvent()

data class ValidationRequired(
    val redirectUrl: String? = null,
    val validationSid: String? = null
) : VKEvent()

data class CaptchaRequired(val captcha: Pair<String, String>) : VKEvent()

data class SuccessAuth(val haveAuthorized: Boolean = true) : VKEvent()