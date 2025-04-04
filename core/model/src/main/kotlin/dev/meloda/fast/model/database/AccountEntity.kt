package dev.meloda.fast.model.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = false)
    val userId: Long,
    val accessToken: String,
    val fastToken: String?,
    val trustedHash: String?,
    val exchangeToken: String?
)
