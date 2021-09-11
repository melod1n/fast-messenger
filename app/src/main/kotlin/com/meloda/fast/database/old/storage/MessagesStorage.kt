package com.meloda.fast.database.old.storage

import android.content.ContentValues
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import androidx.annotation.WorkerThread
import com.meloda.fast.database.old.CacheStorage
import com.meloda.fast.database.old.CacheStorage.selectCursor
import com.meloda.fast.database.old.DatabaseKeys.ACTION
import com.meloda.fast.database.old.DatabaseKeys.ATTACHMENTS
import com.meloda.fast.database.old.DatabaseKeys.CONVERSATION_MESSAGE_ID
import com.meloda.fast.database.old.DatabaseKeys.DATE
import com.meloda.fast.database.old.DatabaseKeys.EDIT_TIME
import com.meloda.fast.database.old.DatabaseKeys.FROM_ID
import com.meloda.fast.database.old.DatabaseKeys.FWD_MESSAGES
import com.meloda.fast.database.old.DatabaseKeys.IS_OUT
import com.meloda.fast.database.old.DatabaseKeys.MESSAGE_ID
import com.meloda.fast.database.old.DatabaseKeys.PEER_ID
import com.meloda.fast.database.old.DatabaseKeys.RANDOM_ID
import com.meloda.fast.database.old.DatabaseKeys.REPLY_MESSAGE_ID
import com.meloda.fast.database.old.DatabaseKeys.TEXT
import com.meloda.fast.database.old.DatabaseUtils.TABLE_MESSAGES
import com.meloda.fast.database.old.base.Storage
import com.meloda.fast.util.Utils
import com.meloda.fast.api.model.old.oldVKMessage
import com.meloda.fast.api.model.old.oldVKMessageAction
import com.meloda.fast.api.model.old.VKModel
import java.util.stream.Collectors

@WorkerThread
@Suppress("UNCHECKED_CAST")
class MessagesStorage : Storage<oldVKMessage>() {

    override val tag = "MessagesStorage"

    @WorkerThread
    fun getMessagesHistory(peerId: Int): ArrayList<oldVKMessage> {
        val cursor = CacheStorage.selectCursor(TABLE_MESSAGES, PEER_ID, peerId)

        val messages = ArrayList<oldVKMessage>(cursor.count)
        while (cursor.moveToNext()) messages.add(parseValue(cursor))

        cursor.close()

        return messages
    }

    @WorkerThread
    fun getMessageById(messageId: Int): oldVKMessage? {
        val cursor = CacheStorage.selectCursor(TABLE_MESSAGES, MESSAGE_ID, messageId)

        if (cursor.moveToFirst()) {
            val message = parseValue(cursor)
            cursor.close()

            return message
        }

        return null
    }

    override fun getAllValues(): ArrayList<oldVKMessage> {
        val cursor = selectCursor(TABLE_MESSAGES)
        val messages = ArrayList<oldVKMessage>()

        while (cursor.moveToNext()) messages.add(parseValue(cursor))

        cursor.close()

        return messages
    }

    @WorkerThread
    override fun insertValues(values: ArrayList<oldVKMessage>, params: Bundle?) {
        if (values.isEmpty()) return

        database.beginTransaction()

        val contentValues = ContentValues()

        for (value in values) {
            cacheValue(contentValues, value)

            database.insert(TABLE_MESSAGES, null, contentValues)

            contentValues.clear()
        }

        database.setTransactionSuccessful()
        database.endTransaction()

        Log.d(tag, "Successful cached messages")
    }

    @WorkerThread
    override fun cacheValue(values: ContentValues, value: oldVKMessage, params: Bundle?) {
        values.put(MESSAGE_ID, value.id)
        values.put(DATE, value.date)
        values.put(PEER_ID, value.peerId)
        values.put(FROM_ID, value.fromId)
        values.put(EDIT_TIME, value.editTime)
        values.put(TEXT, value.text)
        values.put(RANDOM_ID, value.randomId)
        values.put(CONVERSATION_MESSAGE_ID, value.conversationMessageId)

        value.replyMessage?.let {
            values.put(REPLY_MESSAGE_ID, it.id)
        }

        value.action?.let {
            values.put(ACTION, Utils.serialize(it))
        }

        value.attachments.let {
            if (it.isNotEmpty()) {
                values.put(ATTACHMENTS, Utils.serialize(it))
            }
        }

        value.fwdMessages.let {
            if (it.isNotEmpty()) {
                val ids = arrayListOf<String>()
                it.forEach { message -> ids.add(message.id.toString()) }

                ids.stream().collect(Collectors.joining(",")).let { str ->
                    values.put(FWD_MESSAGES, str)
                }
            }
        }
    }

    @WorkerThread
    override fun parseValue(cursor: Cursor): oldVKMessage {
        val message = oldVKMessage()

        message.id = CacheStorage.getInt(cursor, MESSAGE_ID)
        message.date = CacheStorage.getInt(cursor, DATE)
        message.peerId = CacheStorage.getInt(cursor, PEER_ID)
        message.fromId = CacheStorage.getInt(cursor, FROM_ID)
        message.editTime = CacheStorage.getInt(cursor, EDIT_TIME)
        message.isOut = CacheStorage.getInt(cursor, IS_OUT) == 1
        message.text = CacheStorage.getString(cursor, TEXT)
        message.randomId = CacheStorage.getInt(cursor, RANDOM_ID)
        message.conversationMessageId = CacheStorage.getInt(cursor, CONVERSATION_MESSAGE_ID)

        val blobAttachments = Utils.deserialize(CacheStorage.getBlob(cursor, ATTACHMENTS))
        if (blobAttachments != null) message.attachments = blobAttachments as ArrayList<VKModel>
        else message.attachments = arrayListOf()

        val replyMessageId = CacheStorage.getInt(cursor, REPLY_MESSAGE_ID)
        val replyMessage = getMessageById(replyMessageId)
        if (replyMessage != null) message.replyMessage = replyMessage

        val blobAction = Utils.deserialize(CacheStorage.getBlob(cursor, ACTION))
        if (blobAction != null) message.action = blobAction as oldVKMessageAction

        val stringFwdMessages = CacheStorage.getString(cursor, FWD_MESSAGES)
        if (stringFwdMessages != null) {
            val split = stringFwdMessages.split(',')

            val ids = arrayListOf<Int>()
            for (s in split) ids.add(s.toInt())

            val fwdMessages = arrayListOf<oldVKMessage>()

            ids.forEach {
                val fwdMessage = getMessageById(it)
                if (fwdMessage != null) fwdMessages.add(fwdMessage)
            }

            message.fwdMessages = fwdMessages
        } else message.fwdMessages = arrayListOf()

        return message
    }

}