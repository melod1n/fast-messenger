package dev.meloda.fast.model.api.data

import dev.meloda.fast.model.api.domain.VkCuratorDomain
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkCuratorData(
    val id: Long,
    val name: String,
    val description: String,
    val url: String,
    val photo: List<Photo>
) : VkAttachmentData {

    @JsonClass(generateAdapter = true)
    data class Photo(
        val height: Int,
        val url: String,
        val width: String
    )

    fun toDomain() = VkCuratorDomain(
        id = id
    )
}
