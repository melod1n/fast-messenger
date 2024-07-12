package com.meloda.app.fast.auth.captcha.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.meloda.app.fast.auth.captcha.model.CaptchaArguments
import com.meloda.app.fast.auth.captcha.presentation.CaptchaScreen
import com.meloda.app.fast.common.customNavType
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

@Serializable
data class Captcha(val arguments: CaptchaArguments) {

    companion object {
        val typeMap = mapOf(typeOf<CaptchaArguments>() to customNavType<CaptchaArguments>())

        fun from(savedStateHandle: SavedStateHandle) =
            savedStateHandle.toRoute<Captcha>(typeMap)
    }
}


fun NavGraphBuilder.captchaRoute(
    onBack: () -> Unit,
    onResult: (String) -> Unit
) {
    composable<Captcha>(
        typeMap = Captcha.typeMap
    ) {
        CaptchaScreen(
            onBack = onBack,
            onResult = onResult
        )
    }
}

fun NavController.navigateToCaptcha(arguments: CaptchaArguments) {
    this.navigate(Captcha(arguments))
}

fun NavController.setCaptchaResult(code: String?) {
    this.currentBackStackEntry
        ?.savedStateHandle
        ?.set("captchacode", code)
}
