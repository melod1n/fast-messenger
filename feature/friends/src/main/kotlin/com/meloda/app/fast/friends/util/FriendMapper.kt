package com.meloda.app.fast.friends.util

import com.meloda.app.fast.common.model.UiImage
import com.meloda.app.fast.data.VkMemoryCache
import com.meloda.app.fast.friends.model.UiFriend
import com.meloda.app.fast.model.api.domain.VkUser

fun VkUser.asPresentation(
    useContactNames: Boolean = false
): UiFriend = UiFriend(
    userId = id,
    avatar = photo100?.let(UiImage::Url),
    title = if (useContactNames) {
        VkMemoryCache.getContact(id)?.name ?: fullName
    } else {
        fullName
    },
    onlineStatus = onlineStatus
)
