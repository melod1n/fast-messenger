package com.meloda.fast.api.model.request

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ConversationsGetRequest(
    val count: Int? = null,
    val offset: Int? = null,
    val fields: String = "",
    val filter: String = "all",
    val extended: Boolean? = true,
    val startMessageId: Int? = null
) : Parcelable {

    val map
        get() = mutableMapOf(
            "fields" to fields,
            "filter" to filter
        ).apply {
            count?.let { this["count"] = it.toString() }
            offset?.let { this["offset"] = it.toString() }
            extended?.let { this["extended"] = it.toString() }
            startMessageId?.let { this["start_message_id"] = it.toString() }
        }
}