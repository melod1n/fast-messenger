package com.meloda.app.fast.model.api.requests

data class UsersGetRequest(
    val userIds: List<Int>? = null,
    val fields: String? = null,
    val nomCase: String? = null
) {

    val map
        get() = mutableMapOf<String, String>()
            .apply {
                userIds?.let { this["user_ids"] = it.joinToString(",") }
                fields?.let { this["fields"] = it }
                nomCase?.let { this["nom_case"] = it }
            }
}
