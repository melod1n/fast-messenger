package com.meloda.app.fast.auth.captcha.navigation

import android.os.Bundle
import androidx.core.os.BundleCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.meloda.app.fast.auth.captcha.CaptchaViewModel
import com.meloda.app.fast.auth.captcha.CaptchaViewModelImpl
import com.meloda.app.fast.auth.captcha.model.CaptchaArguments
import com.meloda.app.fast.auth.captcha.presentation.CaptchaScreen
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.koinViewModel
import kotlin.reflect.typeOf

@Serializable
data class Captcha(val arguments: CaptchaArguments)

val CaptchaNavType = object : NavType<CaptchaArguments>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): CaptchaArguments? =
        BundleCompat.getParcelable(bundle, key, CaptchaArguments::class.java)

    override fun parseValue(value: String): CaptchaArguments = Json.decodeFromString(value)

    override fun serializeAsValue(value: CaptchaArguments): String = Json.encodeToString(value)

    override fun put(bundle: Bundle, key: String, value: CaptchaArguments) {
        bundle.putParcelable(key, value)
    }

    override val name: String = "CaptchaArguments"
}

fun NavGraphBuilder.captchaRoute(
    onBack: () -> Unit,
    onResult: (String) -> Unit
) {
    composable<Captcha>(typeMap = mapOf(typeOf<CaptchaArguments>() to CaptchaNavType)) { backStackEntry ->
        val viewModel: CaptchaViewModel = koinViewModel<CaptchaViewModelImpl>()
        viewModel.setArguments(backStackEntry.toRoute())
        CaptchaScreen(
            onBack = onBack,
            onResult = onResult,
            viewModel = viewModel
        )
    }
}

fun NavController.navigateToCaptcha(arguments: CaptchaArguments) {
    this.navigate(Captcha(arguments))
}

fun NavController.setCaptchaResult(code: String) {
    this.currentBackStackEntry
        ?.savedStateHandle
        ?.set("captchacode", code)
}
