package com.meloda.fast.screens.login

import androidx.lifecycle.viewModelScope
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.VKException
import com.meloda.fast.api.model.request.RequestAuthDirect
import com.meloda.fast.api.network.datasource.AuthDataSource
import com.meloda.fast.base.viewmodel.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val dataSource: AuthDataSource
) : BaseViewModel() {

    fun login(
        login: String,
        password: String,
        twoFaCode: String? = null,
        captcha: Pair<String, String>? = null
    ) = viewModelScope.launch {
        makeJob(
            {
                dataSource.auth(
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
                    )
                )
            },
            onAnswer = {
                if (it.userId == null || it.accessToken == null) {
                    sendEvent(ErrorEvent(unknownErrorDefaultText))
                    return@makeJob
                }

                UserConfig.userId = it.userId
                UserConfig.accessToken = it.accessToken

                sendEvent(SuccessAuth())
            },
            onError = {
                if (it !is VKException) return@makeJob

                twoFaCode?.let { sendEvent(CodeSent) }
            },
            onStart = { sendEvent(StartProgressEvent) },
            onEnd = { sendEvent(StopProgressEvent) }
        )
    }

    fun sendSms(validationSid: String) = viewModelScope.launch {
        makeJob({ dataSource.sendSms(validationSid) },
            onAnswer = { sendEvent(CodeSent) },
            onError = {},
            onStart = {},
            onEnd = {})
    }

}

data class ShowError(val errorDescription: String) : VKEvent()

object CodeSent : VKEvent()

data class SuccessAuth(val haveAuthorized: Boolean = true) : VKEvent()