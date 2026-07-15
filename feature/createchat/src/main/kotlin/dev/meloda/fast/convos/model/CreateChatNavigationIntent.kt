package dev.meloda.fast.convos.model

sealed class CreateChatNavigationIntent {
    data object Back : CreateChatNavigationIntent()
    data class ToNewChat(val id: Long) : CreateChatNavigationIntent()
}
