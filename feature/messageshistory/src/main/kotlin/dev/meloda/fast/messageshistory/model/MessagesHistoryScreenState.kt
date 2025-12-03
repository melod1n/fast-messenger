package dev.meloda.fast.messageshistory.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import dev.meloda.fast.common.model.UiImage
import dev.meloda.fast.model.api.domain.VkAttachment
import dev.meloda.fast.model.api.domain.VkConversation
import dev.meloda.fast.model.api.domain.VkMessage

@Immutable
data class MessagesHistoryScreenState(
    val conversationId: Long,
    val title: String,
    val status: String?,
    val avatar: UiImage,
    val message: TextFieldValue,
    val attachments: List<VkAttachment>,
    val isLoading: Boolean,
    val isPaginating: Boolean,
    val isPaginationExhausted: Boolean,
    val actionMode: ActionMode,
    val chatImageUrl: String?,
    val conversation: VkConversation,
    val pinnedMessage: VkMessage?,
    val pinnedTitle: String?,
    val pinnedSummary: AnnotatedString?,
    val replyTitle: String?,
    val replyText: String?
) {

    companion object {
        val EMPTY: MessagesHistoryScreenState = MessagesHistoryScreenState(
            conversationId = -1,
            title = "",
            status = null,
            avatar = UiImage.Color(0),
            message = TextFieldValue(),
            attachments = emptyList(),
            isLoading = true,
            isPaginating = false,
            isPaginationExhausted = false,
            actionMode = ActionMode.RECORD_AUDIO,
            chatImageUrl = null,
            conversation = VkConversation.EMPTY,
            pinnedMessage = null,
            pinnedTitle = null,
            pinnedSummary = null,
            replyTitle = null,
            replyText = null,
        )
    }
}
