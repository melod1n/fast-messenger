package dev.meloda.fast.chatmaterials.model

import androidx.compose.runtime.Immutable

@Immutable
data class ChatMaterialsScreenState(
    val isLoading: Boolean,
    val materials: List<UiChatMaterial>,
    val attachmentType: String,
    val isPaginating: Boolean,
    val isPaginationExhausted: Boolean,
    val peerId: Int,
    val conversationMessageId: Int
) {

    companion object {
        val EMPTY: ChatMaterialsScreenState = ChatMaterialsScreenState(
            isLoading = true,
            materials = emptyList(),
            attachmentType = "photo",
            isPaginating = false,
            isPaginationExhausted = false,
            peerId = -1,
            conversationMessageId = -1
        )
    }
}
