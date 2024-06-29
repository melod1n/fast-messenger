package com.meloda.app.fast.auth.twofa.navigation

import android.os.Bundle
import androidx.core.os.BundleCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.meloda.app.fast.auth.twofa.TwoFaViewModel
import com.meloda.app.fast.auth.twofa.TwoFaViewModelImpl
import com.meloda.app.fast.auth.twofa.model.TwoFaArguments
import com.meloda.app.fast.auth.twofa.model.TwoFaUiAction
import com.meloda.app.fast.auth.twofa.presentation.TwoFaScreen
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.koinViewModel
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
    onResult: (String) -> Unit
) {
    composable<TwoFa>(typeMap = mapOf(typeOf<TwoFaArguments>() to TwoFaNavType)) { backStackEntry ->
        val viewModel: TwoFaViewModel = koinViewModel<TwoFaViewModelImpl>()
        viewModel.setArguments(backStackEntry.toRoute())
        TwoFaScreen(
            onAction = { action ->
                when (action) {
                    TwoFaUiAction.BackClicked -> onBack()
                    is TwoFaUiAction.CodeResult -> onResult(action.code)
                }
            },
            viewModel = viewModel
        )
    }
}

fun NavController.navigateToTwoFa(arguments: TwoFaArguments) {
    this.navigate(TwoFa(arguments))
}

fun NavController.setTwoFaResult(code: String) {
    this.currentBackStackEntry
        ?.savedStateHandle
        ?.set("twofacode", code)
}
