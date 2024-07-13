package com.meloda.app.fast.auth.captcha.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.meloda.app.fast.auth.captcha.presentation.CaptchaRoute
import kotlinx.serialization.Serializable

@Serializable
data class Captcha(val captchaImageUrl: String) {

    companion object {
        fun from(savedStateHandle: SavedStateHandle) = savedStateHandle.toRoute<Captcha>()
    }
}


fun NavGraphBuilder.captchaScreen(
    onBack: () -> Unit,
    onResult: (String) -> Unit
) {
    composable<Captcha> {
        CaptchaRoute(
            onBack = onBack,
            onResult = onResult
        )
    }
}

fun NavController.navigateToCaptcha(captchaImageUrl: String) {
    this.navigate(Captcha(captchaImageUrl))
}

fun NavController.setCaptchaResult(code: String?) {
    this.currentBackStackEntry
        ?.savedStateHandle
        ?.set("captcha_code", code)
}
