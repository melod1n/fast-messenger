package com.meloda.fast.database.storage

import android.content.ContentValues
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import androidx.annotation.WorkerThread
import com.meloda.fast.database.CacheStorage
import com.meloda.fast.database.CacheStorage.messagesStorage
import com.meloda.fast.database.DatabaseKeys.CHAT_STATE
import com.meloda.fast.database.DatabaseKeys.CONVERSATION_ID
import com.meloda.fast.database.DatabaseKeys.IN_READ_MESSAGE_ID
import com.meloda.fast.database.DatabaseKeys.IS_ALLOWED
import com.meloda.fast.database.DatabaseKeys.IS_GROUP_CHANNEL
import com.meloda.fast.database.DatabaseKeys.IS_NOTIFICATIONS_DISABLED
import com.meloda.fast.database.DatabaseKeys.LAST_MESSAGE_ID
import com.meloda.fast.database.DatabaseKeys.LOCAL_ID
import com.meloda.fast.database.DatabaseKeys.MEMBERS_COUNT
import com.meloda.fast.database.DatabaseKeys.NOT_ALLOWED_REASON
import com.meloda.fast.database.DatabaseKeys.OUT_READ_MESSAGE_ID
import com.meloda.fast.database.DatabaseKeys.PHOTOS
import com.meloda.fast.database.DatabaseKeys.PINNED_MESSAGE_ID
import com.meloda.fast.database.DatabaseKeys.TITLE
import com.meloda.fast.database.DatabaseKeys.TYPE
import com.meloda.fast.database.DatabaseKeys.UNREAD_COUNT
import com.meloda.fast.database.DatabaseUtils.TABLE_CHATS
import com.meloda.fast.database.base.Storage
import com.meloda.fast.api.model.VKConversation
import com.meloda.fast.api.util.VKUtil
import org.json.JSONObject

@WorkerThread
class ChatsStorage : Storage<VKConversation>() {

    override val tag = "ChatsStorage"

    override fun getAllValues(): ArrayList<VKConversation> {
        val cursor = CacheStorage.selectCursor(TABLE_CHATS)
        val conversations = ArrayList<VKConversation>()

        while (cursor.moveToNext()) conversations.add(parseValue(cursor))

        cursor.close()

        return conversations
    }

    @WorkerThread
    override fun insertValues(values: ArrayList<VKConversation>, params: Bundle?) {
        if (values.isEmpty()) return

        database.beginTransaction()

        val contentValues = ContentValues()

        for (value in values) {
            cacheValue(contentValues, value, params)

            database.insert(TABLE_CHATS, null, contentValues)

            contentValues.clear()
        }

        database.setTransactionSuccessful()
        database.endTransaction()

        Log.d(tag, "Successful cached chats")
    }

    @WorkerThread
    override fun cacheValue(values: ContentValues, value: VKConversation, params: Bundle?) {
        values.put(CONVERSATION_ID, value.id)
        values.put(IS_ALLOWED, value.isAllowed)
        values.put(NOT_ALLOWED_REASON, value.notAllowedReason.value)
        values.put(IN_READ_MESSAGE_ID, value.inReadMessageId)
        values.put(OUT_READ_MESSAGE_ID, value.outReadMessageId)
        values.put(LAST_MESSAGE_ID, value.lastMessageId)
        values.put(UNREAD_COUNT, value.unreadCount)
        values.put(LOCAL_ID, value.localId)
        values.put(IS_NOTIFICATIONS_DISABLED, value.notificationsEnabled)
        values.put(MEMBERS_COUNT, value.membersCount)
        values.put(TITLE, value.title)
        values.put(IS_GROUP_CHANNEL, value.isGroupChannel)
        values.put(TYPE, value.intType)
        values.put(CHAT_STATE, value.intState)

        values.put(
            PHOTOS,
            VKUtil.putPhotosToJson(
                value.photo50,
                value.photo100,
                value.photo200
            ).toString()
        )

        value.pinnedMessage?.let {
            values.put(PINNED_MESSAGE_ID, it.id)
        }
    }

    @WorkerThread
    override fun parseValue(cursor: Cursor): VKConversation {
        val conversation = VKConversation()

        conversation.id = CacheStorage.getInt(cursor, CONVERSATION_ID)
        conversation.isAllowed = CacheStorage.getInt(cursor, IS_ALLOWED) == 1
        conversation.notAllowedReason = VKConversation.Reason.fromInt(
            CacheStorage.getInt(cursor, NOT_ALLOWED_REASON)
        )
        conversation.inReadMessageId = CacheStorage.getInt(cursor, IN_READ_MESSAGE_ID)
        conversation.outReadMessageId = CacheStorage.getInt(cursor, OUT_READ_MESSAGE_ID)
        conversation.unreadCount = CacheStorage.getInt(cursor, UNREAD_COUNT)
        conversation.localId = CacheStorage.getInt(cursor, LOCAL_ID)
        conversation.notificationsEnabled =
            CacheStorage.getInt(cursor, IS_NOTIFICATIONS_DISABLED) == 1
        conversation.membersCount = CacheStorage.getInt(cursor, MEMBERS_COUNT)
        conversation.title = CacheStorage.getString(cursor, TITLE)
        conversation.isGroupChannel = CacheStorage.getInt(cursor, IS_GROUP_CHANNEL) == 1

        val pinnedMessageId = CacheStorage.getInt(cursor, PINNED_MESSAGE_ID)
        if (pinnedMessageId != -1) {
            val pinnedMessage = messagesStorage.getMessageById(pinnedMessageId)
            if (pinnedMessage != null) conversation.pinnedMessage = pinnedMessage
        }

        conversation.intType = CacheStorage.getInt(cursor, TYPE)
        conversation.intState = CacheStorage.getInt(cursor, CHAT_STATE)

        conversation.lastMessageId = CacheStorage.getInt(cursor, LAST_MESSAGE_ID)
        val lastMessage = messagesStorage.getMessageById(conversation.lastMessageId)
        if (lastMessage != null) conversation.lastMessage = lastMessage

        val photos = VKUtil.parseJsonPhotos(JSONObject(CacheStorage.getString(cursor, PHOTOS)))
        conversation.photo50 = photos[0]
        conversation.photo100 = photos[1]
        conversation.photo200 = photos[2]

        return conversation
    }

}