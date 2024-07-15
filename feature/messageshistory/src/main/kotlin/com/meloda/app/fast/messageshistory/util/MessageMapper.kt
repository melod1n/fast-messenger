package com.meloda.app.fast.messageshistory.util

import android.content.res.Resources
import com.meloda.app.fast.common.UiImage
import com.meloda.app.fast.common.UiText
import com.meloda.app.fast.common.UserConfig
import com.meloda.app.fast.common.extensions.orDots
import com.meloda.app.fast.common.parseString
import com.meloda.app.fast.data.VkMemoryCache
import com.meloda.app.fast.ui.R
import com.meloda.app.fast.messageshistory.model.UiMessage
import com.meloda.app.fast.model.api.PeerType
import com.meloda.app.fast.model.api.domain.VkConversation
import com.meloda.app.fast.model.api.domain.VkMessage
import java.text.SimpleDateFormat
import java.util.Locale
import com.meloda.app.fast.ui.R as UiR

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

fun VkMessage.extractDate(): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(date * 1000L)

fun VkMessage.extractTitle(): String = when {
    isUser() -> "%s %s".format(
        user?.firstName.orDots(),
        user?.lastName?.firstOrNull()?.toString().orEmpty().plus(".")
    )

    isGroup() -> group?.name.orDots()

    else -> throw IllegalStateException("Message is not from user nor group. fromId: $fromId")
}

fun VkConversation.extractAvatar(): UiImage = when (peerType) {
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

fun VkMessage.asPresentation(
    showDate: Boolean,
    showName: Boolean,
    prevMessage: VkMessage?,
    nextMessage: VkMessage?
): UiMessage = UiMessage(
    id = id,
    conversationMessageId = conversationMessageId,
    text = text,
    isOut = isOut,
    fromId = fromId,
    date = extractDate(),
    randomId = randomId,
    isInChat = isPeerChat(),
    name = extractTitle(),
    showDate = showDate,
    showAvatar = extractShowAvatar(nextMessage),
    showName = showName && extractShowName(prevMessage),
    avatar = extractAvatar(),
    isEdited = updateTime != null
)

fun VkMessage.extractShowAvatar(nextMessage: VkMessage?): Boolean {
    if (isOut) return false
    return nextMessage == null || nextMessage.fromId != fromId
}

fun VkMessage.extractShowName(prevMessage: VkMessage?): Boolean {
    if (isOut || !isPeerChat()) return false
    return prevMessage == null || prevMessage.fromId != fromId
}
