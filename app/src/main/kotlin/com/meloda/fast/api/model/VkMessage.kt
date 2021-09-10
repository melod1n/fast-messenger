package com.meloda.fast.api.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class VkMessage(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val text: String?,
    val isOut: Boolean,
    val peerId: Int,
    val fromId: Int,
    val date: Int
) {



}
