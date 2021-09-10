package com.meloda.fast.api

import androidx.annotation.WorkerThread
import com.meloda.fast.api.model.*
import com.meloda.fast.api.network.VKErrors
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

// TODO: 8/31/2021 review
object VKUtil {

    private const val TAG = "VKUtil"

    fun isValidationRequired(throwable: Throwable): Boolean {
        if (throwable !is VKException) return false
        return throwable.error == VKErrors.NEED_VALIDATION
    }

    fun isCaptchaRequired(throwable: Throwable): Boolean {
        if (throwable !is VKException) return false
        return throwable.error == VKErrors.NEED_CAPTCHA
    }

    fun sortMessagesByDate(
        values: ArrayList<oldVKMessage>,
        firstOnTop: Boolean
    ): ArrayList<oldVKMessage> {
        values.sortWith { m1, m2 ->
            val d1 = m1.date
            val d2 = m2.date

            if (firstOnTop) {
                d2 - d1
            } else {
                d1 - d2
            }
        }

        return values
    }

    fun sortConversationsByDate(
        values: ArrayList<oldVKConversation>,
        firstOnTop: Boolean
    ): ArrayList<oldVKConversation> {
        values.sortWith { c1, c2 ->
            val d1 = c1.lastMessage.date
            val d2 = c2.lastMessage.date

            return@sortWith if (firstOnTop) {
                d2 - d1
            } else {
                d1 - d2
            }
        }

        return values
    }

    fun prepareMessageText(message: String): String {
        if (message.isEmpty()) return message

        var newText = message

        val mentions = hashMapOf<String, String>()

        var startFrom = 0

        while (true) {
            val leftBracketIndex = newText.indexOf('[', startFrom)
            val verticalLineIndex = newText.indexOf('|', startFrom)
            val rightBracketIndex = newText.indexOf(']', startFrom)

            if (leftBracketIndex == -1 ||
                verticalLineIndex == -1 ||
                rightBracketIndex == -1
            ) {
                break
            }

            val id = newText.substring(leftBracketIndex + 1, verticalLineIndex)

            if (!id.matches(Regex("^id(\\d+)\$")) || rightBracketIndex - verticalLineIndex < 2) {
                break
            }

            val text = newText.substring(verticalLineIndex + 1, rightBracketIndex)

            val str = "[$id|$text]"

            mentions[str] = text
            startFrom = rightBracketIndex + 1
        }

        mentions.forEach {
            newText = newText.replace(it.key, it.value)
        }

        return newText
    }

//    fun removeTime(date: Date): Long {
//        return Calendar.getInstance().apply {
//            time = date
//            this[Calendar.HOUR_OF_DAY] = 0
//            this[Calendar.MINUTE] = 0
//            this[Calendar.SECOND] = 0
//            this[Calendar.MILLISECOND] = 0
//        }.timeInMillis
//    }


