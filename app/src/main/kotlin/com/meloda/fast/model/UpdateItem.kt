package com.meloda.fast.model

import android.os.Parcelable
import com.google.gson.Gson
import kotlinx.parcelize.Parcelize

@Parcelize
data class UpdateItem(
    val id: Int,
    val versionName: String,
    val versionCode: Int,
    val mandatory: Int,
    val changelog: String?,
    val enabled: Int,
    val fileName: String,
    val date: Long,
    val extension: String,
    val originalName: String,
    val fileSize: Int,
    val preRelease: Int,
    val downloadLink: String
) : Parcelable {

    fun isMandatory(): Boolean = mandatory == 1
    fun isEnabled(): Boolean = enabled == 1
    fun isPreRelease(): Boolean = preRelease == 1

    override fun toString(): String {
        return Gson().toJson(this)
    }

}

@Parcelize
data class UpdateActualUrl(val url: String) : Parcelable