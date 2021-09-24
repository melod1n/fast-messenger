package com.meloda.fast.api.model.base.attachments

import android.os.Parcelable
import android.util.Log
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkAttachmentItem(
    val type: String,
    val photo: BaseVkPhoto?,
    val video: BaseVkVideo?,
    val audio: BaseVkAudio?,
    @SerializedName("doc")
    val file: BaseVkFile?,
    val link: BaseVkLink?,
    @SerializedName("mini_app")
    val miniApp: BaseVkMiniApp?,
    @SerializedName("audio_message")
    val voiceMessage: BaseVkVoiceMessage?,
    val sticker: BaseVkSticker?,
    val gift: BaseVkGift?,
    val wall: BaseVkWall?,
    val graffiti: BaseVkGraffiti?,
    val poll: BaseVkPoll?,
    @SerializedName("wall_reply")
    val wallReply: BaseVkWallReply?,
    val call: BaseVkCall?,
    @SerializedName("group_call_in_progress")
    val groupCall: BaseVkGroupCall?,
    val curator: BaseVkCurator?,
    val event: BaseVkEvent?,
    val story: BaseVkStory?
) : Parcelable {

    fun getPreparedType() = AttachmentType.parse(type)

    enum class AttachmentType(var value: String) {
        UNKNOWN("unknown"),
        PHOTO("photo"),
        VIDEO("video"),
        AUDIO("audio"),
        FILE("doc"),
        LINK("link"),
        VOICE("audio_message"),
        MINI_APP("mini_app"),
        STICKER("sticker"),
        GIFT("gift"),
        WALL("wall"),
        GRAFFITI("graffiti"),
        POLL("poll"),
        WALL_REPLY("wall_reply"),
        CALL("call"),
        GROUP_CALL_IN_PROGRESS("group_call_in_progress"),
        CURATOR("curator"),
        EVENT("event"),
        STORY("story")
        ;

        companion object {
            fun parse(value: String): AttachmentType? {
                val parsedValue = values().firstOrNull { it.value == value } ?: UNKNOWN

                if (parsedValue == UNKNOWN) {
                    Log.e("AttachmentType", "Unknown attachment type: $value")
                }

                return parsedValue
            }
        }
    }

}

abstract class BaseVkAttachment : Parcelable