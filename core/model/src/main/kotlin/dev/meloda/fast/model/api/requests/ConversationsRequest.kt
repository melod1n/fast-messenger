package dev.meloda.fast.model.api.requests

import dev.meloda.fast.model.ConversationsFilter

data class ConversationsGetRequest(
    val count: Int? = null,
    val offset: Int? = null,
    val fields: String = "",
    val filter: ConversationsFilter = ConversationsFilter.ALL,
    val extended: Boolean? = true,
    val startMessageId: Long? = null
) {

    val map
        get() = mutableMapOf(
            "fields" to fields,
            "filter" to filter.toString().lowercase()
        ).apply {
            count?.let { this["count"] = it.toString() }
            offset?.let { this["offset"] = it.toString() }
            extended?.let { this["extended"] = it.toString() }
            startMessageId?.let { this["start_message_id"] = it.toString() }
        }
}
