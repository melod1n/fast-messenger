package com.meloda.app.fast.chatmaterials.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.meloda.app.fast.chatmaterials.presentation.ChatMaterialsRoute
import kotlinx.serialization.Serializable

@Serializable
data class ChatMaterials(
    val peerId: Int,
    val conversationMessageId: Int
) {
    companion object {
        fun from(savedStateHandle: SavedStateHandle) =
            savedStateHandle.toRoute<ChatMaterials>()
    }
}

fun NavGraphBuilder.chatMaterialsScreen(
    onBack: () -> Unit
) {
    composable<ChatMaterials> {
        ChatMaterialsRoute(onBack = onBack)
    }
}

fun NavController.navigateToChatMaterials(peerId: Int, conversationMessageId: Int) {
    this.navigate(
        ChatMaterials(
            peerId = peerId,
            conversationMessageId = conversationMessageId
        )
    )
}