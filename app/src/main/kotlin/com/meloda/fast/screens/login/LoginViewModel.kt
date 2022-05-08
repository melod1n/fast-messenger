package com.meloda.fast.screens.login

import androidx.lifecycle.MutableLiveData
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
import com.meloda.fast.database.dao.AccountsDao
import com.meloda.fast.model.AppAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val dataSource: AuthDataSource,
    private val router: Router,
    private val accounts: AccountsDao
) : BaseViewModel() {

    var currentAccount: AppAccount? = null

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

                currentAccount = AppAccount(
                    userId = it.userId,
                    accessToken = it.accessToken,
                    fastToken = null
                ).also { account ->
                    accounts.insert(listOf(account))
                    UserConfig.currentUserId = account.userId
                    UserConfig.parse(account)
                }

                sendEvent(LoginSuccessAuth)

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
                twoFaCode?.let { sendEvent(LoginCodeSent) }
            }, onStart = null, onEnd = null
        )
    }

    fun sendSms(validationSid: String) = viewModelScope.launch {
        makeJob({ dataSource.sendSms(validationSid) },
            onAnswer = { sendEvent(LoginCodeSent) }
        )
    }

    fun openPrimaryScreen() {
        router.replaceScreen(Screens.Main())
    }

    fun initUserConfig() = viewModelScope.launch {
        val account = currentAccount ?: return@launch
        accounts.insert(listOf(account))

        UserConfig.parse(account)
    }

    object LoginCodeSent : VkEvent()
    object LoginSuccessAuth : VkEvent()

}