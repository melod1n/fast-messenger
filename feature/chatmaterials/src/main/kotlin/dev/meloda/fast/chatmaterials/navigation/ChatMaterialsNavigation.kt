package dev.meloda.fast.chatmaterials.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import dev.meloda.fast.chatmaterials.presentation.ChatMaterialsRoute
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
    onBack: () -> Unit,
    onPhotoClicked: (url: String) -> Unit
) {
    composable<ChatMaterials> {
        ChatMaterialsRoute(
            onBack = onBack,
            onPhotoClicked = onPhotoClicked
        )
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
