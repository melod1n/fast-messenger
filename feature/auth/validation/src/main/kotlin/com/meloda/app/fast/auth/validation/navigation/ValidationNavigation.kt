package com.meloda.app.fast.auth.validation.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.meloda.app.fast.auth.validation.model.ValidationArguments
import com.meloda.app.fast.auth.validation.presentation.ValidationRoute
import com.meloda.app.fast.common.extensions.customNavType
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

@Serializable
data class Validation(val arguments: ValidationArguments) {
    companion object {
        val typeMap = mapOf(typeOf<ValidationArguments>() to customNavType<ValidationArguments>())

        fun from(savedStateHandle: SavedStateHandle) =
            savedStateHandle.toRoute<Validation>(typeMap)
    }
}

fun NavGraphBuilder.validationScreen(
    onBack: () -> Unit,
    onResult: (String) -> Unit
) {
    composable<Validation>(typeMap = Validation.typeMap) {
        ValidationRoute(
            onBack = onBack,
            onResult = onResult
        )
    }
}

fun NavController.navigateToValidation(arguments: ValidationArguments) {
    this.navigate(Validation(arguments))
}

fun NavController.setValidationResult(code: String?) {
    this.currentBackStackEntry
        ?.savedStateHandle
        ?.set("validation_code", code)
}


