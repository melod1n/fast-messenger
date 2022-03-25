package com.meloda.fast.screens.login

import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.VKException
import com.meloda.fast.api.network.auth.AuthDataSource
import com.meloda.fast.api.network.auth.AuthDirectRequest
import com.meloda.fast.base.viewmodel.BaseViewModel
import com.meloda.fast.base.viewmodel.ErrorEvent
import com.meloda.fast.base.viewmodel.VkEvent
import com.meloda.fast.common.Screens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val dataSource: AuthDataSource,
    private val router: Router
) : BaseViewModel() {

    companion object {
        private const val TAG = "LoginViewModel"
    }

    fun login(
        login: String,
        password: String,
        twoFaCode: String? = null,
        captcha: Pair<String, String>? = null
    ) = viewModelScope.launch {
        makeJob(
            {
                dataSource.auth(
                    AuthDirectRequest(
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

                // TODO: 19-Oct-21 do somewhen
//                makeJob({
//                    dataSource.authWithApp(
//                        AuthWithAppRequest(
//                            accessToken = it.accessToken
//                        )
//                    )
//                }, onAnswer = { kindaAnswer ->
//                    println("$TAG: AppAuthResponse: $kindaAnswer")
//                }
//                )


            },
            onError = {
                if (it !is VKException) {
                    onError(it)
                    return@makeJob
                }

                // TODO: 9/27/2021 use `delay` parameter
                twoFaCode?.let { sendEvent(CodeSent) }
            }
        )
    }

    fun sendSms(validationSid: String) = viewModelScope.launch {
        makeJob({ dataSource.sendSms(validationSid) },
            onAnswer = { sendEvent(CodeSent) }
        )
    }

    fun openPrimaryScreen() {
        router.navigateTo(Screens.Conversations())
    }

}

object CodeSent : VkEvent()

data class SuccessAuth(
    val haveAuthorized: Boolean = true
) : VkEvent()