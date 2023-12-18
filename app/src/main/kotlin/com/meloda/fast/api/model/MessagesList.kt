package com.meloda.fast.api.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize

@Parcelize
@Immutable
data class MessagesList(
    val messages: List<VkMessage>
) : Parcelable {
    val size: Int get() = messages.size
}
