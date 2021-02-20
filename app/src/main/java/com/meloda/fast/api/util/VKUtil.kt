package com.meloda.fast.api.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.core.content.ContextCompat
import com.amulyakhare.textdrawable.TextDrawable
import com.meloda.fast.BuildConfig
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VKApiKeys
import com.meloda.fast.api.model.*
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.common.TaskManager
import com.meloda.fast.database.MemoryCache
import com.meloda.fast.extensions.ContextExtensions.color
import com.meloda.fast.extensions.ContextExtensions.drawable
import com.meloda.fast.extensions.StringExtensions.lowerCase
import com.meloda.fast.listener.OnResponseListener
import com.meloda.fast.util.TextUtils
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.math.abs

object VKUtil {

    private const val TAG = "VKM: VKUtil"

    fun extractPattern(string: String, pattern: String): String? {
        val p = Pattern.compile(pattern)
        val m = p.matcher(string)
        return if (!m.find()) null else m.toMatchResult().group(1)
    }

    private const val pattern_string_profile_id = "^(id)?(\\d{1,10})$"

    private val pattern_profile_id = Pattern.compile(pattern_string_profile_id)

    fun parseProfileId(text: String): String? {
        val m = pattern_profile_id.matcher(text)
        return if (!m.find()) null else m.group(2)
    }

    fun sortMessagesByDate(
        values: ArrayList<VKMessage>,
        firstOnTop: Boolean
    ): ArrayList<VKMessage> {
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
        values: ArrayList<VKConversation>,
        firstOnTop: Boolean
    ): ArrayList<VKConversation> {
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

    fun getUserOnline(user: VKUser): String {
        val r = AppGlobal.resources
        return if (user.isOnline) {
            if (user.isOnlineMobile) {
                r.getString(R.string.user_online_mobile)
            } else {
                r.getString(R.string.user_online)
            }
        } else {
            if (user.lastSeen == 0) {
                r.getString(R.string.user_last_seen_recently)
            } else {
                r.getString(R.string.user_last_seen_at, getLastSeenTime(user.lastSeen * 1000L))
            }
        }
    }

    fun getUserOnlineIcon(
        context: Context,
        conversation: VKConversation?,
        peerUser: VKUser?
    ): Drawable? {
        return if (conversation != null) {
            if (conversation.isUser() && peerUser != null) {
                if (!peerUser.isOnline) {
                    null
                } else {
                    ContextCompat.getDrawable(
                        context,
                        if (peerUser.isOnlineMobile) R.drawable.ic_online_mobile else R.drawable.ic_online_pc
                    )
                }
            } else null
        } else {
            if (peerUser!!.isOnline) {
                ContextCompat.getDrawable(
                    context,
                    if (peerUser.isOnlineMobile) R.drawable.ic_online_mobile else R.drawable.ic_online_pc
                )
            } else {
                null
            }
        }
    }

    fun getUserOnlineIcon(context: Context, user: VKUser): Drawable? {
        return getUserOnlineIcon(context, null, user)
    }

    //TODO: нормальное время
    fun getLastSeenTime(date: Long): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
    }

    fun getAvatarPlaceholder(context: Context, dialogTitle: String): TextDrawable {
        return TextDrawable.builder().buildRound(
            if (dialogTitle.isEmpty()) "" else {
                TextUtils.getFirstLetterFromString(dialogTitle)
            },
            context.color(R.color.accent)
        )
    }

    @WorkerThread
    fun searchUser(id: Int, onResponseListener: OnResponseListener<VKUser>? = null): VKUser? {
        return if (VKGroup.isGroupId(id) || isChatId(id)) {
            null
        } else {
            MemoryCache.getUserById(id)?.let { return it }

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "User with id $id not found")
            }

            TaskManager.loadUser(VKApiKeys.UPDATE_USER, id, onResponseListener)

