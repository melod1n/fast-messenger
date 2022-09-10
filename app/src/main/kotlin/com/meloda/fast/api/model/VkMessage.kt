package com.meloda.fast.api.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.model.attachments.VkAttachment
import com.meloda.fast.api.model.base.BaseVkMessage
import com.meloda.fast.model.SelectableItem
import com.meloda.fast.util.TimeUtils
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Entity(tableName = "messages")
@Parcelize
data class VkMessage constructor(
    @PrimaryKey(autoGenerate = false)
    var id: Int,
    var text: String? = null,
    val isOut: Boolean,
    val peerId: Int,
    val fromId: Int,
    val date: Int,
    val randomId: Int,
    val action: String? = null,
    val actionMemberId: Int? = null,
    val actionText: String? = null,
    val actionConversationMessageId: Int? = null,
    val actionMessage: String? = null,

    var updateTime: Int? = null,

    var important: Boolean = false,

    var forwards: List<VkMessage>? = null,
    var attachments: List<VkAttachment>? = null,
    var replyMessage: VkMessage? = null,

    val geo: BaseVkMessage.Geo? = null,
) : SelectableItem() {

    @Ignore
    @IgnoredOnParcel
    var user: VkUser? = null

    @Ignore
    @IgnoredOnParcel
    var group: VkGroup? = null

    @Ignore
    @IgnoredOnParcel
    var state: State = State.Sent

    fun isPeerChat() = peerId > 2_000_000_000

    fun isUser() = fromId > 0

    fun isGroup() = fromId < 0

    fun isRead(conversation: VkConversation) =
        if (isOut) {
            conversation.outRead - id >= 0
        } else {
            conversation.inRead - id >= 0
        }

    fun getPreparedAction(): Action? {
        if (action == null) return null
        return Action.parse(action)
    }

    fun canEdit() =
        fromId == UserConfig.userId &&
                (attachments == null ||
                        !VKConstants.restrictedToEditAttachments.contains(
                            requireNotNull(attachments).first().javaClass
                        )) &&
                (System.currentTimeMillis() / 1000 - date.toLong() < TimeUtils.OneDayInSeconds)

    fun hasAttachments(): Boolean = !attachments.isNullOrEmpty()

    fun hasReply(): Boolean = replyMessage != null

    fun hasForwards(): Boolean = !forwards.isNullOrEmpty()

    fun hasGeo(): Boolean = geo != null

    fun isUpdated(): Boolean = updateTime != null && requireNotNull(updateTime) > 0

    fun isSending(): Boolean = state == State.Sending

    fun isError(): Boolean = state == State.Error

    fun isSent(): Boolean = state == State.Sent

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
            fun parse(value: String?): Action? = values().firstOrNull { it.value == value }
        }
    }

    enum class State {
        Sending, Sent, Error
    }

}
