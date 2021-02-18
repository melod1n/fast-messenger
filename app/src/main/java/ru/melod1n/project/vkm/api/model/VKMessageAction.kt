package ru.melod1n.project.vkm.api.model

import androidx.room.Ignore
import org.json.JSONObject

class VKMessageAction() : VKModel() {

    companion object {
        const val ACTION_CHAT_CREATE = "chat_create"
        const val ACTION_PHOTO_UPDATE = "chat_photo_update"
        const val ACTION_PHOTO_REMOVE = "chat_photo_remove"
        const val ACTION_TITLE_UPDATE = "chat_title_update"
        const val ACTION_PIN_MESSAGE = "chat_pin_message"
        const val ACTION_UNPIN_MESSAGE = "chat_unpin_message"
        const val ACTION_INVITE_USER = "chat_invite_user"
        const val ACTION_INVITE_USER_BY_LINK = "chat_invite_user_by_link"
        const val ACTION_KICK_USER = "chat_kick_user"
        const val ACTION_SCREENSHOT = "chat_screenshot"
        const val ACTION_INVITE_USER_BY_CALL = "chat_invite_user_by_call"
        const val ACTION_INVITE_USER_BY_CALL_JOIN_LINK = "chat_invite_user_by_call_link"
    }

    /*
        chat_photo_update — обновлена фотография беседы;
        chat_photo_remove — удалена фотография беседы;
        chat_create — создана беседа;
        chat_title_update — обновлено название беседы;
        chat_invite_user — приглашен пользователь;
        chat_kick_user — исключен пользователь;
        chat_pin_message — закреплено сообщение;
        chat_unpin_message — откреплено сообщение;
        chat_invite_user_by_link — пользователь присоединился к беседе по ссылке.
    */

    var type: String = ""

    var memberId = 0

    @Ignore
    var message: VKMessage? = null

    var conversationMessageId = 0

    var text: String = ""
    var oldText: String = ""

//    @Embedded(prefix = "photo")
//    var photo: Photo? = null

    //TODO: add photo

    constructor(o: JSONObject) : this() {
        type = o.optString("type")
        memberId = o.optInt("member_id", -1)
        text = o.optString("text")
    }
}