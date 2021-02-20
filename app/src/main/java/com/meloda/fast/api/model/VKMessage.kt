package com.meloda.fast.api.model

import android.util.ArrayMap
import androidx.room.*
import com.meloda.fast.api.util.VKUtil
import com.meloda.fast.database.dao.converters.ArrayListToByteArrayConverter
import com.meloda.fast.database.dao.converters.ForwardedConverter
import org.json.JSONObject

@SuppressWarnings(RoomWarnings.PRIMARY_KEY_FROM_EMBEDDED_IS_DROPPED)
@Entity(tableName = "messages")
open class VKMessage() : VKModel() {

    companion object {

        var profiles = arrayListOf<VKUser>()
        var groups = arrayListOf<VKGroup>()
        var conversations = arrayListOf<VKConversation>()

        const val serialVersionUID = 1L

        var lastHistoryCount = 0

        const val UNREAD = 1 // Оно просто есть
        const val OUTBOX = 1 shl 1 // Исходящее сообщение
        const val REPLIED = 1 shl 2 // На сообщение был создан ответ
        const val IMPORTANT = 1 shl 3 // Важное сообщение
        const val FRIENDS = 1 shl 5 // Сообщение в чат друга
        const val SPAM = 1 shl 6 // Сообщение помечено как спам
        const val DELETED = 1 shl 7 // Удаление сообщения
        const val AUDIO_LISTENED = 1 shl 12 // ГС прослушано
        const val CHAT = 1 shl 13 // Сообщение отправлено в беседу
        const val CANCEL_SPAM = 1 shl 15 // Отмена пометки спама
        const val HIDDEN = 1 shl 16 // Приветственное сообщение сообщества
        const val DELETE_FOR_ALL = 1 shl 17 // Сообщение удалено для всех
        const val CHAT_IN = 1 shl 19 // Входящее сообщение в беседе
        const val REPLY_MSG = 1 shl 21 // Ответ на сообщение

        val flags = ArrayMap<String, Int>()

        fun isOut(flags: Int): Boolean {
            return OUTBOX and flags > 0
        }

        fun isDeleted(flags: Int): Boolean {
            return DELETED and flags > 0
        }

        fun isUnread(flags: Int): Boolean {
            return UNREAD and flags > 0
        }

        fun isSpam(flags: Int): Boolean {
            return SPAM and flags > 0
        }

        fun isCanceledSpam(flags: Int): Boolean {
            return CANCEL_SPAM and flags > 0
        }

        fun isImportant(flags: Int): Boolean {
            return IMPORTANT and flags > 0
        }

        fun isDeletedForAll(flags: Int): Boolean {
            return DELETE_FOR_ALL and flags > 0
        }

        init {
            flags["unread"] = UNREAD
            flags["outbox"] = OUTBOX
            flags["replied"] = REPLIED
            flags["important"] = IMPORTANT
            flags["friends"] = FRIENDS
            flags["spam"] = SPAM
            flags["deleted"] = DELETED
            flags["audio_listened"] = AUDIO_LISTENED
            flags["chat"] = CHAT
            flags["cancel_spam"] = CANCEL_SPAM
            flags["hidden"] = HIDDEN
            flags["delete_for_all"] = DELETE_FOR_ALL
            flags["chat_in"] = CHAT_IN
            flags["reply_msg"] = REPLY_MSG
        }
    }

    @PrimaryKey(autoGenerate = false)
    var messageId = 0
    var date = 0
    var peerId = 0
    var fromId = 0
    var editTime = 0
    var isOut = false
    var text: String = ""
    var randomId = 0
    var conversationMessageId = 0

    var hasEmoji = false
    var isImportant = false
    var isRead = false

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    @TypeConverters(ArrayListToByteArrayConverter::class)
    var attachments: ArrayList<VKModel> = arrayListOf()

    //    @Ignore
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    @TypeConverters(ForwardedConverter::class)
    var fwdMessages: ArrayList<VKMessage> = arrayListOf()

    var replyMessageId = 0

    @Embedded(prefix = "mAction")
    var action: VKMessageAction? = null

    @Embedded(prefix = "mUser")
    var fromUser: VKUser? = null

    @Embedded(prefix = "Group")
    var fromGroup: VKGroup? = null

    constructor(o: JSONObject) : this() {
        messageId = o.optInt("id", -1)
        date = o.optInt("date")
        peerId = o.optInt("peer_id", -1)
        fromId = o.optInt("from_id", -1)
        editTime = o.optInt("edit_time", -1)
        isOut = o.optInt("out") == 1

        text = VKUtil.prepareMessageText(o.optString("text"))

        randomId = o.optInt("random_id", -1)
        conversationMessageId = o.optInt("conversation_message_id", -1)
        isImportant = o.optBoolean("important")

        o.optJSONArray("attachments")?.let {
            attachments = VKAttachments.parse(it)
        }

        o.optJSONArray("fwd_messages")?.let {
            val fwdMessages = ArrayList<VKMessage>(it.length())
            for (i in 0 until it.length()) {
                fwdMessages.add(VKMessage(it.optJSONObject(i)))
            }
            this.fwdMessages = fwdMessages
        }

        o.optJSONObject("reply_message")?.let {
            replyMessageId = VKMessage(it).messageId
        }

        o.optJSONObject("action")?.let {
            action = VKMessageAction(it)
        }
    }

    fun getForwardedMessages() = ArrayList<VKMessage>().apply {
        for (model in fwdMessages) add(model)
    }

    fun isFromUser() = fromId > 0

    fun isFromGroup() = fromId < 0

    fun isOutbox() = isOut

    fun isInbox() = !isOutbox()

    fun isReaded() = isRead

    fun isUnreaded() = !isReaded()

    override fun toString(): String {
        return if (text.isNotEmpty()) {
            text
        } else {
            super.toString()
        }
    }
}