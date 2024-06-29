package com.meloda.app.fast.messageshistory.navigation

import android.os.Bundle
import androidx.core.os.BundleCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import com.meloda.app.fast.messageshistory.model.MessagesHistoryArguments
import com.meloda.app.fast.messageshistory.presentation.MessagesHistoryScreen
import com.meloda.app.fast.model.BaseError
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.reflect.typeOf

@Serializable
data class MessagesHistory(val arguments: MessagesHistoryArguments)

val MessagesHistoryNavType = object : NavType<MessagesHistoryArguments>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): MessagesHistoryArguments? =
        BundleCompat.getParcelable(bundle, key, MessagesHistoryArguments::class.java)

    override fun parseValue(value: String): MessagesHistoryArguments = Json.decodeFromString(value)

    override fun serializeAsValue(value: MessagesHistoryArguments): String =
        Json.encodeToString(value)

    override fun put(bundle: Bundle, key: String, value: MessagesHistoryArguments) {
        bundle.putParcelable(key, value)
    }

    override val name: String = "MessagesHistoryArguments"
}

fun NavGraphBuilder.messagesHistoryRoute(
    onError: (BaseError) -> Unit,
    onBack: () -> Unit,
    onNavigateToChatAttachments: () -> Unit
) {
    composable<MessagesHistory>(typeMap = mapOf(typeOf<MessagesHistoryArguments>() to MessagesHistoryNavType)) {
        MessagesHistoryScreen(
            onError = onError,
            onBack = onBack,
            onNavigateToChatMaterials = onNavigateToChatAttachments
        )
    }
}

fun NavController.navigateToMessagesHistory(arguments: MessagesHistoryArguments) {
    this.navigate(MessagesHistory(arguments))
}
