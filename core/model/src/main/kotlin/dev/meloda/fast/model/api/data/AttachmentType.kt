package dev.meloda.fast.model.api.data

import android.util.Log

enum class AttachmentType(var value: String) {
    UNKNOWN("unknown"),
    PHOTO("photo"),
    VIDEO("video"),
    FILE("doc"),
    AUDIO("audio"),
    LINK("link"),
    AUDIO_MESSAGE("audio_message"),
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
    STORY("story"),
    WIDGET("widget"),
    ARTIST("artist"),
    AUDIO_PLAYLIST("audio_playlist"),
    PODCAST("podcast"),
    NARRATIVE("narrative"),
    ARTICLE("article"),
    VIDEO_MESSAGE("video_message"),
    GROUP_CHAT_STICKER("ugc_sticker"),
    STICKER_PACK_PREVIEW("sticker_pack_preview")
    ;

    fun isMultiple(): Boolean = this in listOf(PHOTO, VIDEO, AUDIO, FILE)

    companion object {
        fun parse(value: String): AttachmentType {
            val parsedValue = entries.firstOrNull {
                it.value == value
            } ?: UNKNOWN

            if (parsedValue == UNKNOWN) {
                Log.e("AttachmentType", "Unknown attachment type: $value")
            }

            return parsedValue
        }
    }
}
