package com.meloda.fast.api.model.presentation

import android.graphics.drawable.Drawable
import com.meloda.fast.api.model.ActionState
import com.meloda.fast.api.model.ConversationPeerType
import com.meloda.fast.api.model.InteractionType
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.model.base.UiImage

data class VkConversationUi(
    val conversationId: Int,
    val lastMessageId: Int,
    val avatar: UiImage,
    val title: String,
    val unreadCount: String?,
    val date: String,
    val message: String,
    val attachmentImage: Drawable?,
    val isPinned: Boolean,
    val actionState: ActionState,
    val isBirthday: Boolean,
    val isUnread: Boolean,
    val isAccount: Boolean,
    val isOnline: Boolean,
    val lastMessage: VkMessage?,
    val conversationUser: VkUser?,
    val conversationGroup: VkGroup?,
    val peerType: ConversationPeerType,
    val interactionType: InteractionType?,
    val interactiveUsers: List<String>
)
