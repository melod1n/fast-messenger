package dev.meloda.fast.model.api.requests

data class ConversationsGetRequest(
    val count: Int? = null,
    val offset: Int? = null,
    val fields: String = "",
    val filter: String = "all",
    val extended: Boolean? = true,
    val startMessageId: Long? = null
) {

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

data class ConversationsDeleteRequest(val peerId: Long) {
    val map get() = mapOf("peer_id" to peerId.toString())
}

data class ConversationsPinRequest(val peerId: Long) {
    val map get() = mapOf("peer_id" to peerId.toString())
}

data class ConversationsUnpinRequest(val peerId: Long) {
    val map get() = mapOf("peer_id" to peerId.toString())
}
