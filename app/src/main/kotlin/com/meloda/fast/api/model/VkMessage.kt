package com.meloda.fast.api.model

import androidx.lifecycle.MutableLiveData
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.model.attachments.VkAttachment
import com.meloda.fast.base.adapter.SelectableItem
import com.meloda.fast.util.TimeUtils
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Entity(tableName = "messages")
@Parcelize
data class VkMessage(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val text: String? = null,
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
    val geoType: String? = null,
    val important: Boolean = false,

    var forwards: List<VkMessage>? = null,
    var attachments: List<VkAttachment>? = null,

//    @Embedded(prefix = "replyMessage_")
    var replyMessage: VkMessage? = null
) : SelectableItem() {

    @Ignore
    @IgnoredOnParcel
    val user = MutableLiveData<VkUser?>()

    @Ignore
    @IgnoredOnParcel
    val group = MutableLiveData<VkGroup?>()

    fun isPeerChat() = peerId > 2_000_000_000

    fun isUser() = fromId > 0

    fun isGroup() = fromId < 0

    fun isRead(conversation: VkConversation) =
        if (isOut) conversation.outRead < id
        else conversation.inRead < id

    fun getPreparedAction(): Action? {
        if (action == null) return null
        return Action.parse(action)
    }

    fun canEdit() =
        fromId == UserConfig.userId &&
                (System.currentTimeMillis() / 1000 - date.toLong() < TimeUtils.ONE_DAY_IN_SECONDS)

    fun copyMessage(
        id: Int = this.id,
        text: String? = this.text,
        isOut: Boolean = this.isOut,
        peerId: Int = this.peerId,
        fromId: Int = this.fromId,
        date: Int = this.date,
        randomId: Int = this.randomId,
        action: String? = this.action,
        actionMemberId: Int? = this.actionMemberId,
        actionText: String? = this.actionText,
        actionConversationMessageId: Int? = this.actionConversationMessageId,
        actionMessage: String? = this.actionMessage,
        geoType: String? = this.geoType,
        important: Boolean = this.important
    ) = VkMessage(
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
        geoType = geoType,
        important = important
    ).also {
        it.attachments = attachments
        it.forwards = forwards
    }

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

        // TODO: 9/11/2021 catch this shit
        CHAT_INVITE_USER_BY_CALL("chat_invite_user_by_call"),
        CHAT_INVITE_USER_BY_CALL_LINK("chat_invite_user_by_call_join_link"),
        CHAT_STYLE_UPDATE("conversation_style_update");

        companion object {
            fun parse(value: String) = values().first { it.value == value }
        }
    }

}