package com.meloda.fast.screens.messages.model

import androidx.compose.runtime.Immutable
import com.meloda.fast.api.model.domain.VkAttachment
import com.meloda.fast.api.model.domain.VkMessageDomain

@Immutable
data class MessagesHistoryScreenState(
    val conversationId: Int,
    val title: String,
    val status: String?,
    val avatar: Avatar,
    val messages: List<VkMessageDomain>,
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
            avatar = Avatar.Empty,
            messages = emptyList(),
            message = "",
            attachments = emptyList(),
            isLoading = true,
            actionMode = ActionMode.Record,
            isNeedToOpenChatMaterials = false
        )
    }
}
