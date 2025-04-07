package dev.meloda.fast.messageshistory.model

import androidx.compose.ui.text.AnnotatedString
import dev.meloda.fast.common.model.UiImage
import dev.meloda.fast.model.api.domain.VkAttachment

sealed class UiItem(
    open val id: Long,
    open val cmId: Long
) {

    data class Message(
        override val id: Long,
        override val cmId: Long,
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
        val isImportant: Boolean,
        val attachments: List<VkAttachment>?
    ) : UiItem(id, cmId)

    data class ActionMessage(
        override val id: Long,
        override val cmId: Long,
        val text: AnnotatedString,
        val actionCmId: Long?
    ) : UiItem(id, cmId)
}
