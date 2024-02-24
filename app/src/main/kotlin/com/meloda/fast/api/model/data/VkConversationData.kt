package com.meloda.fast.api.model.data

import com.meloda.fast.api.model.VkGroupDomain
import com.meloda.fast.api.model.VkMessageDomain
import com.meloda.fast.api.model.VkUserDomain
import com.meloda.fast.api.model.base.VkMessageData
import com.meloda.fast.api.model.base.attachments.BaseVkGroupCall
import com.meloda.fast.api.model.domain.VkConversationDomain
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkConversationData(
    val peer: Peer,
    val last_message_id: Int,
    val in_read: Int,
    val out_read: Int,
    val in_read_cmid: Int,
    val out_read_cmid: Int,
    val sort_id: SortId,
    val last_conversation_message_id: Int,
    val is_marked_unread: Boolean,
    val important: Boolean,
    val push_settings: PushSettings?,
    val can_write: CanWrite,
    val can_send_money: Boolean = false,
    val can_receive_money: Boolean = false,
    val chat_settings: ChatSettings?,
    val call_in_progress: CallInProgress?,
    val unread_count: Int?,
) {

    @JsonClass(generateAdapter = true)
    data class Peer(
        val id: Int,
        val type: String,
        val local_id: Int,
    )

    @JsonClass(generateAdapter = true)
    data class SortId(
        val major_id: Int,
        val minor_id: Int,
    )

    @JsonClass(generateAdapter = true)
    data class PushSettings(
        val disabled_forever: Boolean,
        val no_sound: Boolean,
        val disabled_mentions: Boolean,
        val disabled_mass_mentions: Boolean,
    )

    @JsonClass(generateAdapter = true)
    data class CanWrite(
        val allowed: Boolean,
    )

    @JsonClass(generateAdapter = true)
    data class ChatSettings(
        val owner_id: Int,
        val title: String,
        val state: String,
        val acl: Acl,
        val members_count: Int?,
        val friends_count: Int?,
        val photo: Photo?,
        val admin_ids: List<Int> = emptyList(),
        val active_ids: List<Int> = emptyList(),
        val is_group_channel: Boolean = false,
        val is_disappearing: Boolean = false,
        val is_service: Boolean = false,
        val theme: String?,
        val pinned_message: VkMessageData?,
    ) {

        @JsonClass(generateAdapter = true)
        data class Acl(
            val can_change_info: Boolean,
            val can_change_invite_link: Boolean,
            val can_change_pin: Boolean,
            val can_invite: Boolean,
            val can_promote_users: Boolean,
            val can_see_invite_link: Boolean,
            val can_moderate: Boolean,
            val can_copy_chat: Boolean,
            val can_call: Boolean,
            val can_use_mass_mentions: Boolean,
            val can_change_style: Boolean,
        )

        @JsonClass(generateAdapter = true)
        data class Photo(
            val photo_50: String?,
            val photo_100: String?,
            val photo_200: String?,
            val is_default_photo: Boolean,
        )
    }

    @JsonClass(generateAdapter = true)
    data class CallInProgress(
        val participants: BaseVkGroupCall.Participants,
        val join_link: String,
    ) {

        @JsonClass(generateAdapter = true)
        data class Participants(
            val list: List<Int>,
            val count: Int,
        )

    }

    fun mapToDomain(
        lastMessage: VkMessageDomain? = null,
        conversationUser: VkUserDomain? = null,
        conversationGroup: VkGroupDomain? = null,
    ) = VkConversationDomain(
        id = peer.id,
        localId = peer.local_id,
        conversationTitle = chat_settings?.title,
        conversationPhoto = chat_settings?.photo?.photo_200,
        type = peer.type,
        isCallInProgress = call_in_progress != null,
        isPhantom = chat_settings?.is_disappearing == true,
        lastConversationMessageId = last_conversation_message_id,
        inRead = in_read,
        outRead = out_read,
        lastMessageId = last_message_id,
        unreadCount = unread_count ?: 0,
        membersCount = chat_settings?.members_count,
        ownerId = chat_settings?.owner_id,
        majorId = sort_id.major_id,
        minorId = sort_id.minor_id,
        canChangePin = chat_settings?.acl?.can_change_pin == true,
        canChangeInfo = chat_settings?.acl?.can_change_info == true,
        pinnedMessageId = chat_settings?.pinned_message?.id,
        inReadCmId = in_read_cmid,
        outReadCmId = out_read_cmid,
        interactionType = -1,
        interactionIds = emptyList()
    ).also {
        it.lastMessage = lastMessage
        it.pinnedMessage = chat_settings?.pinned_message?.asVkMessage()
        it.conversationUser = conversationUser
        it.conversationGroup = conversationGroup
    }
}
