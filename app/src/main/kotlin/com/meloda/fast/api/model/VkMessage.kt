package com.meloda.fast.api.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.meloda.fast.api.model.attachments.VkAttachment

@Entity(tableName = "messages")
data class VkMessage(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val text: String?,
    val isOut: Boolean,
    val peerId: Int,
    val fromId: Int,
    val date: Int,
    val action: String?,
    val actionMemberId: Int?,
    val actionText: String?,
    val actionConversationMessageId: Int?,
    val actionMessage: String?,
    val geoType: String?
) {
    @Ignore
    var forwards: List<VkMessage>? = null

    @Ignore
    var attachments: List<VkAttachment>? = null

    fun isUser() = id > 0

    fun isGroup() = id < 0

    fun getPreparedAction(): Action? {
        if (action == null) return null
        return Action.parse(action)
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
        CHAT_INVITE_USER_BY_CALL_LINK("chat_invite_user_by_call_join_link");

        companion object {
            fun parse(value: String) = values().first { it.value == value }
        }
    }

}
