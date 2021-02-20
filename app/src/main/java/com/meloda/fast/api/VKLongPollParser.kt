package com.meloda.fast.api

import android.util.Log
import androidx.annotation.WorkerThread
import com.meloda.fast.api.model.VKMessage
import com.meloda.fast.api.util.VKUtil
import com.meloda.fast.common.TaskManager
import com.meloda.fast.database.MemoryCache
import com.meloda.fast.event.EventInfo
import org.json.JSONArray

@Suppress("UNCHECKED_CAST")
class VKLongPollParser {

    companion object {

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

        private const val TAG = "VKLongPollParser"

        private fun messageEvent(item: JSONArray) {
            val message = VKUtil.parseLongPollMessage(item)

            TaskManager.execute {
                if (message.isFromUser()) {
                    VKUtil.searchUser(message.fromId)?.let { message.fromUser = it }
                } else {
                    VKUtil.searchGroup(message.fromId)?.let { message.fromGroup = it }
                }

                MemoryCache.getConversationById(message.peerId)?.let {
                    it.lastMessage = message
                    it.lastMessageId = message.messageId

                    MemoryCache.put(it)
                }

                MemoryCache.put(message)

                val info = EventInfo(VKApiKeys.NEW_MESSAGE, message)

                sendEvent(info)
            }
        }

        private fun messageEdit(item: JSONArray) {
            val message = VKUtil.parseLongPollMessage(item)
            val info = EventInfo(VKApiKeys.EDIT_MESSAGE, message)

            MemoryCache.put(message)

            sendEvent(info)
        }

        private fun messageDelete(item: JSONArray) {
            val messageId = item.optInt(1)
            val peerId = item.optInt(3)
            val info = EventInfo(VKApiKeys.DELETE_MESSAGE, arrayOf(peerId, messageId))

            MemoryCache.deleteMessage(messageId)

            sendEvent(info)
        }

        private fun messageRestored(item: JSONArray) {
            val message = VKUtil.parseLongPollMessage(item)
            val info = EventInfo(VKApiKeys.RESTORE_MESSAGE, message)

            MemoryCache.put(message)

            sendEvent(info)
        }

        private fun messageRead(item: JSONArray) {
            val messageId = item.optInt(1)
            val peerId = item.optInt(3)
            val info = EventInfo(VKApiKeys.READ_MESSAGE, arrayOf(peerId, messageId))

            MemoryCache.edit(MemoryCache.getMessageById(messageId)?.apply { isRead = true })

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
    }

    interface OnMessagesListener {
        fun onNewMessage(message: VKMessage)
        fun onEditMessage(message: VKMessage)
        fun onReadMessage(messageId: Int, peerId: Int)
        fun onDeleteMessage(messageId: Int, peerId: Int)
        fun onRestoredMessage(message: VKMessage)
    }

}