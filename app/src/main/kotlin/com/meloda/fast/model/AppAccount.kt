package com.meloda.fast.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "accounts")
@Parcelize
data class AppAccount(
    @PrimaryKey(autoGenerate = false)
    val userId: Int,
    val accessToken: String,
    val fastToken: String?
) : Parcelable
