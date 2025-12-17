package dev.meloda.fast.domain.util

import android.content.res.Resources
import dev.meloda.fast.common.util.TimeUtils
import dev.meloda.fast.model.api.domain.VkConvo
import dev.meloda.fast.ui.R
import dev.meloda.fast.ui.model.vk.ActionState
import dev.meloda.fast.ui.model.vk.ConvoOption
import dev.meloda.fast.ui.model.vk.UiConvo
import dev.meloda.fast.ui.util.ImmutableList
import dev.meloda.fast.ui.util.emptyImmutableList

fun VkConvo.asPresentation(
    resources: Resources,
    useContactName: Boolean,
    isExpanded: Boolean = false,
    options: ImmutableList<ConvoOption> = emptyImmutableList()
): UiConvo = UiConvo(
    id = id,
    lastMessageId = lastMessageId,
    avatar = extractAvatar(),
    title = extractTitle(useContactName, resources),
    unreadCount = extractUnreadCount(lastMessage, this),
    date = TimeUtils.getLocalizedTime(
        date = (lastMessage?.date ?: -1) * 1000L,
        yearShort = { resources.getString(R.string.year_short) },
        monthShort = { resources.getString(R.string.month_short) },
        weekShort = { resources.getString(R.string.week_short) },
        dayShort = { resources.getString(R.string.day_short) },
        now = { resources.getString(R.string.time_now) },
    ),
    message = extractMessage(resources, lastMessage, id, peerType),
    attachmentImage = if (lastMessage?.text == null) null
    else getAttachmentConvoIcon(lastMessage),
    isPinned = majorId > 0,
    actionImageId = ActionState.parse(isPhantom, isCallInProgress).getResourceId(),
    isBirthday = extractBirthday(this),
    isUnread = !isRead(),
    isAccount = isAccount(id),
    isOnline = !isAccount(id) && user?.onlineStatus?.isOnline() == true,
    lastMessage = lastMessage,
    peerType = peerType,
    interactionText = extractInteractionText(resources, this),
    isExpanded = isExpanded,
    isArchived = isArchived,
    options = options
)
