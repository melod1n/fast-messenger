package com.meloda.fast.api.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class VkConversation(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val title: String?,
    val photo200: String?,
    val type: String,
    val callInProgress: Boolean
) {
    @Ignore
    var lastMessage: VkMessage? = null

    fun isChat() = type == "chat"
    fun isUser() = type == "user"
    fun isGroup() = type == "group"

}
