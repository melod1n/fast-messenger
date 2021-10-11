package com.meloda.fast.api.network.users

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UsersGetRequest(
    val usersIds: List<Int>? = null,
    val fields: String? = null,
    val nomCase: String? = null
) : Parcelable {

    val map
        get() = mutableMapOf<String, String>()
            .apply {
                usersIds?.let { this["user_ids"] = it.joinToString { id -> id.toString() } }
                fields?.let { this["fields"] = it }
                nomCase?.let { this["nom_case"] = it }
            }

}