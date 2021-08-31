package com.meloda.fast.screens.login

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.meloda.fast.api.UserConfig
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repo: AuthRepo
) : BaseViewModel() {

    fun login(
        login: String,
        password: String,
        twoFaCode: String? = null,
        captcha: Pair<String, String>? = null
    ) = viewModelScope.launch {
        makeJob(
            {
                repo.auth(
                    RequestAuthDirect(
                        grantType = VKConstants.Auth.GrantType.PASSWORD,
                        clientId = VKConstants.VK_APP_ID,
                        clientSecret = VKConstants.VK_SECRET,
                        username = login,
                        password = password,
                        scope = VKConstants.Auth.SCOPE,
                        twoFaForceSms = true,
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

                twoFaCode?.let { sendEvent(CodeSent) }

                if (VKUtil.isValidationRequired(it)) {
                    it.validationSid?.let { sid ->
                        sendEvent(ValidationRequired(validationSid = sid))

                        sendSms(sid)
                    }
                } else if (VKUtil.isCaptchaRequired(it)) {
                    it.captcha?.let { captcha ->
                        sendEvent(CaptchaRequired(captcha.first to captcha.second))
                    }
                }
            },
            onStart = { sendEvent(StartProgressEvent) },
            onEnd = { sendEvent(StopProgressEvent) }
        )
    }

    fun sendSms(validationSid: String) = viewModelScope.launch {
        makeJob({ repo.sendSms(validationSid) },
            onAnswer = { sendEvent(CodeSent) },
            onError = {},
            onStart = {},
            onEnd = {})
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

data class ValidationRequired(val validationSid: String) : VKEvent()
data class CaptchaRequired(val captcha: Pair<String, String>) : VKEvent()

object CodeSent : VKEvent()

data class SuccessAuth(val haveAuthorized: Boolean = true) : VKEvent()