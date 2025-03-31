package dev.meloda.fast.model.api.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import dev.meloda.fast.model.api.domain.VkGroupCallDomain

@JsonClass(generateAdapter = true)
data class VkGroupCallData(
    @Json(name = "initiator_id") val initiatorId: Long,
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
