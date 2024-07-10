package com.meloda.app.fast.friends.model

import com.meloda.app.fast.common.UiImage
import com.meloda.app.fast.model.api.domain.OnlineStatus

data class UiFriend(
    val userId: Int,
    val avatar: UiImage?,
    val title: String,
    val onlineStatus: OnlineStatus
)
