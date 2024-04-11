package com.meloda.fast.api.model.data

import com.meloda.fast.api.model.domain.VkLinkDomain
import com.meloda.fast.ext.isTrue
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkLinkData(
    @Json(name = "url") val url: String,
    @Json(name = "title") val title: String?,
    @Json(name = "caption") val caption: String?,
    @Json(name = "photo") val photo: VkPhotoData?,
    @Json(name = "target") val target: String?,
    @Json(name = "is_favorite") val isFavorite: Boolean?
) : VkAttachmentData {

    fun toDomain() = VkLinkDomain(
        url = url,
        title = title,
        caption = caption,
        photo = photo?.toDomain(),
        target = target,
        isFavorite = isFavorite.isTrue
    )
}
