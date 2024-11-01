package dev.meloda.fast.friends.model

import dev.meloda.fast.common.model.UiImage
import dev.meloda.fast.model.api.domain.OnlineStatus

data class UiFriend(
    val userId: Int,
    val avatar: UiImage?,
    val title: String,
    val onlineStatus: OnlineStatus,
    val photo400Orig: UiImage?
)
