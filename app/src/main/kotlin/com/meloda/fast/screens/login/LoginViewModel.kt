package com.meloda.fast.screens.login

import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.network.auth.AuthDirectRequest
import com.meloda.fast.base.viewmodel.BaseViewModel
import com.meloda.fast.base.viewmodel.ErrorTextEvent
import com.meloda.fast.base.viewmodel.VkEvent
import com.meloda.fast.common.Screens
import com.meloda.fast.data.account.AccountsDao
import com.meloda.fast.data.auth.AuthRepository
import com.meloda.fast.model.AppAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
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
                authRepository.auth(
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
                    sendEvent(ErrorTextEvent(unknownErrorDefaultText))
                    return@makeJob
                }

                currentAccount = AppAccount(
                    userId = it.userId,
                    accessToken = it.accessToken,
                    fastToken = null
                ).also { account ->
                    UserConfig.currentUserId = account.userId
                    UserConfig.userId = account.userId
                    UserConfig.accessToken = account.accessToken
                }

                sendEvent(LoginSuccessAuth)
            }
        )
    }

    fun sendSms(validationSid: String) = viewModelScope.launch {
        makeJob({ authRepository.sendSms(validationSid) },
            onAnswer = { sendEvent(LoginCodeSent) }
        )
    }

    fun openPrimaryScreen() {
        router.replaceScreen(Screens.Main())
    }

    fun initUserConfig() = viewModelScope.launch {
        val account = requireNotNull(currentAccount)
        UserConfig.fastToken = account.fastToken

        accounts.insert(listOf(account))
    }

    fun saveAccount(userId: Int, accessToken: String, fastToken: String) = viewModelScope.launch {
        val account = AppAccount(userId, accessToken, fastToken)
        accounts.insert(listOf(account))
    }
}

object LoginCodeSent : VkEvent()
object LoginSuccessAuth : VkEvent()