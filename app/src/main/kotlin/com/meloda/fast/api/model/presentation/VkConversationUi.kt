package com.meloda.fast.api.model.presentation

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString
import com.meloda.fast.api.model.ConversationPeerType
import com.meloda.fast.api.model.domain.VkGroupDomain
import com.meloda.fast.api.model.domain.VkMessageDomain
import com.meloda.fast.api.model.domain.VkUserDomain
import com.meloda.fast.model.base.UiImage
import com.meloda.fast.screens.conversations.model.ConversationOption

@Immutable
data class VkConversationUi(
    val id: Int,
    val lastMessageId: Int,
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
    val lastMessage: VkMessageDomain?,
    val conversationUser: VkUserDomain?,
    val conversationGroup: VkGroupDomain?,
    val peerType: ConversationPeerType,
    val interactionText: String?,
    val isExpanded: Boolean,
    val options: List<ConversationOption>,
    val lastSeenStatus: String?
)
