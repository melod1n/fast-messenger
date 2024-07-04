package com.meloda.app.fast.messageshistory.model

import com.meloda.app.fast.common.UiImage

data class UiMessage(
    val id: Int,
    val text: String?,
    val isOut: Boolean,
    val fromId: Int,
    val date: String,
    val randomId: Int,
    val isInChat: Boolean,
    val name: String,
    val showDate: Boolean,
    val showAvatar: Boolean,
    val showName: Boolean,
    val avatar: UiImage
)
