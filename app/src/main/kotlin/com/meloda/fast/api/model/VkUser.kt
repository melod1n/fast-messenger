package com.meloda.fast.api.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class VkUser(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val firstName: String,
    val lastName: String,
    val online: Boolean,
    val photo200: String?
) {

    override fun toString() = "$firstName $lastName".trim()

}