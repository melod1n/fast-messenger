package com.meloda.fast.api.model.base

import android.os.Parcelable
import com.meloda.fast.api.model.VkConversation
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.base.attachments.BaseVkGroupCall
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkConversation(
    val peer: Peer,
    val last_message_id: Int,
    val in_read: Int,
    val out_read: Int,
    val sort_id: SortId,
    val last_conversation_message_id: Int,
    val is_marked_unread: Boolean,
    val important: Boolean,
    val push_settings: PushSettings,
    val can_write: CanWrite,
    val can_send_money: Boolean,
    val can_receive_money: Boolean,
    val chat_settings: ChatSettings?,
    val call_in_progress: CallInProgress?,
    val unread_count: Int?
) : Parcelable {

    fun asVkConversation(lastMessage: VkMessage? = null) = VkConversation(
        id = peer.id,
        title = chat_settings?.title,
        photo200 = chat_settings?.photo?.photo_200,
        type = peer.type,
        callInProgress = call_in_progress != null,
        isPhantom = chat_settings?.is_disappearing == true,
        lastConversationMessageId = last_conversation_message_id,
        inRead = in_read,
        outRead = out_read,
        isMarkedUnread = is_marked_unread,
        lastMessageId = last_message_id,
        unreadCount = unread_count ?: 0,
        membersCount = chat_settings?.members_count,
        ownerId = chat_settings?.owner_id,
        majorId = sort_id.major_id,
        minorId = sort_id.minor_id,
        canChangePin = chat_settings?.acl?.can_change_pin == true
    ).apply {
        this.lastMessage = lastMessage
        this.pinnedMessage = chat_settings?.pinned_message?.asVkMessage()
    }

    @Parcelize
    data class Peer(
        val id: Int,
        val type: String,
        val local_id: Int
    ) : Parcelable

    @Parcelize
    data class SortId(
        val major_id: Int,
        val minor_id: Int
    ) : Parcelable

    @Parcelize
    data class PushSettings(
        val disabled_forever: Boolean,
        val no_sound: Boolean,
        val disabled_mentions: Boolean,
        val disabled_mass_mentions: Boolean
    ) : Parcelable

    @Parcelize
    data class CanWrite(
        val allowed: Boolean
    ) : Parcelable

    @Parcelize
    data class ChatSettings(
        val owner_id: Int,
        val title: String,
        val state: String,
        val acl: Acl,
        val members_count: Int,
        val friends_count: Int,
        val photo: Photo?,
        val admin_ids: List<Int>,
        val active_ids: List<Int>,
        val is_group_channel: Boolean,
        val is_disappearing: Boolean,
        val is_service: Boolean,
        val theme: String?,
        val pinned_message: BaseVkMessage?
    ) : Parcelable {

        @Parcelize
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
            val can_change_style: Boolean
        ) : Parcelable

        @Parcelize
        data class Photo(
            val photo_50: String?,
            val photo_100: String?,
            val photo_200: String?,
            val is_default_photo: Boolean
        ) : Parcelable
    }

    @Parcelize
    data class CallInProgress(
        val participants: BaseVkGroupCall.Participants,
        val join_link: String
    ) : Parcelable {

        @Parcelize
        data class Participants(
            val list: List<Int>,
            val count: Int
        ) : Parcelable

    }
}