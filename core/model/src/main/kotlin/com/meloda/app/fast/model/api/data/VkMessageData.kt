package com.meloda.app.fast.model.api.data

import com.meloda.app.fast.model.api.domain.VkAttachment
import com.meloda.app.fast.model.api.domain.VkMessage
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkMessageData(
    @Json(name = "id") val id: Int?,
    @Json(name = "peer_id") val peerId: Int?,
    @Json(name = "date") val date: Int,
    @Json(name = "from_id") val fromId: Int,
    @Json(name = "out") val out: Int?,
    @Json(name = "text") val text: String,
    @Json(name = "conversation_message_id") val conversationMessageId: Int,
    @Json(name = "fwd_messages") val fwdMessages: List<VkMessageData>? = emptyList(),
    @Json(name = "important") val important: Boolean = false,
    @Json(name = "random_id") val randomId: Int = 0,
    @Json(name = "attachments") val attachments: List<VkAttachmentItemData> = emptyList(),
    @Json(name = "is_hidden") val isHidden: Boolean = false,
    @Json(name = "payload") val payload: String?,
    @Json(name = "geo") val geo: Geo?,
    @Json(name = "action") val action: Action?,
    @Json(name = "ttl") val ttl: Int?,
    @Json(name = "reply_message") val replyMessage: VkMessageData?,
    @Json(name = "update_time") val updateTime: Int?
) {

    @JsonClass(generateAdapter = true)
    data class Geo(
        @Json(name = "type") val type: String,
        @Json(name = "coordinates") val coordinates: Coordinates,
        @Json(name = "place") val place: Place
    ) {

        @JsonClass(generateAdapter = true)
        data class Coordinates(
            @Json(name = "latitude") val latitude: Float,
            @Json(name = "longitude") val longitude: Float
        )

        @JsonClass(generateAdapter = true)
        data class Place(
            @Json(name = "country") val country: String,
            @Json(name = "city") val city: String,
            @Json(name = "title") val title: String
        )
    }

    @JsonClass(generateAdapter = true)
    data class Action(
        @Json(name = "type") val type: String,
        @Json(name = "member_id") val memberId: Int?,
        @Json(name = "text") val text: String?,
        @Json(name = "conversation_message_id") val conversationMessageId: Int?,
        @Json(name = "message") val message: String?
    )
}

fun VkMessageData.asDomain(): VkMessage = VkMessage(
    id = id ?: -1,
    text = text.ifBlank { null },
    isOut = out == 1,
    peerId = peerId ?: -1,
    fromId = fromId,
    date = date,
    randomId = randomId,
    action = VkMessage.Action.parse(action?.type),
    actionMemberId = action?.memberId,
    actionText = action?.text,
    actionConversationMessageId = action?.conversationMessageId,
    actionMessage = action?.message,
    geoType = geo?.type,
    important = important,
    updateTime = updateTime,
    forwards = fwdMessages.orEmpty().map(VkMessageData::asDomain),
    attachments = parseAttachments(),
    replyMessage = replyMessage?.asDomain(),
    user = null,
    group = null,
    actionUser = null,
    actionGroup = null,
)

private fun VkMessageData.parseAttachments(): List<VkAttachment> {
    if (attachments.isEmpty()) return emptyList()

    val attachments = mutableListOf<VkAttachment>()

    for (baseAttachment in this.attachments) {
        when (baseAttachment.getPreparedType()) {
            AttachmentType.UNKNOWN -> continue
            AttachmentType.PHOTO -> {
                val photo = baseAttachment.photo ?: continue
                attachments += photo.toDomain()
            }

            AttachmentType.VIDEO -> {
                val video = baseAttachment.video ?: continue
                attachments += video.toDomain()
            }

            AttachmentType.AUDIO -> {
                val audio = baseAttachment.audio ?: continue
                attachments += audio.toDomain()
            }

            AttachmentType.FILE -> {
                val file = baseAttachment.file ?: continue
                attachments += file.toDomain()
            }

            AttachmentType.LINK -> {
                val link = baseAttachment.link ?: continue
                attachments += link.toDomain()
            }

            AttachmentType.MINI_APP -> {
                val miniApp = baseAttachment.miniApp ?: continue
                attachments += miniApp.toDomain()
            }

            AttachmentType.AUDIO_MESSAGE -> {
                val voiceMessage = baseAttachment.voiceMessage ?: continue
                attachments += voiceMessage.toDomain()
            }

            AttachmentType.STICKER -> {
                val sticker = baseAttachment.sticker ?: continue
                attachments += sticker.toDomain()
            }

            AttachmentType.GIFT -> {
                val gift = baseAttachment.gift ?: continue
                attachments += gift.toDomain()
            }

            AttachmentType.WALL -> {
                val wall = baseAttachment.wall ?: continue
                attachments += wall.toDomain()
            }

            AttachmentType.GRAFFITI -> {
                val graffiti = baseAttachment.graffiti ?: continue
                attachments += graffiti.toDomain()
            }

            AttachmentType.POLL -> {
                val poll = baseAttachment.poll ?: continue
                attachments += poll.toDomain()
            }

            AttachmentType.WALL_REPLY -> {
                val wallReply = baseAttachment.wallReply ?: continue
                attachments += wallReply.toDomain()
            }

            AttachmentType.CALL -> {
                val call = baseAttachment.call ?: continue
                attachments += call.toDomain()
            }

            AttachmentType.GROUP_CALL_IN_PROGRESS -> {
                val groupCall = baseAttachment.groupCall ?: continue
                attachments += groupCall.toDomain()
            }

            AttachmentType.CURATOR -> {
                val curator = baseAttachment.curator ?: continue
                attachments += curator.toDomain()
            }

            AttachmentType.EVENT -> {
                val event = baseAttachment.event ?: continue
                attachments += event.toDomain()
            }

            AttachmentType.STORY -> {
                val story = baseAttachment.story ?: continue
                attachments += story.toDomain()
            }

            AttachmentType.WIDGET -> {
                val widget = baseAttachment.widget ?: continue
                attachments += widget.toDomain()
            }

            AttachmentType.ARTIST -> {
                val artist = baseAttachment.artist ?: continue
                attachments += artist.toDomain()
                val audios = baseAttachment.audios ?: continue
                audios.map(VkAudioData::toDomain).let(attachments::addAll)
            }

            AttachmentType.AUDIO_PLAYLIST -> {
                val audioPlaylist = baseAttachment.audioPlaylist ?: continue
                attachments += audioPlaylist.toDomain()
            }

            AttachmentType.PODCAST -> {
                val podcast = baseAttachment.podcast ?: continue
                attachments += podcast.toDomain()
            }
        }
    }
    return attachments
}
