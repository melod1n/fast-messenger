package com.meloda.fast.api.model

import org.json.JSONObject

class VKConversation() : VKModel(), Cloneable {

    override val attachmentType = VKAttachments.Type.NONE

    companion object {
        const val serialVersionUID: Long = 1L

        var profiles = arrayListOf<VKUser>()
        var groups = arrayListOf<VKGroup>()

        var conversationsCount: Int = 0

        var count: Int = 0
    }

    var isAllowed: Boolean = false
    var notAllowedReason: Reason = Reason.NULL

    var inReadMessageId: Int = 0
    var outReadMessageId: Int = 0
    var lastMessageId: Int = 0
    var unreadCount: Int = 0

    var id: Int = 0

    var intType: Int = 0
    var type: Type = Type.NULL

    var localId: Int = 0

    var notificationsEnabled: Boolean = false

    var disabledUntil: Int = 0
    var isDisabledForever: Boolean = false
    var isNoSound: Boolean = false

    var membersCount: Int = 0
    var title: String = ""

    var pinnedMessage: VKMessage? = null

    var intState: Int = 0
    var state: State = State.IN

    var lastMessage: VKMessage = VKMessage()

    var isGroupChannel: Boolean = false

    var photo50: String = ""
    var photo100: String = ""
    var photo200: String = ""

    var peerUser: VKUser? = null

    var peerGroup: VKGroup? = null

    constructor(o: JSONObject) : this() {
        inReadMessageId = o.optInt("in_read")
        outReadMessageId = o.optInt("out_read")
        lastMessageId = o.optInt("last_message_id", -1)
        unreadCount = o.optInt("unread_count", 0)

        o.optJSONObject("peer")?.let {
            id = it.optInt("id", -1)
            type = Type.fromString(it.optString("type"))
            localId = it.optInt("local_id")
        }

        o.optJSONObject("push_settings")?.let {
            disabledUntil = it.optInt("disabled_until")
            isDisabledForever = it.optBoolean("disabled_forever")
            isNoSound = it.optBoolean("no_sound")
        }

        o.optJSONObject("can_write")?.let {
            isAllowed = it.optBoolean("allowed")
            notAllowedReason = Reason.fromInt(it.optInt("reason", -1))
        }

        o.optJSONObject("chat_settings")?.let {
            membersCount = it.optInt("members_count")
            title = it.optString("title")

            it.optJSONObject("pinned_message")?.let { pinned ->
                pinnedMessage = VKMessage(pinned)
            }

            state = State.fromString(it.optString("state"))

            it.optJSONObject("photo")?.let { photo ->
                photo50 = photo.optString("photo_50")
                photo100 = photo.optString("photo_100")
                photo200 = photo.optString("photo_200")
            }

            isGroupChannel = it.optBoolean("is_group_channel")
        }
    }

    fun isNotificationsDisabled() = (isDisabledForever || disabledUntil > 0 || isNoSound)

    fun isChat() = type == Type.CHAT

    fun isUser() = type == Type.USER

    fun isGroup() = type == Type.GROUP

    override fun toString(): String {
        return title
    }

    public override fun clone(): VKConversation {
        return super.clone() as VKConversation
    }

    enum class Type(val value: String) {
        NULL("null"),
        USER("user"),
        CHAT("chat"),
        GROUP("group");

        companion object {
            fun fromString(value: String) = values().first { it.value == value }
        }
    }

    enum class State(val value: String) {
        IN("in"),
        KICKED("kicked"),
        LEFT("left");

        companion object {
            fun fromString(value: String) = values().first { it.value == value }
        }
    }

    enum class Reason(val value: Int) {
        NULL(-1),
        U(0),
        BLOCKED_DELETED(18),
        BLACKLISTED(900),
        BLOCKED_GROUP_MESSAGES(901),
        PRIVACY_SETTINGS(902),
        GROUP_DISABLED_MESSAGES(915),
        GROUP_BLOCKED_MESSAGES(916),
        NO_ACCESS_CHAT(917),
        NO_ACCESS_EMAIL(918),
        U1(925),
        NO_ACCESS_COMMUNITY(203);

        companion object {
            fun fromInt(value: Int) = values().first { it.value == value }
        }
    }
}
