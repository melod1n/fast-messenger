package com.meloda.fast.api.network.request

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UsersGetRequest(
    @SerializedName("user_ids")
    val usersIds: List<Int>,
    val fields: String? = null,
    @SerializedName("nom_case")
    val nomCase: String? = null
) : Parcelable