package dev.meloda.fast.model.api.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import dev.meloda.fast.model.api.data.VkAttachmentHistoryMessageData
import dev.meloda.fast.model.api.data.VkChatMemberData
import dev.meloda.fast.model.api.data.VkContactData
import dev.meloda.fast.model.api.data.VkConvoData
import dev.meloda.fast.model.api.data.VkGroupData
import dev.meloda.fast.model.api.data.VkMessageData
import dev.meloda.fast.model.api.data.VkUserData

@JsonClass(generateAdapter = true)
data class MessagesGetHistoryResponse(
    val count: Int,
    val items: List<VkMessageData>,
    val convos: List<VkConvoData>?,
    val profiles: List<VkUserData>?,
    val groups: List<VkGroupData>?,
    val contacts: List<VkContactData>?
)

@JsonClass(generateAdapter = true)
data class MessagesGetByIdResponse(
    val count: Int,
    val items: List<VkMessageData> = emptyList(),
    val profiles: List<VkUserData>?,
    val groups: List<VkGroupData>?,
    val contacts: List<VkContactData>?
)

@JsonClass(generateAdapter = true)
data class MessagesGetConvoMembersResponse(
    val count: Int,
    val items: List<VkChatMemberData>?,
    val profiles: List<VkUserData>?,
    val groups: List<VkGroupData>?,
    val contacts: List<VkContactData>?
)

@JsonClass(generateAdapter = true)
data class MessagesGetHistoryAttachmentsResponse(
    @Json(name = "items") val items: List<VkAttachmentHistoryMessageData>,
    @Json(name = "next_from") val nextFrom: String?,
    @Json(name = "profiles") val profiles: List<VkUserData>?,
    @Json(name = "groups") val groups: List<VkGroupData>?,
    @Json(name = "contacts") val contacts: List<VkContactData>?
)

@JsonClass(generateAdapter = true)
data class MessagesCreateChatResponse(
    @Json(name = "chat_id") val chatId: Long,
    @Json(name = "peer_ids") val peerIds: List<Int>
)

@JsonClass(generateAdapter = true)
data class MessagesSendResponse(
    @Json(name = "message_id") val messageId: Long,
    @Json(name = "cmid") val cmId: Long
)

@JsonClass(generateAdapter = true)
data class MessagesMarkAsImportantResponse(
    @Json(name = "marked") val marked: List<Mark>
) {
    @JsonClass(generateAdapter = true)
    data class Mark(
        @Json(name = "cmid") val cmId: Long,
        @Json(name = "message_id") val messageId: Long,
        @Json(name = "peer_id") val peerId: Long
    )
}

@JsonClass(generateAdapter = true)
data class MessagesGetReadPeersResponse(
    @Json(name = "items") val items: List<Long>,
    @Json(name = "total_count") val totalCount: Int,
    @Json(name = "profiles") val profiles: List<VkUserData>?,
)
