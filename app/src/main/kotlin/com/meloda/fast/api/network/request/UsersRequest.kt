package com.meloda.fast.api.network.request

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UsersGetRequest(
    val usersIds: List<Int>,
    val fields: String? = null,
    val nomCase: String? = null
) : Parcelable {

    val map
        get() = mutableMapOf(
            "user_ids" to usersIds.joinToString { it.toString() }
        ).apply {
            fields?.let { this["fields"] = it }
            nomCase?.let { this["nom_case"] = it }
        }

}