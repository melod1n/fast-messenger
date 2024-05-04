package com.meloda.fast.database.model

import androidx.room.Embedded
import androidx.room.Relation

data class ConversationWithMessage(
    @Embedded val conversation: VkConversationDB,
    @Relation(
        parentColumn = "lastMessageId",
        entityColumn = "id"
    )
    val message: VkMessageDB?
)
