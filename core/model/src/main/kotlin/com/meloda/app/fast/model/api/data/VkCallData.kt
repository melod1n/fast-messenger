package dev.meloda.fast.model.api.data

import dev.meloda.fast.model.api.domain.VkCallDomain
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkCallData(
    @Json(name = "initiator_id") val initiatorId: Int,
    @Json(name = "receiver_id") val receiverId: Int,
    @Json(name = "state") val state: String,
    @Json(name = "time") val time: Int,
    @Json(name = "duration") val duration: Int,
    @Json(name = "video") val video: Boolean
) {

    fun toDomain() = VkCallDomain(
        initiatorId = initiatorId,
        receiverId = receiverId,
        state = state,
        time = time,
        duration = duration,
        isVideo = video
    )
}