            return null
        }
    }

    @WorkerThread
    fun searchGroup(id: Int, onResponseListener: OnResponseListener<VKGroup>? = null): VKGroup? {
        return if (!VKGroup.isGroupId(id) || isChatId(id)) {
            null
        } else {
            MemoryCache.getGroupById(abs(id))?.let { return it }

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Group with id $id not found")
            }

            TaskManager.loadGroup(VKApiKeys.UPDATE_GROUP, abs(id), onResponseListener)

            return null
        }
    }

    fun getTitle(conversation: VKConversation, peerUser: VKUser?, peerGroup: VKGroup?): String {
        return when {
            conversation.isUser() -> {
                peerUser?.let { return it.toString() } ?: ""
            }

            conversation.isGroup() -> {
                peerGroup?.let { return it.name } ?: ""
            }

            conversation.isChat() -> {
                conversation.title
            }

            else -> ""
        }
    }

    fun getMessageTitle(message: VKMessage, fromUser: VKUser?, fromGroup: VKGroup?): String {
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

    fun getAvatar(conversation: VKConversation, peerUser: VKUser?, peerGroup: VKGroup?): String {
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

    fun getUserAvatar(message: VKMessage, fromUser: VKUser?, fromGroup: VKGroup?): String {
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

    fun getUserPhoto(user: VKUser): String {
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

    fun getDialogType(context: Context, conversation: VKConversation): Drawable? {
        return when {
            conversation.isGroupChannel -> {
                ContextCompat.getDrawable(context, R.drawable.ic_dialog_type_channel)
            }
            conversation.isChat() -> {
                ContextCompat.getDrawable(context, R.drawable.ic_dialog_type_conversation)
            }
            else -> null
        }
    }

    fun getAttachmentText(context: Context, attachments: List<VKModel>): String {
        val resId: Int

        if (attachments.isNotEmpty()) {
            if (attachments.size > 1) {
                var oneType = true
                val className = attachments[0].javaClass.simpleName

                for (model in attachments) {
                    if (model.javaClass.simpleName != className) {
                        oneType = false
                        break
                    }
                }

                return if (oneType) {
                    val objectClass: Class<VKModel> = attachments[0].javaClass
                    resId = when (objectClass) {
                        VKPhoto::class.java -> {
                            R.string.message_attachment_photos
                        }
                        VKVideo::class.java -> {
                            R.string.message_attachment_videos
                        }
                        VKAudio::class.java -> {
                            R.string.message_attachment_audios
                        }
                        VKDoc::class.java -> {
                            R.string.message_attachment_docs
                        }
                        else -> {
                            -1
                        }
                    }
                    if (resId == -1) "Unknown attachments" else context.getString(
                        resId,
                        attachments.size
                    ).toLowerCase(Locale.getDefault())
                } else {
                    context.getString(R.string.message_attachments_many)
                }
            } else {
                val objectClass: Class<VKModel> = attachments[0].javaClass

                resId = when (objectClass) {
                    VKPhoto::class.java -> {
                        R.string.message_attachment_photo
                    }
                    VKAudio::class.java -> {
                        R.string.message_attachment_audio
                    }
                    VKVideo::class.java -> {
                        R.string.message_attachment_video
                    }
                    VKDoc::class.java -> {
                        R.string.message_attachment_doc
                    }
                    VKGraffiti::class.java -> {
                        R.string.message_attachment_graffiti
                    }
                    VKAudioMessage::class.java -> {
                        R.string.message_attachment_voice
                    }
                    VKSticker::class.java -> {
                        R.string.message_attachment_sticker
                    }
                    VKGift::class.java -> {
                        R.string.message_attachment_gift
                    }
                    VKLink::class.java -> {
                        R.string.message_attachment_link
                    }
                    VKPoll::class.java -> {
                        R.string.message_attachment_poll
                    }
                    VKCall::class.java -> {
                        R.string.message_attachment_call
                    }
                    else -> {
                        return "Unknown"
                    }
                }
            }
        } else {
            return ""
        }
        return context.getString(resId)
    }

    fun getAttachmentDrawable(context: Context, attachments: List<VKModel>): Drawable? {
        if (attachments.isEmpty() || attachments.size > 1) return null

        var resId = -1

        when (attachments[0].javaClass) {
            VKPhoto::class.java -> {
                resId = R.drawable.ic_message_attachment_camera
            }
            VKAudio::class.java -> {
                resId = R.drawable.ic_message_attachment_audio
            }
            VKVideo::class.java -> {
                resId = R.drawable.ic_message_attachment_video
            }
            VKDoc::class.java -> {
                resId = R.drawable.ic_message_attachment_doc
            }
            VKGraffiti::class.java -> {
                resId = R.drawable.ic_message_attachment_graffiti
            }
            VKAudioMessage::class.java -> {
                resId = R.drawable.ic_message_attachment_audio_message
            }
            VKSticker::class.java -> {
                resId = R.drawable.ic_message_attachment_sticker
            }
            VKGift::class.java -> {
                resId = R.drawable.ic_message_attachment_gift
            }
            VKLink::class.java -> {
                resId = R.drawable.ic_message_attachment_link
            }
            VKPoll::class.java -> {
                resId = R.drawable.ic_message_attachment_poll
            }
            VKCall::class.java -> {
                resId = R.drawable.ic_message_attachment_call
            }
        }

        if (resId != -1) {
            val drawable = context.drawable(resId)

            drawable?.setTint(context.color(R.color.accent))
            return drawable
        }
        return null
    }

    fun getFwdText(context: Context, forwardedMessages: List<VKMessage>): String {
        return if (forwardedMessages.isNotEmpty()) {
            if (forwardedMessages.size > 1) {
                context.getString(R.string.message_fwd_many, forwardedMessages.size).lowerCase()
            } else {
                context.getString(R.string.message_fwd_one)
            }
        } else ""
    }

    @Deprecated("need to rewrite")
    fun getActionText(
        context: Context,
        lastMessage: VKMessage,
        onResponseListener: OnResponseListener<String>
    ) {
        TaskManager.execute {
            lastMessage.action?.let {
                var result = ""

                when (it.type) {
                    VKMessageAction.ACTION_CHAT_CREATE -> result = context.getString(
                        R.string.message_action_created_chat,
                        ""
                    )
                    VKMessageAction.ACTION_INVITE_USER -> result =
                        if (lastMessage.fromId == lastMessage.action!!.memberId) {
                            context.getString(R.string.message_action_returned_to_chat, "")
                        } else {
                            val invited = MemoryCache.getUserById(lastMessage.action!!.memberId)
                            context.getString(R.string.message_action_invited_user, invited)
                        }
                    VKMessageAction.ACTION_INVITE_USER_BY_LINK -> result = context.getString(
                        R.string.message_action_invited_by_link,
                        ""
                    )
                    VKMessageAction.ACTION_KICK_USER -> result =
                        if (lastMessage.fromId == lastMessage.action!!.memberId) {
                            context.getString(R.string.message_action_left_from_chat, "")
                        } else {
                            val kicked = MemoryCache.getUserById(lastMessage.action!!.memberId)
                            context.getString(R.string.message_action_kicked_user, kicked)
                        }
                    VKMessageAction.ACTION_PHOTO_REMOVE -> result = context.getString(
                        R.string.message_action_removed_photo,
                        ""
                    )
                    VKMessageAction.ACTION_PHOTO_UPDATE -> result = context.getString(
                        R.string.message_action_updated_photo,
                        ""
                    )
                    VKMessageAction.ACTION_PIN_MESSAGE -> result = context.getString(
                        R.string.message_action_pinned_message,
                        ""
                    )
                    VKMessageAction.ACTION_UNPIN_MESSAGE -> result = context.getString(
                        R.string.message_action_unpinned_message,
                        ""
                    )
                    VKMessageAction.ACTION_TITLE_UPDATE -> result = context.getString(
                        R.string.message_action_updated_title,
                        ""
                    )
                }

                AppGlobal.post { onResponseListener.onResponse(result) }
            }
        }
    }

    fun getTime(context: Context, lastMessage: VKMessage): String {
        val then = lastMessage.date * 1000L
        val now = System.currentTimeMillis()

        val change = abs(now - then)

        val seconds = change / 1000

        if (seconds == 0L) {
            return context.getString(R.string.time_format_now)
        }

        val minutes = seconds / 60

        if (minutes == 0L) {
            return context.getString(R.string.time_format_second, seconds)
        }

        val hours = minutes / 60

        if (hours == 0L) {
            return context.getString(R.string.time_format_minute, minutes)
        }

        val days = hours / 24

        if (days == 0L) {
            return context.getString(R.string.time_format_hour, hours)
        }

        val months = days / 30

        if (months == 0L) {
            return context.getString(R.string.time_format_day, days)
        }

        val years = months / 12

        if (years == 0L) {
            return context.getString(R.string.time_format_month, months)
        } else if (years > 0L) {
            return context.getString(R.string.time_format_year, years)
        }

        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(then)
    }

    fun parseConversations(array: JSONArray): ArrayList<VKConversation> {
        val conversations = arrayListOf<VKConversation>()
        for (i in 0 until array.length()) {
            conversations.add(VKConversation(array.optJSONObject(i)))
        }

        return conversations
    }

    fun parseMessages(array: JSONArray): ArrayList<VKMessage> {
        val messages = arrayListOf<VKMessage>()
        for (i in 0 until array.length()) {
            messages.add(VKMessage(array.optJSONObject(i)))
        }

        return messages
    }

    fun isChatId(id: Int) = id > 2_000_000_000

    fun isMessageHasFlag(mask: Int, flagName: String): Boolean {
        val o: Any? = VKMessage.flags[flagName]
        return if (o != null) { //has flag
            val flag = o as Int
            flag and mask > 0
        } else false
    }

    //TODO: rewrite parsing
    //fromUser and fromGroup are null
    @Deprecated("need to rewrite")
    @WorkerThread
    fun parseLongPollMessage(array: JSONArray): VKMessage {
        val message = VKMessage()

        val id = array.optInt(1)
        val flags = array.optInt(2)
        val peerId = array.optInt(3)
        val date = array.optInt(4)
        val text = array.optString(5)

        message.messageId = id
        message.peerId = peerId
        message.date = date
        message.text = text

        val fromId =
            if (isMessageHasFlag(flags, "outbox")) UserConfig.userId
            else peerId

        message.fromId = fromId

        array.optJSONObject(6)?.let {
            if (it.has("emoji")) message.hasEmoji = true

            if (it.has("from")) {
                message.fromId = it.optInt("from", -1)
            }

            if (it.has("source_act")) {
                message.action = VKMessageAction().also { action ->
                    action.type = it.optString("source_act")

                    when (action.type) {
                        VKMessageAction.ACTION_CHAT_CREATE -> {
                            action.text = it.optString("source_text")
                        }
                        VKMessageAction.ACTION_TITLE_UPDATE -> {
                            action.oldText = it.optString("source_old_text")
                            action.text = it.optString("source_text")
                        }
                        VKMessageAction.ACTION_PIN_MESSAGE -> {
                            action.memberId = it.optInt("source_mid")
                            action.conversationMessageId = it.optInt("source_chat_local_id")

                            it.optJSONObject("source_message")?.let { message ->
                                action.message = VKMessage(message)
                            }
                        }
                        VKMessageAction.ACTION_UNPIN_MESSAGE -> {
                            action.memberId = it.optInt("source_mid")
                            action.conversationMessageId = it.optInt("source_chat_local_id")
                        }
                        VKMessageAction.ACTION_INVITE_USER,
                        VKMessageAction.ACTION_KICK_USER,
                        VKMessageAction.ACTION_SCREENSHOT,
                        VKMessageAction.ACTION_INVITE_USER_BY_CALL -> {
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

        val out = fromId == UserConfig.userId
        message.isOut = out

        if (message.isFromUser()) {
            message.fromUser = MemoryCache.getUserById(fromId)
        } else {
            message.fromGroup = MemoryCache.getGroupById(abs(fromId))
        }

        return message
    }
}