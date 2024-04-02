package com.meloda.fast.api.model.data

import com.meloda.fast.api.model.domain.VkCuratorDomain
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkCuratorData(
    val id: Int,
    val name: String,
    val description: String,
    val url: String,
    val photo: List<Photo>
) : VkAttachmentData {

    fun toDomain() = VkCuratorDomain(
        id = id
    )

    @JsonClass(generateAdapter = true)
    data class Photo(
        val height: Int,
        val url: String,
        val width: String
    )
}
