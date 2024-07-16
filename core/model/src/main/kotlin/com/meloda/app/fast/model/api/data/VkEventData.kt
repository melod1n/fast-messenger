package dev.meloda.fast.model.api.data

import dev.meloda.fast.model.api.domain.VkEventDomain
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkEventData(
    @Json(name = "button_text") val buttonText: String,
    @Json(name = "id") val id: Int,
    @Json(name = "is_favorite") val isFavorite: Boolean,
    @Json(name = "text") val text: String,
    @Json(name = "address") val address: String,
    @Json(name = "friends") val friends: List<Int> = emptyList(),
    @Json(name = "member_status") val memberStatus: Int,
    @Json(name = "time") val time: Int
) : VkAttachmentData {

    fun toDomain() = VkEventDomain(id = id)
}
