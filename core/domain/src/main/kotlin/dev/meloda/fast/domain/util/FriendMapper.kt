package dev.meloda.fast.domain.util

import dev.meloda.fast.common.model.UiImage
import dev.meloda.fast.data.VkMemoryCache
import dev.meloda.fast.model.api.domain.VkUser
import dev.meloda.fast.ui.model.api.UiFriend

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
    onlineStatus = onlineStatus,
    photo400Orig = photo400Orig?.let(UiImage::Url),
    firstName = firstName,
    lastName = lastName
)
