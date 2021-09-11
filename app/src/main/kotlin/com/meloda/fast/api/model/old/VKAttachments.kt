package com.meloda.fast.api.model.old

import org.json.JSONArray
import java.util.*

object VKAttachments {

    fun parse(array: JSONArray): ArrayList<VKModel> {
        val attachments = ArrayList<VKModel>(array.length())

        for (i in 0 until array.length()) {
            var attachment = array.optJSONObject(i) ?: continue
            if (attachment.has("attachment")) {
                attachment = attachment.optJSONObject("attachment") ?: continue
            }

            val type = Type.fromString(attachment.optString("type"))
            val jsonObject = attachment.optJSONObject(type.value) ?: continue

            when (type) {
//                Type.PHOTO -> attachments.add(oldVKPhoto(jsonObject))
//                Type.AUDIO -> attachments.add(oldVKAudio(jsonObject))
//                Type.VIDEO -> attachments.add(oldVKVideo(jsonObject))
//                Type.DOCUMENT -> attachments.add(oldVKDocument(jsonObject))
//                Type.STICKER -> attachments.add(oldVKSticker(jsonObject))
//                Type.LINK -> attachments.add(oldVKLink(jsonObject))
//                Type.GIFT -> attachments.add(VKGift(jsonObject))
//                Type.VOICE_MESSAGE -> attachments.add(oldVKAudioMessage(jsonObject))
//                Type.GRAFFITI -> attachments.add(VKGraffiti(jsonObject))
                Type.POLL -> attachments.add(oldVKPoll(jsonObject))
                Type.CALL -> attachments.add(VKCall(jsonObject))
//                Type.WALL_POST -> attachments.add(VKWall(jsonObject))
                Type.WALL_REPLY -> attachments.add(oldVKComment(jsonObject))
//                Type.GEOLOCATION -> attachments.add(oldVKGeolocation(jsonObject))
                else -> continue
            }
        }

        return attachments
    }

    enum class Type(val value: String) {
        NONE("none"),
        PHOTO("photo"),
        VIDEO("video"),
        AUDIO("audio"),
        AUDIO_PLAYLIST("audio_playlist"),
        DOCUMENT("doc"),
        LINK("link"),
        STICKER("sticker"),
        GIFT("gift"),
        VOICE_MESSAGE("audio_message"),
        GRAFFITI("graffiti"),
        POLL("poll"),
        GEOLOCATION("geo"),
        WALL_POST("wall"),
        WALL_REPLY("wall_reply"),
        CALL("call"),
        STORY("story"),
        POINT("point"),
        MARKET("market"),
        ARTICLE("article"),
        PODCAST("podcast"),
        MONEY_REQUEST("money_request");

        companion object {
            fun fromString(value: String): Type {
                for (v in values()) {
                    if (v.value == value) return v
                }

                return NONE
            }
        }
    }
}