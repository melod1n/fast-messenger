package com.meloda.fast.api.model.base.attachments

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkFile(
    val id: Int,
    @SerializedName("owner_id")
    val ownerId: Int,
    val title: String,
    val size: Int,
    val ext: String,
    val date: Int,
    val type: Int,
    val url: String,
    val preview: Preview?,
    @SerializedName("is_licensed")
    val isLicensed: Int,
    @SerializedName("access_key")
    val accessKey: String,
    @SerializedName("web_preview_url")
    val webPreviewUrl: String?
) : BaseVkAttachment() {

    @Parcelize
    data class Preview(
        val photo: Photo?,
        val video: Video?
    ) : Parcelable {

        @Parcelize
        data class Photo(val sizes: List<Size>) : Parcelable

        @Parcelize
        data class Video(
            val src: String,
            val width: Int,
            val height: Int,
            @SerializedName("file_size")
            val fileSize: Int
        ) : Parcelable

    }

}