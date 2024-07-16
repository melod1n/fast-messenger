package dev.meloda.fast.auth.userbanned.navigation

import android.os.Bundle
import androidx.core.os.BundleCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import dev.meloda.fast.auth.userbanned.model.UserBannedArguments
import dev.meloda.fast.auth.userbanned.presentation.UserBannedRoute
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.reflect.typeOf

@Serializable
data class UserBanned(val arguments: UserBannedArguments)

val UserBannedNavType = object : NavType<UserBannedArguments>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): UserBannedArguments? =
        BundleCompat.getParcelable(bundle, key, UserBannedArguments::class.java)

    override fun parseValue(value: String): UserBannedArguments = Json.decodeFromString(value)

    override fun serializeAsValue(value: UserBannedArguments): String = Json.encodeToString(value)

    override fun put(bundle: Bundle, key: String, value: UserBannedArguments) {
        bundle.putParcelable(key, value)
    }

    override val name: String = "UserBannedArguments"
}

fun NavGraphBuilder.userBannedRoute(onBack: () -> Unit) {
    composable<UserBanned>(
        typeMap = mapOf(typeOf<UserBannedArguments>() to UserBannedNavType)
    ) { backStackEntry ->
        val arguments: UserBannedArguments = backStackEntry.toRoute()

        UserBannedRoute(
            onBack = onBack,
            userName = arguments.userName,
            message = arguments.message
        )
    }
}

fun NavController.navigateToUserBanned(arguments: UserBannedArguments) {
    this.navigate(UserBanned(arguments))
}
