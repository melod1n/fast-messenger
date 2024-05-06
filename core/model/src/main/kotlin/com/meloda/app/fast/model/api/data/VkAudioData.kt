package com.meloda.app.fast.model.api.data

import com.meloda.app.fast.model.api.domain.VkAudioDomain
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkAudioData(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String,
    @Json(name = "artist") val artist: String,
    @Json(name = "duration") val duration: Int,
    @Json(name = "url") val url: String,
    @Json(name = "date") val date: Int,
    @Json(name = "owner_id") val ownerId: Int,
    @Json(name = "access_key") val accessKey: String?,
    @Json(name = "is_explicit") val isExplicit: Boolean,
    @Json(name = "is_focus_track") val isFocusTrack: Boolean,
    @Json(name = "is_licensed") val isLicensed: Boolean?,
    @Json(name = "genre_id") val genreId: Int?,
    @Json(name = "album") val album: Album?,
) : VkAttachmentData {

    @JsonClass(generateAdapter = true)
    data class Album(
        @Json(name = "id") val id: Int,
        @Json(name = "title") val title: String,
        @Json(name = "owner_id") val ownerId: Int,
        @Json(name = "access_key") val accessKey: String,
        @Json(name = "thumb") val thumb: Thumb
    ) {

        @JsonClass(generateAdapter = true)
        data class Thumb(
            @Json(name = "width") val width: Int,
            @Json(name = "height") val height: Int,
            @Json(name = "photo_34") val photo34: String?,
            @Json(name = "photo_68") val photo68: String?,
            @Json(name = "photo_135") val photo135: String?,
            @Json(name = "photo_270") val photo270: String?,
            @Json(name = "photo_300") val photo300: String?,
            @Json(name = "photo_600") val photo600: String?,
            @Json(name = "photo_1200") val photo1200: String?
        )
    }

    fun toDomain() = VkAudioDomain(
        id = id,
        ownerId = ownerId,
        title = title,
        artist = artist,
        url = url,
        duration = duration,
        accessKey = accessKey
    )
}
