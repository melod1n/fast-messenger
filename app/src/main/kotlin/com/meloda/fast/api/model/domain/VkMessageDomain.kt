package com.meloda.fast.api.model.domain

import androidx.compose.runtime.Immutable
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.VkGroupsMap
import com.meloda.fast.api.VkUsersMap
import com.meloda.fast.api.model.data.VkMessageData
import com.meloda.fast.database.model.VkMessageDB
import com.meloda.fast.util.TimeUtils

@Immutable
data class VkMessageDomain(
    val id: Int,
    val text: String?,
    val isOut: Boolean,
    val peerId: Int,
    val fromId: Int,
    val date: Int,
    val randomId: Int,
    val action: String?,
    val actionMemberId: Int?,
    val actionText: String?,
    val actionConversationMessageId: Int?,
    val actionMessage: String?,

    val updateTime: Int?,

    val important: Boolean = false,

    val forwards: List<VkMessageDomain>?,
    val attachments: List<VkAttachment>?,
    val replyMessage: VkMessageDomain?,

    val geo: VkMessageData.Geo?,

    val user: VkUserDomain?,
    val group: VkGroupDomain?,
    val actionUser: VkUserDomain?,
    val actionGroup: VkGroupDomain?,
) {

    fun isPeerChat() = peerId > 2_000_000_000

    fun isUser() = fromId > 0

    fun isGroup() = fromId < 0

    fun isRead(conversation: VkConversationDomain) =
        if (isOut) {
            conversation.outRead - id >= 0
        } else {
            conversation.inRead - id >= 0
        }

    fun getPreparedAction(): Action? {
        if (action == null) return null
        return Action.parse(action)
    }

    fun getUserAndGroup(
        usersMap: VkUsersMap,
        groupsMap: VkGroupsMap
    ): Pair<VkUserDomain?, VkGroupDomain?> {
        val user: VkUserDomain? = usersMap.messageUser(this)
        val group: VkGroupDomain? = groupsMap.messageGroup(this)

        return user to group
    }

    fun getActionUserAndGroup(
        usersMap: VkUsersMap,
        groupsMap: VkGroupsMap
    ): Pair<VkUserDomain?, VkGroupDomain?> {
        val user: VkUserDomain? = usersMap.messageActionUser(this)
        val group: VkGroupDomain? = groupsMap.messageActionGroup(this)

        return user to group
    }

    fun canEdit() =
        fromId == UserConfig.userId &&
                (attachments == null ||
                        !VKConstants.restrictedToEditAttachments.contains(
                            attachments.first().javaClass
                        )) &&
                (System.currentTimeMillis() / 1000 - date.toLong() < TimeUtils.OneDayInSeconds)

    fun hasAttachments(): Boolean = !attachments.isNullOrEmpty()

    fun hasReply(): Boolean = replyMessage != null

    fun hasForwards(): Boolean = !forwards.isNullOrEmpty()

    fun hasGeo(): Boolean = geo != null

    fun isUpdated(): Boolean = updateTime != null && updateTime > 0

    enum class Action(val value: String) {
        CHAT_CREATE("chat_create"),
        CHAT_PHOTO_UPDATE("chat_photo_update"),
        CHAT_PHOTO_REMOVE("chat_photo_remove"),
        CHAT_TITLE_UPDATE("chat_title_update"),
        CHAT_PIN_MESSAGE("chat_pin_message"),
        CHAT_UNPIN_MESSAGE("chat_unpin_message"),
        CHAT_INVITE_USER("chat_invite_user"),
        CHAT_INVITE_USER_BY_LINK("chat_invite_user_by_link"),
        CHAT_KICK_USER("chat_kick_user"),
        CHAT_SCREENSHOT("chat_screenshot"),

        CHAT_INVITE_USER_BY_CALL("chat_invite_user_by_call"),
        CHAT_INVITE_USER_BY_CALL_LINK("chat_invite_user_by_call_join_link"),
        CHAT_STYLE_UPDATE("conversation_style_update");

        companion object {
            fun parse(value: String?): Action? = entries.firstOrNull { it.value == value }
        }
    }

    fun mapToDB(): VkMessageDB = VkMessageDB(
        id = id,
        text = text,
        isOut = isOut,
        peerId = peerId,
        fromId = fromId,
        date = date,
        randomId = randomId,
        action = action,
        actionMemberId = actionMemberId,
        actionText = actionText,
        actionConversationMessageId = actionConversationMessageId,
        actionMessage = actionMessage,
        updateTime = updateTime,
        important = important,
        forwardIds = forwards.orEmpty().map(VkMessageDomain::id).joinToString(),
        attachments = attachments.orEmpty().map(VkAttachment::type).joinToString(),
        replyMessageId = replyMessage?.id,
        geoType = geo?.type
    )
}
