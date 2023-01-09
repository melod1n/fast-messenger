package com.meloda.fast.screens.login

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.network.auth.AuthDirectRequest
import com.meloda.fast.base.viewmodel.BaseViewModel
import com.meloda.fast.base.viewmodel.UnknownErrorEvent
import com.meloda.fast.base.viewmodel.VkEvent
import com.meloda.fast.common.Screens
import com.meloda.fast.data.account.AccountsDao
import com.meloda.fast.data.auth.AuthRepository
import com.meloda.fast.ext.requireValue
import com.meloda.fast.model.AppAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val router: Router,
    private val accounts: AccountsDao
) : BaseViewModel() {

    val loginValue = MutableLiveData<String>()
    val passwordValue = MutableLiveData<String>()
    val captchaValue = MutableLiveData<String?>()
    val twoFaValue = MutableLiveData<String?>()

    val isLoginValid = MediatorLiveData<Boolean>().apply {
        addSource(loginValue) { login ->
            this.value = !login.isNullOrBlank()
        }
    }
    val isPasswordValid = MediatorLiveData<Boolean>().apply {
        addSource(passwordValue) { password ->
            this.value = !password.isNullOrBlank()
        }
    }
    val isFieldsValid = MediatorLiveData<Boolean>().apply {
        val observer = Observer<Boolean> {
            this.value = isLoginValid.value == true && isPasswordValid.value == true
        }
        addSource(isLoginValid, observer)
        addSource(isPasswordValid, observer)
    }

    var currentAccount: AppAccount? = null

    fun login() {
        if (isLoginValid.value != true || isPasswordValid.value != true) return

        val captchaSplit =
            if (captchaValue.value != null) {
                captchaValue.requireValue()?.split(";")?.run { first() to last() }
            } else {
                null
            }


        viewModelScope.launch(Dispatchers.IO) {
            makeJob(
                {
                    authRepository.auth(
                        AuthDirectRequest(
                            grantType = VKConstants.Auth.GrantType.PASSWORD,
                            clientId = VKConstants.VK_APP_ID,
                            clientSecret = VKConstants.VK_SECRET,
                            username = loginValue.value.orEmpty(),
                            password = passwordValue.value.orEmpty(),
                            scope = VKConstants.Auth.SCOPE,
                            twoFaForceSms = true,
                            twoFaCode = twoFaValue.value,
                            captchaSid = captchaSplit?.first,
                            captchaKey = captchaSplit?.second
                        )
                    )
                },
                onAnswer = {
                    if (it.userId == null || it.accessToken == null) {
                        sendEvent(UnknownErrorEvent)
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
                },
                onAnyResult = {
                    captchaValue.value = null
                    twoFaValue.value = null
                }
            )
        }
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
}

object LoginCodeSent : VkEvent()
object LoginSuccessAuth : VkEvent()
