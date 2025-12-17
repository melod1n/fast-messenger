package dev.meloda.fast.model.api

import dev.meloda.fast.model.api.domain.VkMessage

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

        fun VkMessage.getPeerType(): PeerType {
            return when {
                peerId > 2_000_000_000 -> CHAT
                peerId > 0 -> USER
                peerId < 0 -> GROUP
                else -> throw IllegalArgumentException("Unknown peer type for peerId: 0")
            }
        }
    }
}
