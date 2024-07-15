package com.meloda.app.fast.messageshistory.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.meloda.app.fast.common.extensions.customNavType
import com.meloda.app.fast.messageshistory.model.MessagesHistoryArguments
import com.meloda.app.fast.messageshistory.presentation.MessagesHistoryRoute
import com.meloda.app.fast.model.BaseError
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
    onChatMaterialsDropdownItemClicked: (peerId: Int, conversationMessageId: Int) -> Unit
) {
    composable<MessagesHistory>(typeMap = MessagesHistory.typeMap) {
        MessagesHistoryRoute(
            onError = onError,
            onBack = onBack,
            onChatMaterialsDropdownItemClicked = onChatMaterialsDropdownItemClicked,
        )
    }
}

fun NavController.navigateToMessagesHistory(conversationId: Int) {
    this.navigate(MessagesHistory(MessagesHistoryArguments(conversationId)))
}
