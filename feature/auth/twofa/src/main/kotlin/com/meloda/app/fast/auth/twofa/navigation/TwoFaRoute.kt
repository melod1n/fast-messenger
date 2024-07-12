package com.meloda.app.fast.auth.twofa.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.meloda.app.fast.auth.twofa.model.TwoFaArguments
import com.meloda.app.fast.auth.twofa.presentation.TwoFaScreen
import com.meloda.app.fast.common.customNavType
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

@Serializable
data class TwoFa(val arguments: TwoFaArguments) {
    companion object {
        val typeMap = mapOf(typeOf<TwoFaArguments>() to customNavType<TwoFaArguments>())

        fun from(savedStateHandle: SavedStateHandle) =
            savedStateHandle.toRoute<TwoFa>(typeMap)
    }
}

fun NavGraphBuilder.twoFaRoute(
    onBack: () -> Unit,
    onResult: (String) -> Unit
) {
    composable<TwoFa>(typeMap = TwoFa.typeMap) {
        TwoFaScreen(
            onBack = onBack,
            onCodeResult = onResult
        )
    }
}

fun NavController.navigateToTwoFa(arguments: TwoFaArguments) {
    this.navigate(TwoFa(arguments))
}

fun NavController.setTwoFaResult(code: String?) {
    this.currentBackStackEntry
        ?.savedStateHandle
        ?.set("twofacode", code)
}


