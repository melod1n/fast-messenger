package com.meloda.fast.api.model.base

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkConversation(
    val peer: Peer,
    @SerializedName("last_message_id")
    val lastMessageId: Int,
    @SerializedName("in_read")
    val inRead: Int,
    @SerializedName("out_read")
    val outRead: Int,
    @SerializedName("sort_id")
    val sortId: SortId,
    @SerializedName("last_conversation_message_id")
    val lastConversationMessageId: Int,
    @SerializedName("is_marked_unread")
    val isMarkedUnread: Boolean,
    val important: Boolean,
    @SerializedName("push_settings")
    val pushSettings: PushSettings,
    @SerializedName("can_write")
    val canWrite: CanWrite,
    @SerializedName("can_send_money")
    val canSendMoney: Boolean,
    @SerializedName("can_receive_money")
    val canReceiveMoney: Boolean,
    @SerializedName("chat_settings")
    val chatSettings: ChatSettings?
) : Parcelable {

    @Parcelize
    data class Peer(
        val id: Int,
        val type: String,
        @SerializedName("local_id")
        val localId: Int
    ) : Parcelable

    @Parcelize
    data class SortId(
        @SerializedName("major_id")
        val majorId: Int,
        @SerializedName("minor_id")
        val minorId: Int
    ) : Parcelable

    @Parcelize
    data class PushSettings(
        @SerializedName("disabled_forever")
        val disabledForever: Boolean,
        @SerializedName("no_sound")
        val noSound: Boolean,
        @SerializedName("disabled_mentions")
        val disabledMentions: Boolean,
        @SerializedName("disabled_mass_mentions")
        val disabledMassMentions: Boolean
    ) : Parcelable

    @Parcelize
    data class CanWrite(
        val allowed: Boolean
    ) : Parcelable

    @Parcelize
    data class ChatSettings(
        @SerializedName("owner_id")
        val ownerId: Int,
        val title: String,
        val state: String,
        val acl: Acl,
        @SerializedName("members_count")
        val membersCount: Int,
        @SerializedName("friends_count")
        val friendsCount: Int,
        val photo: Photo,
        @SerializedName("admin_ids")
        val adminsIds: List<Int>,
        @SerializedName("active_ids")
        val activeIds: List<Int>,
        @SerializedName("is_group_channel")
        val isGroupChannel: Boolean,
        @SerializedName("is_disappearing")
        val isDisappearing: Boolean,
        @SerializedName("is_service")
        val isService: Boolean
    ) : Parcelable {

        @Parcelize
        data class Acl(
            @SerializedName("can_change_info")
            val canChangeInfo: Boolean,
            @SerializedName("can_change_invite_link")
            val canChangeInviteLink: Boolean,
            @SerializedName("can_change_pin")
            val canChangePin: Boolean,
            @SerializedName("can_invite")
            val canInvite: Boolean,
            @SerializedName("can_promote_users")
            val canPromoteUsers: Boolean,
            @SerializedName("can_see_invite_link")
            val canSeeInviteLink: Boolean,
            @SerializedName("can_moderate")
            val canModerate: Boolean,
            @SerializedName("can_copy_chat")
            val canCopyChat: Boolean,
            @SerializedName("can_call")
            val canCall: Boolean,
            @SerializedName("can_use_mass_mentions")
            val canUseMassMentions: Boolean,
            @SerializedName("can_change_style")
            val canChangeStyle: Boolean
        ) : Parcelable

        @Parcelize
        data class Photo(
            @SerializedName("photo_50")
            val photo50: String,
            @SerializedName("photo_100")
            val photo100: String,
            @SerializedName("photo_200")
            val photo200: String,
            @SerializedName("is_default_photo")
            val isDefaultPhoto: Boolean
        ) : Parcelable
    }
}