    //TODO: нормальное время
    fun getLastSeenTime(date: Long): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
    }


    fun getTitle(
        conversation: oldVKConversation,
        peerUser: oldVKUser?,
        peerGroup: VKGroup?
    ): String {
        return when {
            conversation.isUser() -> peerUser?.let { return it.toString() } ?: ""


            conversation.isGroup() -> peerGroup?.let { return it.name } ?: ""


            conversation.isChat() -> conversation.title ?: ""

            else -> ""
        }
    }

    fun getMessageTitle(
        message: oldVKMessage,
        fromUser: oldVKUser?,
        fromGroup: VKGroup?
    ): String {
        return when {
            message.isFromUser() -> {
                fromUser?.let { return it.toString() } ?: ""
            }

            message.isFromGroup() -> {
                fromGroup?.let { return it.name } ?: ""
            }

            else -> ""
        }
    }

    fun getAvatar(
        conversation: oldVKConversation,
        peerUser: oldVKUser?,
        peerGroup: VKGroup?
    ): String {
        return when {
            conversation.isUser() -> {
                peerUser?.let { return it.photo200 } ?: ""
            }

            conversation.isGroup() -> {
                peerGroup?.let { return it.photo200 } ?: ""
            }

            conversation.isChat() -> {
                conversation.photo200
            }

            else -> ""
        }
    }

    fun getUserAvatar(
        message: oldVKMessage,
        fromUser: oldVKUser?,
        fromGroup: VKGroup?
    ): String {
        return when {
            message.isFromUser() -> {
                fromUser?.let { return it.photo100 } ?: ""
            }

            message.isFromGroup() -> {
                fromGroup?.let { return it.photo100 } ?: ""
            }

            else -> ""
        }
    }

    fun getUserPhoto(user: oldVKUser): String {
        if (user.photo200.isEmpty()) {
            if (user.photo100.isEmpty()) {
                if (user.photo50.isEmpty()) {
                    return ""
                }
            } else {
                return user.photo100
            }
        } else {
            return user.photo200
        }

        return ""
    }

    fun getGroupPhoto(group: VKGroup): String {
        if (group.photo200.isEmpty()) {
            if (group.photo100.isEmpty()) {
                if (group.photo50.isEmpty()) {
                    return ""
                }
            } else {
                return group.photo100
            }
        } else {
            return group.photo200
        }

        return ""
    }


    fun parseConversations(array: JSONArray): ArrayList<oldVKConversation> {
        val conversations = arrayListOf<oldVKConversation>()
        for (i in 0 until array.length()) {
            conversations.add(oldVKConversation(array.optJSONObject(i)))
        }

        return conversations
    }

    fun parseMessages(array: JSONArray): ArrayList<oldVKMessage> {
        val messages = arrayListOf<oldVKMessage>()
        for (i in 0 until array.length()) {
            messages.add(oldVKMessage(array.optJSONObject(i)))
        }

        return messages
    }

    fun isMessageHasFlag(mask: Int, flagName: String): Boolean {
        val o: Any? = oldVKMessage.flags[flagName]
        return if (o != null) { //has flag
            val flag = o as Int
            flag and mask > 0
        } else false
    }

    //TODO: rewrite parsing
    //fromUser and fromGroup are null
    @Deprecated("need to rewrite")
    @WorkerThread
    fun parseLongPollMessage(array: JSONArray): oldVKMessage {
        val message = oldVKMessage()

        val id = array.optInt(1)
        val flags = array.optInt(2)
        val peerId = array.optInt(3)
        val date = array.optInt(4)
        val text = array.optString(5)

        message.id = id
        message.peerId = peerId
        message.date = date
        message.text = text

//        val fromId =
//            if (isMessageHasFlag(flags, "outbox")) com.meloda.fast.api.UserConfig.userId
//            else peerId

        message.fromId = peerId

        array.optJSONObject(6)?.let {
            if (it.has("emoji")) message.hasEmoji = true

            if (it.has("from")) {
                message.fromId = it.optInt("from", -1)
            }

            if (it.has("source_act")) {
                message.action = VKMessageAction().also { action ->
                    action.type =
                        VKMessageAction.Type.fromString(it.optString("source_act"))

                    when (action.type) {
                        VKMessageAction.Type.CHAT_CREATE -> {
                            action.text = it.optString("source_text")
                        }
                        VKMessageAction.Type.TITLE_UPDATE -> {
                            action.oldText = it.optString("source_old_text")
                            action.text = it.optString("source_text")
                        }
                        VKMessageAction.Type.PIN_MESSAGE -> {
                            action.memberId = it.optInt("source_mid")
                            action.conversationMessageId = it.optInt("source_chat_local_id")

                            it.optJSONObject("source_message")?.let { message ->
                                action.message = oldVKMessage(message)
                            }
                        }
                        VKMessageAction.Type.UNPIN_MESSAGE -> {
                            action.memberId = it.optInt("source_mid")
                            action.conversationMessageId = it.optInt("source_chat_local_id")
                        }
                        VKMessageAction.Type.INVITE_USER,
                        VKMessageAction.Type.KICK_USER,
                        VKMessageAction.Type.SCREENSHOT,
                        VKMessageAction.Type.INVITE_USER_BY_CALL -> {
                            action.memberId = it.optInt("source_mid")
                        }
                    }
                }
            }
        }

        array.optJSONObject(7)?.let {
            /**
             *
             *    fwd?   reply?   attachments_count?  attachments?
             *
             */
        }

        val randomId = array.optInt(8)
        message.randomId = randomId

        val conversationMessageId = array.optInt(9)
        message.conversationMessageId = conversationMessageId

        val editTime = array.optInt(10)
        message.editTime = editTime

//        val out = fromId == com.meloda.fast.api.UserConfig.userId
//        message.isOut = out
//
//        if (message.isFromUser()) {
//            message.fromUser = MemoryCache.getUserById(fromId)
//        } else {
//            message.fromGroup = MemoryCache.getGroupById(abs(fromId))
//        }

        return message
    }

    fun parseJsonPhotos(jsonPhotos: JSONObject): List<String> {
        val photos = arrayListOf<String>()

        for (key in jsonPhotos.keys()) {
            photos.add(jsonPhotos.getString(key))
        }

        return photos
    }

    fun putPhotosToJson(photo50: String, photo100: String, photo200: String): JSONObject {
        val json = JSONObject()

        json.put("photo_50", photo50)
        json.put("photo_100", photo100)
        json.put("photo_200", photo200)

        return json
    }

    fun isGroupId(id: Int) = id < 0

    fun isUserId(id: Int) = id in 1..1999999999

    fun isChatId(id: Int) = id > 2_000_000_000

}