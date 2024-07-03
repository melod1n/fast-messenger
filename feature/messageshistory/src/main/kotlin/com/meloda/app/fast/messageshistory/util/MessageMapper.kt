package com.meloda.app.fast.messageshistory.util

import android.content.res.Resources
import com.meloda.app.fast.common.UiImage
import com.meloda.app.fast.common.UiText
import com.meloda.app.fast.common.UserConfig
import com.meloda.app.fast.common.extensions.orDots
import com.meloda.app.fast.common.parseString
import com.meloda.app.fast.data.VkMemoryCache
import com.meloda.app.fast.designsystem.R
import com.meloda.app.fast.model.api.PeerType
import com.meloda.app.fast.model.api.domain.VkConversation
import com.meloda.app.fast.model.api.domain.VkMessage

import com.meloda.app.fast.designsystem.R as UiR

private fun isAccount(fromId: Int) = fromId == UserConfig.userId

fun VkMessage.extractAvatar() = when {
    isUser() -> {
        if (isAccount(id)) null
        else user?.photo200
    }

    isGroup() -> {
        group?.photo200
    }

    else -> null
}?.let(UiImage::Url) ?: UiImage.Resource(UiR.drawable.ic_account_circle_cut)

fun VkConversation.extractAvatar() = when (peerType) {
    PeerType.USER -> {
        if (isAccount(id)) null
        else user?.photo200
    }

    PeerType.GROUP -> {
        group?.photo200
    }

    PeerType.CHAT -> {
        photo200
    }
}?.let(UiImage::Url) ?: UiImage.Resource(R.drawable.ic_account_circle_cut)

fun VkConversation.extractTitle(
    useContactName: Boolean,
    resources: Resources
) = when (peerType) {
    PeerType.USER -> {
        if (isAccount(id)) {
            UiText.Resource(UiR.string.favorites)
        } else {
            val userName = user?.let { user ->
                if (useContactName) {
                    VkMemoryCache.getContact(user.id)?.name
                } else {
                    user.fullName
                }
            }

            UiText.Simple(userName.orDots())
        }
    }

    PeerType.GROUP -> UiText.Simple(group?.name.orDots())
    PeerType.CHAT -> UiText.Simple(title.orDots())
}.parseString(resources).orDots()
