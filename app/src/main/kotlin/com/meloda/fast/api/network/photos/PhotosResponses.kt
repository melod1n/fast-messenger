package com.meloda.fast.api.network.photos

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class PhotosGetMessagesUploadServerResponse(
    @SerializedName("album_id")
    val albumId: Int,
    @SerializedName("upload_url")
    val uploadUrl: String
) : Parcelable

@Parcelize
data class PhotosUploadPhotoResponse(
    val server: Int, val photo: String, val hash: String
) : Parcelable