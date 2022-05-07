package com.meloda.fast.model

import android.os.Parcelable
import com.google.gson.Gson
import kotlinx.parcelize.Parcelize

@Parcelize
data class UpdateItem(
    val version: String,
    val link: String,
    val changelogs: Map<String, String>
) : Parcelable {

    override fun toString(): String {
        return Gson().toJson(this)
    }

}