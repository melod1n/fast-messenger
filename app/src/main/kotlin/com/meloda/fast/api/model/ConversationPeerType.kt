package com.meloda.fast.api.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class ConversationPeerType : Parcelable {
    object User : ConversationPeerType()
    object Group : ConversationPeerType()
    object Chat : ConversationPeerType()

    fun isUser() = this == User
    fun isGroup() = this == Group
    fun isChat() = this == Chat

    companion object {
        fun parse(type: String): ConversationPeerType {
            return when (type) {
                "user" -> User
                "group" -> Group
                else -> Chat
            }
        }
    }
}
