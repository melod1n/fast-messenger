package dev.meloda.fast.model.api.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import dev.meloda.fast.model.api.PeerType
import dev.meloda.fast.model.api.domain.VkConversation
import dev.meloda.fast.model.api.domain.VkMessage

@JsonClass(generateAdapter = true)
data class VkConversationData(
    @Json(name = "peer") val peer: Peer,
    @Json(name = "last_message_id") val lastMessageId: Long?,
    @Json(name = "in_read") val inRead: Long,
    @Json(name = "out_read") val outRead: Long,
    @Json(name = "in_read_cmid") val inReadConversationMessageId: Long,
    @Json(name = "out_read_cmid") val outReadConversationMessageId: Long,
    @Json(name = "sort_id") val sortId: SortId,
    @Json(name = "last_conversation_message_id") val lastConversationMessageId: Long,
    @Json(name = "is_marked_unread") val isMarkedUnread: Boolean,
    @Json(name = "important") val important: Boolean,
    @Json(name = "push_settings") val pushSettings: PushSettings?,
    @Json(name = "can_write") val canWrite: CanWrite,
    @Json(name = "can_send_money") val canSendMoney: Boolean = false,
    @Json(name = "can_receive_money") val canReceiveMoney: Boolean = false,
    @Json(name = "chat_settings") val chatSettings: ChatSettings?,
    @Json(name = "call_in_progress") val callInProgress: CallInProgress?,
    @Json(name = "unread_count") val unreadCount: Int?,
    @Json(name = "is_archived") val isArchived: Boolean?
) {

    @JsonClass(generateAdapter = true)
    data class Peer(
        @Json(name = "id") val id: Long,
        @Json(name = "type") val type: String,
        @Json(name = "local_id") val localId: Long,
    )

    @JsonClass(generateAdapter = true)
    data class SortId(
        @Json(name = "major_id") val majorId: Int,
        @Json(name = "minor_id") val minorId: Int,
    )

    @JsonClass(generateAdapter = true)
    data class PushSettings(
        @Json(name = "disabled_forever") val disabledForever: Boolean,
        @Json(name = "no_sound") val noSound: Boolean,
        @Json(name = "disabled_mentions") val disabledMentions: Boolean,
        @Json(name = "disabled_mass_mentions") val disabledMassMentions: Boolean,
    )

    @JsonClass(generateAdapter = true)
    data class CanWrite(
        @Json(name = "allowed") val allowed: Boolean,
    )

    @JsonClass(generateAdapter = true)
    data class ChatSettings(
        @Json(name = "owner_id") val ownerId: Long,
        @Json(name = "title") val title: String,
        @Json(name = "state") val state: String,
        @Json(name = "acl") val acl: Acl,
        @Json(name = "members_count") val membersCount: Int?,
        @Json(name = "friends_count") val friendsCount: Int?,
        @Json(name = "photo") val photo: Photo?,
        @Json(name = "admin_ids") val adminIds: List<Int> = emptyList(),
        @Json(name = "active_ids") val activeIds: List<Int> = emptyList(),
        @Json(name = "is_group_channel") val isGroupChannel: Boolean = false,
        @Json(name = "is_disappearing") val isDisappearing: Boolean = false,
        @Json(name = "is_service") val isService: Boolean = false,
        @Json(name = "theme") val theme: String?,
        @Json(name = "pinned_message") val pinnedMessage: VkPinnedMessageData?,
    ) {

        @JsonClass(generateAdapter = true)
        data class Acl(
            @Json(name = "can_change_info") val canChangeInfo: Boolean,
            @Json(name = "can_change_invite_link") val canChangeInviteLink: Boolean,
            @Json(name = "can_change_pin") val canChangePin: Boolean,
            @Json(name = "can_invite") val canInvite: Boolean,
            @Json(name = "can_promote_users") val canPromoteUsers: Boolean,
            @Json(name = "can_see_invite_link") val canSeeInviteLink: Boolean,
            @Json(name = "can_moderate") val canModerate: Boolean,
            @Json(name = "can_copy_chat") val canCopyChat: Boolean,
            @Json(name = "can_call") val canCall: Boolean,
            @Json(name = "can_use_mass_mentions") val canUseMassMentions: Boolean,
            @Json(name = "can_change_style") val canChangeStyle: Boolean,
        )

        @JsonClass(generateAdapter = true)
        data class Photo(
            @Json(name = "photo_50") val photo50: String?,
            @Json(name = "photo_100") val photo100: String?,
            @Json(name = "photo_200") val photo200: String?,
            @Json(name = "is_default_photo") val isDefaultPhoto: Boolean,
        )
    }

    @JsonClass(generateAdapter = true)
    data class CallInProgress(
        @Json(name = "participants") val participants: Participants,
        @Json(name = "join_link") val joinLink: String,
    ) {

        @JsonClass(generateAdapter = true)
        data class Participants(
            @Json(name = "list") val list: List<Int>,
            @Json(name = "count") val count: Int,
        )
    }

    fun asDomain(
        lastMessage: VkMessage? = null,
    ): VkConversation = VkConversation(
        id = peer.id,
        localId = peer.localId,
        title = chatSettings?.title,
        photo50 = chatSettings?.photo?.photo50,
        photo100 = chatSettings?.photo?.photo100,
        photo200 = chatSettings?.photo?.photo200,
        isCallInProgress = callInProgress != null,
        isPhantom = chatSettings?.isDisappearing == true,
        lastCmId = lastConversationMessageId,
        inRead = inRead,
        outRead = outRead,
        lastMessageId = lastMessageId,
        unreadCount = unreadCount ?: 0,
        membersCount = chatSettings?.membersCount,
        ownerId = chatSettings?.ownerId,
        majorId = sortId.majorId,
        minorId = sortId.minorId,
        canChangePin = chatSettings?.acl?.canChangePin == true,
        canChangeInfo = chatSettings?.acl?.canChangeInfo == true,
        pinnedMessageId = chatSettings?.pinnedMessage?.id,
        inReadCmId = inReadConversationMessageId,
        outReadCmId = outReadConversationMessageId,
        interactionType = -1,
        interactionIds = emptyList(),
        peerType = PeerType.parse(peer.type),
        lastMessage = lastMessage,
        pinnedMessage = chatSettings?.pinnedMessage?.mapToDomain(),
        user = null,
        group = null,
        isArchived = isArchived == true
    )
}
