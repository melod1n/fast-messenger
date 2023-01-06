package com.meloda.fast.api.model.presentation

import android.text.SpannableString
import com.meloda.fast.api.model.data.ActionState
import com.meloda.fast.model.base.AdapterDiffItem
import com.meloda.fast.model.base.Image
import com.meloda.fast.model.base.Text

data class VkConversationUi(
    val conversationId: Int,
    val messageId: Int,
    val avatar: Image,
    val title: Text,
    val unreadCount: String?,
    val date: Int?,
    val message: SpannableString?,
    val attachmentImage: Image?,
    val isPinned: Boolean,
    val actionState: ActionState,
    val isBirthday: Boolean,
    val isRead: Boolean,
    val isAccount: Boolean,
) : AdapterDiffItem {
    override val id = conversationId
}
