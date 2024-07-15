package com.meloda.app.fast.messageshistory.model

import androidx.compose.runtime.Immutable
import com.meloda.app.fast.common.model.UiImage
import com.meloda.app.fast.model.api.domain.VkAttachment

@Immutable
data class MessagesHistoryScreenState(
    val conversationId: Int,
    val title: String,
    val status: String?,
    val avatar: UiImage,
    val messages: List<UiMessage>,
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
