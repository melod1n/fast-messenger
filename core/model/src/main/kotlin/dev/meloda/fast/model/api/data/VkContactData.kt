package dev.meloda.fast.model.api.data

import dev.meloda.fast.model.api.domain.VkContactDomain
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkContactData(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "can_write") val canWrite: Boolean,
    @Json(name = "user_id") val userId: Int,
    @Json(name = "last_seen_status") val lastSeenStatus: String?,
    @Json(name = "photo_50") val photo50: String?,
    @Json(name = "calls_id") val callsId: String
) {

    fun mapToDomain(): VkContactDomain = VkContactDomain(
        name = name,
        userId = userId
    )
}
