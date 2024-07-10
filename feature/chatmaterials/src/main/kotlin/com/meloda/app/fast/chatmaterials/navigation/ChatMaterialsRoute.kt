package com.meloda.app.fast.chatmaterials.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.meloda.app.fast.chatmaterials.ChatMaterialsScreen
import kotlinx.serialization.Serializable

@Serializable
data class ChatMaterials(val a: String)

fun NavGraphBuilder.chatMaterialsRoute(
    onBack: () -> Unit
) {
    composable<ChatMaterials> {
        ChatMaterialsScreen(
            onBack = onBack
        )
    }
}

fun NavController.navigateToChatMaterials() {
    this.navigate(ChatMaterials(""))
}
