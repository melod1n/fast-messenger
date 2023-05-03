package com.meloda.fast.api.model.presentation

import android.text.SpannableString
import com.meloda.fast.api.model.ActionState
import com.meloda.fast.api.model.ConversationPeerType
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.model.base.AdapterDiffItem
import com.meloda.fast.model.base.UiImage
import com.meloda.fast.model.base.UiText

data class VkConversationUi(
    val conversationId: Int,
    val lastMessageId: Int,
    val avatar: UiImage,
    val title: UiText,
    val unreadCount: String?,
    val date: Int?,
    val message: SpannableString?,
    val attachmentImage: UiImage?,
    val isPinned: Boolean,
    val actionState: ActionState,
    val isBirthday: Boolean,
    val isRead: Boolean,
    val isAccount: Boolean,
    val isOnline: Boolean,
    val lastMessage: VkMessage?,
    val conversationUser: VkUser?,
    val conversationGroup: VkGroup?,
    val actionUser: VkUser?,
    val actionGroup: VkGroup?,
    val action: VkMessage.Action?,
    val messageUser: VkUser?,
    val messageGroup: VkGroup?,
    val peerType: ConversationPeerType,
) : AdapterDiffItem {
    override val id = conversationId
}
