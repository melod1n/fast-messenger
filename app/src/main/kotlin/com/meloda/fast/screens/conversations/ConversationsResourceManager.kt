package com.meloda.fast.screens.conversations

import android.content.Context
import com.meloda.fast.R
import com.meloda.fast.base.ResourceManager
import com.meloda.fast.extensions.TypeTransformations

class ConversationsResourceManager(context: Context) : ResourceManager(context) {

    val colorOutline = getColor(R.color.colorOutline)
    val colorOnPrimary = getColor(R.color.colorOnPrimary)
    val colorUserAvatarAction = getColor(R.color.colorUserAvatarAction)
    val colorOnUserAvatarAction = getColor(R.color.colorOnUserAvatarAction)

    val icLauncherColor = getColor(R.color.a1_500)

    val youPrefix = getString(R.string.you_message_prefix)

}