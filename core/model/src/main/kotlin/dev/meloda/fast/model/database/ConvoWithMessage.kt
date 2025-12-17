package dev.meloda.fast.model.database

import androidx.room.Embedded
import androidx.room.Relation

data class ConvoWithMessage(
    @Embedded val convo: VkConvoEntity,
    @Relation(
        parentColumn = "lastMessageId",
        entityColumn = "id"
    )
    val message: VkMessageEntity?
)
