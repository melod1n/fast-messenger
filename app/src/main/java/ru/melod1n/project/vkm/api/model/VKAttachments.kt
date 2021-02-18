package ru.melod1n.project.vkm.api.model

import org.json.JSONArray
import java.util.*

object VKAttachments {

    private const val TYPE_PHOTO = "photo"
    private const val TYPE_VIDEO = "video"
    private const val TYPE_AUDIO = "audio"
    private const val TYPE_DOC = "doc"
    private const val TYPE_LINK = "link"
    private const val TYPE_STICKER = "sticker"
    private const val TYPE_GIFT = "gift"
    private const val TYPE_AUDIO_MESSAGE = "audio_message"
    private const val TYPE_GRAFFITI = "graffiti"
    private const val TYPE_POLL = "poll"
    private const val TYPE_GEO = "geo"
    private const val TYPE_WALL = "wall"
    private const val TYPE_CALL = "call"
    private const val TYPE_STORY = "story"
    private const val TYPE_POINT = "point"
    private const val TYPE_MARKET = "market"
    private const val TYPE_ARTICLE = "article"
    private const val TYPE_PODCAST = "podcast"
    private const val TYPE_WALL_REPLY = "wall_reply"
    private const val TYPE_MONEY_REQUEST = "money_request"
    private const val TYPE_AUDIO_PLAYLIST = "audio_playlist"

    fun parse(array: JSONArray): ArrayList<VKModel> {
        val attachments = ArrayList<VKModel>(array.length())

        for (i in 0 until array.length()) {
            var attachment = array.optJSONObject(i) ?: continue
            if (attachment.has("attachment")) {
                attachment = attachment.optJSONObject("attachment") ?: continue
            }

            val type = attachment.optString("type")
            val jsonObject = attachment.optJSONObject(type) ?: continue

            when (type) {
                TYPE_PHOTO -> attachments.add(VKPhoto(jsonObject))
                TYPE_AUDIO -> attachments.add(VKAudio(jsonObject))
                TYPE_VIDEO -> attachments.add(VKVideo(jsonObject))
                TYPE_DOC -> attachments.add(VKDoc(jsonObject))
                TYPE_STICKER -> attachments.add(VKSticker(jsonObject))
                TYPE_LINK -> attachments.add(VKLink(jsonObject))
                TYPE_GIFT -> attachments.add(VKGift(jsonObject))
                TYPE_AUDIO_MESSAGE -> attachments.add(VKAudioMessage(jsonObject))
                TYPE_GRAFFITI -> attachments.add(VKGraffiti(jsonObject))
                TYPE_POLL -> attachments.add(VKPoll(jsonObject))
                TYPE_CALL -> attachments.add(VKCall(jsonObject))
            }
        }

        return attachments
    }
}