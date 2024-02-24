package com.meloda.fast.api.model.base.attachments

import android.os.Parcelable
import com.meloda.fast.api.model.attachments.VkAudio
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
data class BaseVkAudio(
    val id: Int,
    val title: String,
    val artist: String,
    val duration: Int,
    val url: String,
    val date: Int,
    val owner_id: Int,
    val access_key: String?,
    val is_explicit: Boolean,
    val is_focus_track: Boolean,
    val is_licensed: Boolean,
    val track_code: String,
    val genre_id: Int,
    val album: Album,
    val short_videos_allowed: Boolean,
    val stories_allowed: Boolean,
    val stories_cover_allowed: Boolean
) : BaseVkAttachment() {

    fun asVkAudio() = VkAudio(
        id = id,
        ownerId = owner_id,
        title = title,
        artist = artist,
        url = url,
        duration = duration,
        accessKey = access_key
    )

    @JsonClass(generateAdapter = true)
    data class Album(
        val id: Int,
        val title: String,
        val owner_id: Int,
        val access_key: String,
        val thumb: Thumb
    ) {

        @JsonClass(generateAdapter = true)
        data class Thumb(
            val width: Int,
            val height: Int,
            val photo_34: String,
            val photo_68: String,
            val photo_135: String,
            val photo_270: String,
            val photo_300: String,
            val photo_600: String,
            val photo_1200: String
        )
    }
}
