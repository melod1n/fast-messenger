package dev.meloda.fast.ui.model.api

import androidx.compose.runtime.Immutable
import dev.meloda.fast.common.model.UiImage
import dev.meloda.fast.model.api.domain.OnlineStatus

@Immutable
data class UiFriend(
    val userId: Int,
    val avatar: UiImage?,
    val firstName: String,
    val lastName: String,
    val title: String,
    val onlineStatus: OnlineStatus,
    val photo400Orig: UiImage?
)
