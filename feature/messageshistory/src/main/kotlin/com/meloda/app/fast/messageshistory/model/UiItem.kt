package com.meloda.app.fast.messageshistory.model

import androidx.compose.ui.text.AnnotatedString
import com.meloda.app.fast.common.model.UiImage

sealed class UiItem(
    open val id: Int,
    val cmId: Int
) {

    data class Message(
        override val id: Int,
        val conversationMessageId: Int,
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
        val avatar: UiImage,
        val isEdited: Boolean
    ) : UiItem(id, conversationMessageId)

    data class ActionMessage(
        override val id: Int,
        val conversationMessageId: Int,
        val text: AnnotatedString,
        val actionCmId: Int?
    ) : UiItem(id, conversationMessageId)
}

