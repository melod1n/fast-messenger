package dev.meloda.fast.messageshistory.model

import androidx.compose.ui.text.AnnotatedString
import dev.meloda.fast.common.model.UiImage

sealed class UiItem(
    open val id: Long,
    val cmId: Long
) {

    data class Message(
        override val id: Long,
        val conversationMessageId: Long,
        val text: AnnotatedString?,
        val isOut: Boolean,
        val fromId: Long,
        val date: String,
        val randomId: Long,
        val isInChat: Boolean,
        val name: String,
        val showDate: Boolean,
        val showAvatar: Boolean,
        val showName: Boolean,
        val avatar: UiImage,
        val isEdited: Boolean,
        val isRead: Boolean,
        val sendingStatus: SendingStatus,
        val isSelected: Boolean,
        val isPinned: Boolean,
        val isImportant: Boolean
    ) : UiItem(id, conversationMessageId)

    data class ActionMessage(
        override val id: Long,
        val conversationMessageId: Long,
        val text: AnnotatedString,
        val actionCmId: Long?
    ) : UiItem(id, conversationMessageId)
}
