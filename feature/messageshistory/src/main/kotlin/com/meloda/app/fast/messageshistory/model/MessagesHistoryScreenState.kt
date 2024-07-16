package dev.meloda.fast.messageshistory.model

import androidx.compose.runtime.Immutable
import dev.meloda.fast.common.model.UiImage
import dev.meloda.fast.model.api.domain.VkAttachment

@Immutable
data class MessagesHistoryScreenState(
    val conversationId: Int,
    val title: String,
    val status: String?,
    val avatar: UiImage,
    val messages: List<UiItem>,
    val message: String,
    val attachments: List<VkAttachment>,
    val isLoading: Boolean,
    val isPaginating: Boolean,
    val isPaginationExhausted: Boolean,
    val actionMode: ActionMode,
) {

    companion object {
        val EMPTY: MessagesHistoryScreenState = MessagesHistoryScreenState(
            conversationId = -1,
            title = "",
            status = null,
            avatar = UiImage.Color(0),
            messages = emptyList(),
            message = "",
            attachments = emptyList(),
            isLoading = true,
            isPaginating = false,
            isPaginationExhausted = false,
            actionMode = ActionMode.Record,
        )
    }
}
