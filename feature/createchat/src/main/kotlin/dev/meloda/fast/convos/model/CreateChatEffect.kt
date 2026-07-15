package dev.meloda.fast.convos.model

sealed class CreateChatEffect {
    data class Navigate(val intent: CreateChatNavigationIntent) : CreateChatEffect()
}
