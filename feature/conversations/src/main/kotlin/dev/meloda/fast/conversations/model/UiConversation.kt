package dev.meloda.fast.conversations.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import dev.meloda.fast.common.model.UiImage
import dev.meloda.fast.model.api.PeerType
import dev.meloda.fast.model.api.domain.VkMessage
import dev.meloda.fast.ui.util.ImmutableList

@Immutable
data class UiConversation(
    val id: Int,
    val lastMessageId: Int?,
    val avatar: UiImage?,
    val title: String,
    val unreadCount: String?,
    val date: String,
    val message: AnnotatedString,
    val attachmentImage: UiImage?,
    val isPinned: Boolean,
    val actionImageId: Int,
    val isBirthday: Boolean,
    val isUnread: Boolean,
    val isAccount: Boolean,
    val isOnline: Boolean,
    val lastMessage: VkMessage?,
    val peerType: PeerType,
    val interactionText: String?,
    val isExpanded: Boolean,
    val options: ImmutableList<ConversationOption>,
)
