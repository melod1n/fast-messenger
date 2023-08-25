package com.meloda.fast.screens.messages.model

import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.attachments.VkAttachment
import com.meloda.fast.model.base.UiImage

data class MessagesHistoryScreenState(
    val title: String,
    val avatar: UiImage?,
    val messages: List<VkMessage>,
    val message: String,
    val attachments: List<VkAttachment>,
    val isLoading: Boolean,
    val actionButtonMode: MessagesHistoryActionButtonMode
) {

    companion object {
        val EMPTY: MessagesHistoryScreenState = MessagesHistoryScreenState(
            title = "Loading...",
            avatar = null,
            messages = emptyList(),
            message = "",
            attachments = emptyList(),
            isLoading = true,
            actionButtonMode = MessagesHistoryActionButtonMode.Record
        )
    }
}
