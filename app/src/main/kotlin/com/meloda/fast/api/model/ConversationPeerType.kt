package com.meloda.fast.api.model

enum class ConversationPeerType(val value: String) {
    USER("user"), GROUP("group"), CHAT("chat");

    fun isUser(): Boolean = this == USER
    fun isGroup(): Boolean = this == GROUP
    fun isChat(): Boolean = this == CHAT

    companion object {
        fun parse(type: String): ConversationPeerType {
            return entries.first { it.value == type }
        }
    }
}
