package com.meloda.fast.api.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomWarnings
import org.json.JSONObject

@SuppressWarnings(RoomWarnings.PRIMARY_KEY_FROM_EMBEDDED_IS_DROPPED)
@Entity(tableName = "conversations")
class VKConversation() : VKModel(), Cloneable {

    companion object {
        var profiles = arrayListOf<VKUser>()
        var groups = arrayListOf<VKGroup>()

        var conversationsCount: Int = 0

        const val STATE_IN = "in"
        const val STATE_KICKED = "kicked"
        const val STATE_LEFT = "left"

        const val TYPE_USER = "user"
        const val TYPE_CHAT = "chat"
        const val TYPE_GROUP = "group"

        var count = 0
    }

    /*
           18 — пользователь заблокирован или удален;
           900 — нельзя отправить сообщение пользователю, который в чёрном списке;
           901 — пользователь запретил сообщения от сообщества;
           902 — пользователь запретил присылать ему сообщения с помощью настроек приватности;
           915 — в сообществе отключены сообщения;
           916 — в сообществе заблокированы сообщения;
           917 — нет доступа к чату;
           918 — нет доступа к e-mail;
           203 — нет доступа к сообществу
       */

    var isAllowed = false
    var reason = -1

    var inRead = 0
    var outRead = 0
    var lastMessageId = 0
    var unreadCount = 0

    @PrimaryKey(autoGenerate = false)
    var conversationId = 0

    var type: String = ""
    var localId = 0

    var disabledUntil = 0
    var isDisabledForever = false
    var isNoSound = false

    var membersCount = 0
    var title: String = ""

    var pinnedMessageId = 0

    var state: String = ""

    @Embedded(prefix = "cMessage")
    var lastMessage = VKMessage()

    var isGroupChannel = false

    var photo50: String = ""
    var photo100: String = ""
    var photo200: String = ""

    @Embedded(prefix = "cUser")
    var peerUser: VKUser? = null

    @Embedded(prefix = "cGroup")
    var peerGroup: VKGroup? = null

    constructor(o: JSONObject) : this() {
        inRead = o.optInt("in_read")
        outRead = o.optInt("out_read")
        lastMessageId = o.optInt("last_message_id", -1)
        unreadCount = o.optInt("unread_count", 0)

        o.optJSONObject("peer")?.let {
            conversationId = it.optInt("id", -1)
            type = it.optString("type")
            localId = it.optInt("local_id")
        }

        o.optJSONObject("push_settings")?.let {
            disabledUntil = it.optInt("disabled_until")
            isDisabledForever = it.optBoolean("disabled_forever")
            isNoSound = it.optBoolean("no_sound")
        }

        o.optJSONObject("can_write")?.let {
            isAllowed = it.optBoolean("allowed")
            reason = it.optInt("reason", -1)
        }

        o.optJSONObject("chat_settings")?.let {
            membersCount = it.optInt("members_count")
            title = it.optString("title")

            it.optJSONObject("pinned_message")?.let { pinned ->
                pinnedMessageId = VKPinnedMessage(pinned).id
            }

            state = it.optString("state")

            it.optJSONObject("photo")?.let { photo ->
                photo50 = photo.optString("photo_50")
                photo100 = photo.optString("photo_100")
                photo200 = photo.optString("photo_200")
            }

            isGroupChannel = it.optBoolean("is_group_channel")
        }
    }

    fun isNotificationsDisabled() = (isDisabledForever || disabledUntil > 0 || isNoSound)

    fun isChatId() = conversationId > 2_000_000_000

    fun isChat() = type == TYPE_CHAT

    fun isUser() = type == TYPE_USER

    fun isNotUser() = !isUser()

    fun isGroup() = type == TYPE_GROUP

    override fun toString(): String {
        return title
    }

    public override fun clone(): VKConversation {
        return super.clone() as VKConversation
    }
}
