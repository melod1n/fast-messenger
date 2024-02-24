package com.meloda.fast.api.model.base.attachments

import android.os.Parcelable
import com.meloda.fast.api.model.attachments.VkFile
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
data class BaseVkFile(
    val id: Int,
    val owner_id: Int,
    val title: String,
    val size: Int,
    val ext: String,
    val date: Int,
    val type: Int,
    val url: String,
    val preview: Preview?,
    val ic_licensed: Int,
    val access_key: String?,
    val web_preview_url: String?
) : BaseVkAttachment() {

    fun asVkFile() = VkFile(
        id = id,
        ownerId = owner_id,
        title = title,
        ext = ext,
        url = url,
        size = size,
        accessKey = access_key,
        preview = preview
    )

    @JsonClass(generateAdapter = true)
    data class Preview(
        val photo: Photo?,
        val video: Video?
    )  {

        @JsonClass(generateAdapter = true)
        data class Photo(val sizes: List<Size>)  {

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
            val src: String,
            val width: Int,
            val height: Int,
            val file_size: Int
        )
    }
}
