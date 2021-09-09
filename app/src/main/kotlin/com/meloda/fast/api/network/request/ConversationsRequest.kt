package com.meloda.fast.api.network.request

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ConversationsGetRequest(
    val count: Int? = null,
    val offset: Int? = null,
    val fields: String = "",
    val filter: String = "all",
    val extended: Boolean? = true,
    @SerializedName("start_message_id")
    val startMessageId: Int? = null
) : Parcelable