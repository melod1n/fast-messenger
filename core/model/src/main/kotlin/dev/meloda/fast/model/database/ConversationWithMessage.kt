package dev.meloda.fast.model.database

import androidx.room.Embedded
import androidx.room.Relation

data class ConversationWithMessage(
    @Embedded val conversation: VkConversationEntity,
    @Relation(
        parentColumn = "lastMessageId",
        entityColumn = "id"
    )
    val message: VkMessageEntity?
)
