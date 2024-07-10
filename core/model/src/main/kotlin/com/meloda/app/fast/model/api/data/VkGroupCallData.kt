package com.meloda.app.fast.model.api.data

import com.meloda.app.fast.model.api.domain.VkGroupCallDomain
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkGroupCallData(
    @Json(name = "initiator_id") val initiatorId: Int,
    @Json(name = "join_link") val joinLink: String,
    @Json(name = "participants") val participants: Participants
) {

    @JsonClass(generateAdapter = true)
    data class Participants(
        val list: List<Int>,
        val count: Int
    )

    fun toDomain() = VkGroupCallDomain(initiatorId = initiatorId)
}
