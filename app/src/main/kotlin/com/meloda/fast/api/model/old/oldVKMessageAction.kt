package com.meloda.fast.api.model.old

import org.json.JSONObject

class oldVKMessageAction() : VKModel() {

    companion object {
        const val serialVersionUID: Long = 1L
    }

    override val attachmentType = VKAttachments.Type.NONE

    var type: Type = Type.NONE
    var memberId = 0
    var message: oldVKMessage? = null
    var conversationMessageId: Int = 0
    var text: String = ""
    var oldText: String = ""

    //TODO: add photo

    constructor(o: JSONObject) : this() {
        type = Type.fromString(o.optString("type"))
        memberId = o.optInt("member_id", -1)
        text = o.optString("text")
    }

    enum class Type(val value: String) {
        NONE("none"),
        CHAT_CREATE("chat_create"),
        PHOTO_UPDATE("chat_photo_update"),
        PHOTO_REMOVE("chat_photo_remove"),
        TITLE_UPDATE("chat_title_update"),
        PIN_MESSAGE("chat_pin_message"),
        UNPIN_MESSAGE("chat_unpin_message"),
        INVITE_USER("chat_invite_user"),
        INVITE_USER_BY_LINK("chat_invite_user_by_link"),
        KICK_USER("chat_kick_user"),
        SCREENSHOT("chat_screenshot"),
        INVITE_USER_BY_CALL("chat_invite_user_by_call"),
        INVITE_USER_BY_CALL_LINK("chat_invite_user_by_call_link");

        companion object {
            fun fromString(value: String) = values().first { it.value == value }
        }
    }
}