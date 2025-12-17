package dev.meloda.fast.ui.model.vk

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.AnnotatedString
import dev.meloda.fast.common.model.UiImage
import dev.meloda.fast.model.api.domain.VkAttachment
import dev.meloda.fast.ui.util.ImmutableList

@Stable
sealed class MessageUiItem(
    open val id: Long,
    open val cmId: Long
) {

    @Stable
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
        val attachments: ImmutableList<VkAttachment>?,
        val replyCmId: Long?,
        val replyTitle: String?,
        val replySummary: AnnotatedString?
    ) : MessageUiItem(id, cmId)

    @Stable
    data class ActionMessage(
        override val id: Long,
        override val cmId: Long,
        val text: AnnotatedString,
        val actionCmId: Long?
    ) : MessageUiItem(id, cmId)
}
