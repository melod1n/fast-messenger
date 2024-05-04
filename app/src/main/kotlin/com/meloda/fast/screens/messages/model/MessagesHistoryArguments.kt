package com.meloda.fast.screens.messages.model

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
