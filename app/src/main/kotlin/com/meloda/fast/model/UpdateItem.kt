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
    val downloadLink: String,
) : Parcelable {

    fun isMandatory(): Boolean = mandatory == 1
    fun isEnabled(): Boolean = enabled == 1
    fun isPreRelease(): Boolean = preRelease == 1

    override fun toString(): String {
        return Gson().toJson(this)
    }

    companion object {
        val EMPTY = UpdateItem(
            id = 0,
            versionName = "1.0.0",
            versionCode = 2,
            mandatory = 1,
            changelog = "Changelog",
            enabled = 1,
            fileName = "",
            date = 0,
            extension = "",
            originalName = "",
            fileSize = 0,
            preRelease = 0,
            downloadLink = "https://c4.kemono.party/data/98/8c/988cf166f1ee9cd318e2407e6bfbabf60bffa53ed229ea0b2434009f1598e039.png?f=JessieGym002b4pt.png"
        )
    }

}

@Parcelize
data class UpdateActualUrl(val url: String) : Parcelable
