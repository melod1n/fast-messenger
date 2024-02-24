package com.meloda.fast.screens.messages.model

import androidx.compose.runtime.Immutable
import com.meloda.fast.api.model.VkMessageDomain
import com.meloda.fast.api.model.attachments.VkAttachment
import com.meloda.fast.model.base.UiImage

@Immutable
data class MessagesHistoryScreenState(
    val title: String,
    val avatar: UiImage?,
    val messages: List<VkMessageDomain>,
    val message: String,
    val attachments: List<VkAttachment>,
    val isLoading: Boolean,
    val actionButtonMode: MessagesHistoryActionButtonMode,
    val isNeedToOpenChatMaterials: Boolean
) {

    companion object {
        val EMPTY: MessagesHistoryScreenState = MessagesHistoryScreenState(
            title = "Loading...",
            avatar = null,
            messages = emptyList(),
            message = "",
            attachments = emptyList(),
            isLoading = true,
            actionButtonMode = MessagesHistoryActionButtonMode.Record,
            isNeedToOpenChatMaterials = false
        )
    }
}
