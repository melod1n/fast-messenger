package com.meloda.fast.api.model.base.attachments

import android.os.Parcelable
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
    val call: BaseVkCall?
) : Parcelable {

    fun getPreparedType() = AttachmentType.parse(type)

    enum class AttachmentType(val value: String) {
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
        CALL("call")
        ;

        companion object {
            fun parse(value: String) = values().firstOrNull { it.value == value }
        }
    }

}

abstract class BaseVkAttachment : Parcelable