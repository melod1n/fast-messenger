package com.meloda.app.fast.messageshistory.model

import androidx.compose.runtime.Immutable
import com.meloda.app.fast.common.UiImage
import com.meloda.app.fast.model.api.domain.VkAttachment
import com.meloda.app.fast.model.api.domain.VkMessage

@Immutable
data class MessagesHistoryScreenState(
    val conversationId: Int,
    val title: String,
    val status: String?,
    val avatar: UiImage,
    val messages: List<VkMessage>,
    val message: String,
    val attachments: List<VkAttachment>,
    val isLoading: Boolean,
    val actionMode: ActionMode,
    val isNeedToOpenChatMaterials: Boolean
) {

    companion object {
        val EMPTY: MessagesHistoryScreenState = MessagesHistoryScreenState(
            conversationId = -1,
            title = "Loading...",
            status = null,
            avatar = UiImage.Color(0),
            messages = emptyList(),
            message = "",
            attachments = emptyList(),
            isLoading = true,
            actionMode = ActionMode.Record,
            isNeedToOpenChatMaterials = false
        )
    }
}
