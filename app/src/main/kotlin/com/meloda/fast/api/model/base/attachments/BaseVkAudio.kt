package com.meloda.fast.api.model.base.attachments

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkAudio(
    val id: Int,
    val title: String,
    val artist: String,
    val duration: Int,
    val url: String,
    val date: Int,
    @SerializedName("owner_id")
    val ownerId: Int,
    @SerializedName("access_key")
    val accessKey: String,
    @SerializedName("is_explicit")
    val isExplicit: Boolean,
    @SerializedName("is_focus_track")
    val isFocusTrack: Boolean,
    @SerializedName("is_licensed")
    val isLicensed: Boolean,
    @SerializedName("track_code")
    val trackCode: String,
    @SerializedName("genre_id")
    val genreId: Int,
    val album: Album,
    @SerializedName("short_videos_allowed")
    val shortVideosAllowed: Boolean,
    @SerializedName("stories_allowed")
    val storiesAllowed: Boolean,
    @SerializedName("stories_cover_allowed")
    val storiesCoverAllowed: Boolean
) : BaseVkAttachment() {

    @Parcelize
    data class Album(
        val id: Int,
        val title: String,
        @SerializedName("owner_id")
        val ownerId: Int,
        @SerializedName("access_key")
        val accessKey: String,
        val thumb: Thumb
    ) : Parcelable {

        @Parcelize
        data class Thumb(
            val width: Int,
            val height: Int,
            @SerializedName("photo_34")
            val photo34: String,
            @SerializedName("photo_68")
            val photo68: String,
            @SerializedName("photo_135")
            val photo135: String,
            @SerializedName("photo_270")
            val photo270: String,
            @SerializedName("photo_300")
            val photo300: String,
            @SerializedName("photo_600")
            val photo600: String,
            @SerializedName("photo_1200")
            val photo1200: String
        ) : Parcelable

    }

}