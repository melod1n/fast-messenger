package com.meloda.fast.api.model.base.attachments

import android.os.Parcelable
import com.meloda.fast.api.model.attachments.VkFile
import kotlinx.parcelize.Parcelize

@Parcelize
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
    val access_key: String,
    val web_preview_url: String?
) : BaseVkAttachment() {

    fun asVkFile() = VkFile(
        id = id,
        title = title,
        ext = ext,
        url = url,
        size = size
    )

    @Parcelize
    data class Preview(
        val photo: Photo?,
        val video: Video?
    ) : Parcelable {

        @Parcelize
        data class Photo(val sizes: List<Size>) : Parcelable {

            @Parcelize
            data class Size(
                val height: Int,
                val width: Int,
                val type: String,
                val src: String
            ) : Parcelable

        }

        @Parcelize
        data class Video(
            val src: String,
            val width: Int,
            val height: Int,
            val file_size: Int
        ) : Parcelable

    }

}