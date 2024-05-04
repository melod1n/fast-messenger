package com.meloda.fast.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class VkConversationDB(
    @PrimaryKey val id: Int,
    val localId: Int,
    val ownerId: Int?,
    val title: String?,
    val photo50: String?,
    val photo100: String?,
    val photo200: String?,
    val isPhantom: Boolean,
    val lastConversationMessageId: Int,
    val inReadCmId: Int,
    val outReadCmId: Int,
    val inRead: Int,
    val outRead: Int,
    val lastMessageId: Int?,
    val unreadCount: Int,
    val membersCount: Int?,
    val canChangePin: Boolean,
    val canChangeInfo: Boolean,
    val majorId: Int,
    val minorId: Int,
    val pinnedMessageId: Int?,
    val peerType: String
)
