package com.meloda.app.fast.model.api;

enum class PeerType(val value: String) {
    USER("user"),
    GROUP("group"),
    CHAT("chat");

    fun isUser(): Boolean = this == USER
    fun isGroup(): Boolean = this == GROUP
    fun isChat(): Boolean = this == CHAT

    companion object {
        fun parse(type: String): PeerType {
            return entries.first { it.value == type }
        }
    }
}
