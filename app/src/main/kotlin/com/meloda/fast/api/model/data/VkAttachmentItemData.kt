package com.meloda.fast.api.model.data

import android.util.Log
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
    @Json(name = "audio_message") val voiceMessage: VkAudioMessageData?,
    @Json(name = "sticker") val sticker: VkStickerData?,
    @Json(name = "gift") val gift: VkGiftData?,
    @Json(name = "wall") val wall: VkWallData?,
    @Json(name = "graffiti") val graffiti: VkGraffitiData?,
    @Json(name = "poll") val poll: VkPollData?,
    @Json(name = "wall_reply") val wallReply: VkWallReplyData?,
    @Json(name = "call") val call: VkCallData?,
    @Json(name = "group_call_in_progress") val groupCall: VkGroupCallData?,
    @Json(name = "curator") val curator: VkCuratorData?,
    @Json(name = "event") val event: VkEventData?,
    @Json(name = "story") val story: VkStoryData?,
    @Json(name = "widget") val widget: VkWidgetData?
) {

    fun getPreparedType() = AttachmentType.parse(type)

    enum class AttachmentType(var value: String) {
        Unknown("unknown"),
        Photo("photo"),
        Video("video"),
        Audio("audio"),
        File("doc"),
        Link("link"),
        Voice("audio_message"),
        MiniApp("mini_app"),
        Sticker("sticker"),
        Gift("gift"),
        Wall("wall"),
        Graffiti("graffiti"),
        Poll("poll"),
        WallReply("wall_reply"),
        Call("call"),
        GroupCallInProgress("group_call_in_progress"),
        Curator("curator"),
        Event("event"),
        Story("story"),
        Widget("widget")
        ;

        companion object {
            fun parse(value: String): AttachmentType {
                val parsedValue = entries.firstOrNull { it.value == value } ?: Unknown

                if (parsedValue == Unknown) {
                    Log.e("AttachmentType", "Unknown attachment type: $value")
                }

                return parsedValue
            }
        }
    }

}
