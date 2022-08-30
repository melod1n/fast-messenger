package com.meloda.fast.api.network.videos

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class VideosSaveResponse(
    @SerializedName("access_key")
    val accessKey: String,
    val description: String,
    @SerializedName("owner_id")
    val ownerId: Int,
    val title: String,
    @SerializedName("upload_url")
    val uploadUrl: String,
    @SerializedName("video_id")
    val videoId: Int
) : Parcelable {

}

@Parcelize
data class VideosUploadResponse(
    @SerializedName("video_hash")
    val hash: String?,
    val size: Int,
    @SerializedName("direct_link")
    val directLink: String,
    @SerializedName("owner_id")
    val ownerId: Int,
    @SerializedName("video_id")
    val videoId: Int,
    val error: String?
) : Parcelable