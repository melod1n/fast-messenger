package dev.meloda.fast.model.api.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import dev.meloda.fast.model.api.domain.VkFileDomain

@JsonClass(generateAdapter = true)
data class VkFileData(
    @Json(name = "id") val id: Long,
    @Json(name = "owner_id") val ownerId: Long,
    @Json(name = "title") val title: String,
    @Json(name = "size") val size: Int,
    @Json(name = "ext") val extension: String,
    @Json(name = "date") val date: Int,
    @Json(name = "type") val type: Int,
    @Json(name = "url") val url: String,
    @Json(name = "preview") val preview: Preview?,
    @Json(name = "ic_licensed") val isLicensed: Int?,
    @Json(name = "access_key") val accessKey: String?,
    @Json(name = "web_preview_url") val webPreviewUrl: String?
) : VkAttachmentData {

    @JsonClass(generateAdapter = true)
    data class Preview(
        val photo: Photo?,
        val video: Video?
    ) {

        @JsonClass(generateAdapter = true)
        data class Photo(
            val sizes: List<Size>
        ) {

            @JsonClass(generateAdapter = true)
            data class Size(
                val height: Int,
                val width: Int,
                val type: String,
                val src: String
            )
        }

        @JsonClass(generateAdapter = true)
        data class Video(
            @Json(name = "src") val src: String,
            @Json(name = "width") val width: Int,
            @Json(name = "height") val height: Int,
            @Json(name = "file_size") val fileSize: Int
        )
    }

    fun toDomain() = VkFileDomain(
        id = id,
        ownerId = ownerId,
        title = title,
        ext = extension,
        url = url,
        size = size,
        accessKey = accessKey,
        preview = preview
    )
}
