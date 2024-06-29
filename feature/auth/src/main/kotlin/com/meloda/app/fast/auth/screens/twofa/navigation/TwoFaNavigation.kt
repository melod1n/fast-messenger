package com.meloda.app.fast.auth.screens.twofa.navigation

import android.os.Bundle
import androidx.core.os.BundleCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.meloda.app.fast.auth.screens.twofa.model.TwoFaArguments
import com.meloda.app.fast.auth.screens.twofa.model.TwoFaUiAction
import com.meloda.app.fast.auth.screens.twofa.presentation.TwoFaScreen
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.reflect.typeOf

@Serializable
data class TwoFa(val arguments: TwoFaArguments)

val TwoFaNavType = object : NavType<TwoFaArguments>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): TwoFaArguments? =
        BundleCompat.getParcelable(bundle, key, TwoFaArguments::class.java)

    override fun parseValue(value: String): TwoFaArguments = Json.decodeFromString(value)

    override fun serializeAsValue(value: TwoFaArguments): String = Json.encodeToString(value)

    override fun put(bundle: Bundle, key: String, value: TwoFaArguments) {
        bundle.putParcelable(key, value)
    }

    override val name: String = "TwoFaArguments"
}

fun NavGraphBuilder.twoFaRoute(
    onBack: () -> Unit,
    resultCode: (String) -> Unit
) {
    composable<TwoFa>(typeMap = mapOf(typeOf<TwoFaArguments>() to TwoFaNavType)) { backStackEntry ->
        TwoFaScreen(
            arguments = backStackEntry.toRoute(),
            onAction = { action ->
                when (action) {
                    TwoFaUiAction.BackClicked -> onBack()
                    is TwoFaUiAction.CodeResult -> resultCode(action.code)
                }
            }
        )
    }
}

fun NavController.setTwoFaResult(code: String) {
    this.currentBackStackEntry
        ?.savedStateHandle
        ?.set("code", code)
}
