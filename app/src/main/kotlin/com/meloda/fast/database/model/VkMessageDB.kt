package com.meloda.fast.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class VkMessageDB(
    @PrimaryKey val id: Int,
    val text: String?,
    val isOut: Boolean,
    val peerId: Int,
    val fromId: Int,
    val date: Int,
    val randomId: Int,
    val action: String?,
    val actionMemberId: Int?,
    val actionText: String?,
    val actionConversationMessageId: Int?,
    val actionMessage: String?,
    val updateTime: Int?,
    val important: Boolean,
    val forwardIds: String?,
    val attachments: String?, // TODO: 01/05/2024, Danil Nikolaev: how to store???
    val replyMessageId: Int?,
    val geoType: String?
)
