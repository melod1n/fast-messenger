package com.meloda.fast.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UpdateItem(
    val version: String,
    val link: String
) : Parcelable {

    override fun toString(): String {
        return "UpdateItem {version: $version; link: $link}"
    }

}