package com.meloda.fast

import android.util.Log
import androidx.annotation.WorkerThread
import com.meloda.concurrent.EventInfo
import com.meloda.concurrent.TaskManager
import com.meloda.vksdk.VKApiKeys
import com.meloda.vksdk.model.VKMessage
import com.meloda.vksdk.util.VKUtil
import org.json.JSONArray

@Suppress("UNCHECKED_CAST")
object VKLongPollParser {


    @WorkerThread
    fun parse(updates: JSONArray) {
        if (updates.length() == 0) {
            return
        }

        for (i in 0 until updates.length()) {
            val item = updates.optJSONArray(i)
            when (item.optInt(0)) {
                2 -> messageSetFlags(item)
                3 -> messageClearFlags(item)
                4 -> messageEvent(item)
                5 -> messageEdit(item)
            }
        }
    }

    fun parseEvent(eventInfo: EventInfo<*>, onMessagesListener: OnMessagesListener) {
        when (eventInfo.key) {
            VKApiKeys.NEW_MESSAGE.value -> onMessagesListener.onNewMessage(eventInfo.data as VKMessage)
            VKApiKeys.EDIT_MESSAGE.value -> onMessagesListener.onEditMessage(eventInfo.data as VKMessage)
            VKApiKeys.RESTORE_MESSAGE.value -> onMessagesListener.onRestoredMessage(eventInfo.data as VKMessage)
            VKApiKeys.DELETE_MESSAGE.value -> {
                val array = eventInfo.data as Array<Int>
                onMessagesListener.onDeleteMessage(array[0], array[1])
            }
            VKApiKeys.READ_MESSAGE.value -> {
                val array = eventInfo.data as Array<Int>
                onMessagesListener.onReadMessage(array[0], array[1])
            }
        }
    }

    private const val TAG = "VKLongPollParser"

    private fun messageEvent(item: JSONArray) {
        val message = VKUtil.parseLongPollMessage(item)

        TaskManager.execute {
            if (message.isFromUser()) {
//                    VKUtil.searchUser(message.fromId)?.let { message.fromUser = it }
            } else {
//                    VKUtil.searchGroup(message.fromId)?.let { message.fromGroup = it }
            }

//                MemoryCache.getConversationById(message.peerId)?.let {
//                    it.lastMessage = message
//                    it.lastMessageId = message.messageId
//
//                    MemoryCache.put(it)
//                }
//
//                MemoryCache.put(message)

            val info = EventInfo(VKApiKeys.NEW_MESSAGE.name, message)

            sendEvent(info)
        }
    }

    private fun messageEdit(item: JSONArray) {
        val message = VKUtil.parseLongPollMessage(item)
        val info = EventInfo(VKApiKeys.EDIT_MESSAGE.name, message)

//            MemoryCache.put(message)

        sendEvent(info)
    }

    private fun messageDelete(item: JSONArray) {
        val messageId = item.optInt(1)
        val peerId = item.optInt(3)
        val info = EventInfo(VKApiKeys.DELETE_MESSAGE.name, arrayOf(peerId, messageId))

//            MemoryCache.deleteMessage(messageId)

        sendEvent(info)
    }

    private fun messageRestored(item: JSONArray) {
        val message = VKUtil.parseLongPollMessage(item)
        val info = EventInfo(VKApiKeys.RESTORE_MESSAGE.name, message)

//            MemoryCache.put(message)

        sendEvent(info)
    }

    private fun messageRead(item: JSONArray) {
        val messageId = item.optInt(1)
        val peerId = item.optInt(3)
        val info = EventInfo(VKApiKeys.READ_MESSAGE.name, arrayOf(peerId, messageId))

//            MemoryCache.edit(MemoryCache.getMessageById(messageId)?.apply { isRead = true })

        sendEvent(info)
    }

    private fun messageClearFlags(item: JSONArray) {
        val id = item.optInt(1)
        val flags = item.optInt(2)
        if (VKUtil.isMessageHasFlag(flags, "cancel_spam")) {
            Log.i(TAG, "Message with id $id: Not spam")
        }
        if (VKUtil.isMessageHasFlag(flags, "deleted")) {
            messageRestored(item)
        }
        if (VKUtil.isMessageHasFlag(flags, "important")) {
            Log.i(TAG, "Message with id $id: Not Important")
        }
        if (VKUtil.isMessageHasFlag(flags, "unread")) {
            messageRead(item)
        }
    }

    private fun messageSetFlags(item: JSONArray) {
        val id = item.optInt(1)
        val flags = item.optInt(2)
        if (VKUtil.isMessageHasFlag(flags, "delete_for_all")) {
            messageDelete(item)
        }
        if (VKUtil.isMessageHasFlag(flags, "deleted")) {
            messageDelete(item)
        }
        if (VKUtil.isMessageHasFlag(flags, "spam")) {
            Log.i(TAG, "Message with id $id: Spam")
        }
        if (VKUtil.isMessageHasFlag(flags, "important")) {
            Log.i(TAG, "Message with id $id: Important")
        }
    }

    private fun sendEvent(eventInfo: EventInfo<*>) {
        TaskManager.sendEvent(eventInfo)
    }

    interface OnMessagesListener {
        fun onNewMessage(message: VKMessage)
        fun onEditMessage(message: VKMessage)
        fun onRestoredMessage(message: VKMessage)
        fun onDeleteMessage(peerId: Int, messageId: Int)
        fun onReadMessage(peerId: Int, messageId: Int)
    }

}