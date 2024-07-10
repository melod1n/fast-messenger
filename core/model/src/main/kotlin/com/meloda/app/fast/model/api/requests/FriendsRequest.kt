package com.meloda.app.fast.model.api.requests

data class GetFriendsRequest(
    val order: String?,
    val count: Int?,
    val offset: Int?,
    val fields: String?
) {

    val map
        get() = mutableMapOf<String, String>()
            .apply {
                order?.let { this["order"] = it }
                count?.let { this["count"] = it.toString() }
                offset?.let { this["offset"] = it.toString() }
                fields?.let { this["fields"] = it }
            }
}
