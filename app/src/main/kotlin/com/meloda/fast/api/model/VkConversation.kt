package com.meloda.fast.api.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class VkConversation(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val title: String?,
) {
    @Ignore
    var lastMessage: VkMessage? = null
}
