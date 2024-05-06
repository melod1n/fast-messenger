package com.meloda.app.fast.model.api.data

import com.meloda.app.fast.model.api.domain.VkCuratorDomain
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkCuratorData(
    val id: Int,
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
