package com.meloda.fast.api.model.base.attachments

import android.os.Parcelable
import android.util.Log
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BaseVkAttachmentItem(
    val type: String,
    val photo: BaseVkPhoto?,
    val video: BaseVkVideo?,
    val audio: BaseVkAudio?,
    @Json(name = "doc")
    val file: BaseVkFile?,
    val link: BaseVkLink?,
    @Json(name = "mini_app")
    val miniApp: BaseVkMiniApp?,
    @Json(name = "audio_message")
    val voiceMessage: BaseVkVoiceMessage?,
    val sticker: BaseVkSticker?,
    val gift: BaseVkGift?,
    val wall: BaseVkWall?,
    val graffiti: BaseVkGraffiti?,
    val poll: BaseVkPoll?,
    @Json(name = "wall_reply")
    val wallReply: BaseVkWallReply?,
    val call: BaseVkCall?,
    @Json(name = "group_call_in_progress")
    val groupCall: BaseVkGroupCall?,
    val curator: BaseVkCurator?,
    val event: BaseVkEvent?,
    val story: BaseVkStory?,
    val widget: BaseVkWidget?
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

abstract class BaseVkAttachment
