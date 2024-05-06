package com.meloda.app.fast.messageshistory.model

import com.meloda.app.fast.common.UiImage
import java.io.Serializable

data class MessagesHistoryArguments(
    val conversationId: Int,
    val title: String,
    val status: String?,
    val avatar: Avatar
) : Serializable

sealed class Avatar : Serializable {
    data object Empty : Avatar()
    data object Favorites : Avatar()
    data class Url(val url: String) : Avatar()
}

fun UiImage.mapToAvatar(): Avatar = when (this) {
    is UiImage.Url -> Avatar.Url(url = url)
    else -> Avatar.Empty
}
