package com.meloda.fast.api.model.presentation

import androidx.compose.runtime.Immutable
import com.meloda.fast.api.model.domain.VkConversationDomain
import com.meloda.fast.api.model.domain.VkGroupDomain
import com.meloda.fast.api.model.domain.VkMessageDomain
import com.meloda.fast.api.model.domain.VkUserDomain
import com.meloda.fast.model.base.UiImage
import com.meloda.fast.screens.conversations.model.ConversationOption

@Immutable
data class VkConversationUi(
    val conversationId: Int,
    val lastMessageId: Int,
    val avatar: UiImage,
    val title: String,
    val unreadCount: String?,
    val date: String,
    val message: String,
    val attachmentImage: UiImage?,
    val isPinned: Boolean,
    // TODO: 10/03/2024, Danil Nikolaev: rewrite to use image
    val actionState: VkConversationDomain.ActionState,
    val isBirthday: Boolean,
    val isUnread: Boolean,
    val isAccount: Boolean,
    val isOnline: Boolean,
    val lastMessage: VkMessageDomain?,
    val conversationUser: VkUserDomain?,
    val conversationGroup: VkGroupDomain?,
    val peerType: VkConversationDomain.ConversationPeerType,
    val interactionText: String?,
    val isExpanded: Boolean,
    val options: List<ConversationOption>
)
