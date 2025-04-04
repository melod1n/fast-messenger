package dev.meloda.fast.messageshistory.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import dev.meloda.fast.messageshistory.model.MessagesHistoryArguments
import dev.meloda.fast.messageshistory.presentation.MessagesHistoryRoute
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.ui.extensions.customNavType
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

@Serializable
data class MessagesHistory(val arguments: MessagesHistoryArguments) {

    companion object {
        val typeMap =
            mapOf(typeOf<MessagesHistoryArguments>() to customNavType<MessagesHistoryArguments>())

        fun from(savedStateHandle: SavedStateHandle) =
            savedStateHandle.toRoute<MessagesHistory>(typeMap)
    }
}

fun NavGraphBuilder.messagesHistoryScreen(
    onError: (BaseError) -> Unit,
    onBack: () -> Unit,
    onNavigateToChatMaterials: (peerId: Long, cmId: Long) -> Unit
) {
    composable<MessagesHistory>(typeMap = MessagesHistory.typeMap) {
        MessagesHistoryRoute(
            onError = onError,
            onBack = onBack,
            onNavigateToChatMaterials = onNavigateToChatMaterials
        )
    }
}

fun NavController.navigateToMessagesHistory(conversationId: Long) {
    this.navigate(MessagesHistory(MessagesHistoryArguments(conversationId)))
}
