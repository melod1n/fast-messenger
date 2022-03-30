package com.meloda.fast.screens.conversations

import android.content.Context
import com.meloda.fast.R
import com.meloda.fast.base.ResourceProvider

class ConversationsResourceProvider(context: Context) : ResourceProvider(context) {

    val colorOutline = getColor(R.color.colorOutline)
    val colorOnPrimary = getColor(R.color.colorOnPrimary)
    val colorUserAvatarAction = getColor(R.color.colorUserAvatarAction)
    val colorOnUserAvatarAction = getColor(R.color.colorOnUserAvatarAction)
    val colorBackground = getColor(R.color.colorBackground)
    val colorBackgroundVariant = getColor(R.color.colorBackgroundVariant)

    val icLauncherColor = getColor(R.color.a1_500)

    val youPrefix = getString(R.string.you_message_prefix)

    val conversationUnreadBackground = getDrawable(R.drawable.ic_message_unread)

    val iconForwardedMessages = getDrawable(R.drawable.ic_attachment_forwarded_messages)
    val iconForwardedMessage = getDrawable(R.drawable.ic_attachment_forwarded_message)

}