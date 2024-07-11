package com.meloda.app.fast.model.api.data

import com.meloda.app.fast.model.api.domain.VkAttachment
import com.meloda.app.fast.model.api.domain.VkUnknownAttachment
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkAttachmentItemData(
    @Json(name = "type") val type: String,
    @Json(name = "photo") val photo: VkPhotoData?,
    @Json(name = "video") val video: VkVideoData?,
    @Json(name = "audio") val audio: VkAudioData?,
    @Json(name = "doc") val file: VkFileData?,
    @Json(name = "link") val link: VkLinkData?,
    @Json(name = "mini_app") val miniApp: VkMiniAppData?,
    @Json(name = "audio_message") val audioMessage: VkAudioMessageData?,
    @Json(name = "sticker") val sticker: VkStickerData?,
    @Json(name = "gift") val gift: VkGiftData?,
    @Json(name = "wall") val wall: VkWallData?,
    @Json(name = "graffiti") val graffiti: VkGraffitiData?,
    @Json(name = "poll") val poll: VkPollData?,
    @Json(name = "wall_reply") val wallReply: VkWallReplyData?,
    @Json(name = "call") val call: VkCallData?,
    @Json(name = "group_call_in_progress") val groupCallInProgress: VkGroupCallData?,
    @Json(name = "curator") val curator: VkCuratorData?,
    @Json(name = "event") val event: VkEventData?,
    @Json(name = "story") val story: VkStoryData?,
    @Json(name = "widget") val widget: VkWidgetData?,
    @Json(name = "artist") val artist: VkArtistData?,
    @Json(name = "audios") val audios: List<VkAudioData>?,
    @Json(name = "audio_playlist") val audioPlaylist: VkAudioPlaylistData?,
    @Json(name = "podcast") val podcast: VkPodcastData?,
    @Json(name = "narrative") val narrative: VkNarrativeData?
) {
    fun toDomain(): VkAttachment = when (AttachmentType.parse(type)) {
        AttachmentType.UNKNOWN -> VkUnknownAttachment
        AttachmentType.PHOTO -> photo?.toDomain()
        AttachmentType.VIDEO -> video?.toDomain()
        AttachmentType.AUDIO -> audio?.toDomain()
        AttachmentType.FILE -> file?.toDomain()
        AttachmentType.LINK -> link?.toDomain()
        AttachmentType.MINI_APP -> miniApp?.toDomain()
        AttachmentType.AUDIO_MESSAGE -> audioMessage?.toDomain()
        AttachmentType.STICKER -> sticker?.toDomain()
        AttachmentType.GIFT -> gift?.toDomain()
        AttachmentType.WALL -> wall?.toDomain()
        AttachmentType.GRAFFITI -> graffiti?.toDomain()
        AttachmentType.POLL -> poll?.toDomain()
        AttachmentType.WALL_REPLY -> wallReply?.toDomain()
        AttachmentType.CALL -> call?.toDomain()
        AttachmentType.GROUP_CALL_IN_PROGRESS -> groupCallInProgress?.toDomain()
        AttachmentType.CURATOR -> curator?.toDomain()
        AttachmentType.EVENT -> event?.toDomain()
        AttachmentType.STORY -> story?.toDomain()
        AttachmentType.WIDGET -> widget?.toDomain()
        AttachmentType.ARTIST -> artist?.toDomain()
        AttachmentType.AUDIO_PLAYLIST -> audioPlaylist?.toDomain()
        AttachmentType.PODCAST -> podcast?.toDomain()
        AttachmentType.NARRATIVE -> narrative?.toDomain()
    } ?: VkUnknownAttachment
}
