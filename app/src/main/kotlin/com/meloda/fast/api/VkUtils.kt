package com.meloda.fast.api

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.style.StyleSpan
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.meloda.fast.R
import com.meloda.fast.api.base.ApiError
import com.meloda.fast.api.model.VkConversation
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.model.attachments.*
import com.meloda.fast.api.model.base.BaseVkMessage
import com.meloda.fast.api.model.base.attachments.BaseVkAttachmentItem
import com.meloda.fast.api.network.*
import com.meloda.fast.extensions.orDots

@Suppress("MemberVisibilityCanBePrivate")
object VkUtils {

    fun <T> attachmentToString(
        attachmentClass: Class<T>,
        id: Int,
        ownerId: Int,
        withAccessKey: Boolean,
        accessKey: String?
    ): String {
        val type = when (attachmentClass) {
            VkAudio::class.java -> "audio"
            VkFile::class.java -> "doc"
            VkVideo::class.java -> "video"
            VkPhoto::class.java -> "photo"
            VkWall::class.java -> "wall"
            else -> throw IllegalArgumentException("unknown attachment class: $attachmentClass")
        }

        val result = StringBuilder(type).append(ownerId).append('_').append(id)
        if (withAccessKey && !accessKey.isNullOrBlank()) {
            result.append('_')
            result.append(accessKey)
        }
        return result.toString()
    }


    fun getMessageUser(message: VkMessage, profiles: Map<Int, VkUser>): VkUser? {
        return (if (!message.isUser()) null
        else profiles[message.fromId]).also { message.user = it }
    }

    fun getMessageGroup(message: VkMessage, groups: Map<Int, VkGroup>): VkGroup? {
        return (if (!message.isGroup()) null
        else groups[message.fromId]).also { message.group = it }
    }

    fun getMessageAvatar(
        message: VkMessage,
        messageUser: VkUser?,
        messageGroup: VkGroup?
    ): String? {
        return when {
            message.isUser() -> messageUser?.photo200
            message.isGroup() -> messageGroup?.photo200
            else -> null
        }
    }

    fun getMessageTitle(
        message: VkMessage,
        messageUser: VkUser?,
        messageGroup: VkGroup?
    ): String? {
        return when {
            message.isUser() -> messageUser?.fullName
            message.isGroup() -> messageGroup?.name
            else -> null
        }
    }

    fun getConversationUser(conversation: VkConversation, profiles: Map<Int, VkUser>): VkUser? {
        return (if (!conversation.isUser()) null
        else profiles[conversation.id]).also { conversation.user.value = it }
    }

    fun getConversationGroup(conversation: VkConversation, groups: Map<Int, VkGroup>): VkGroup? {
        return (if (!conversation.isGroup()) null
        else groups[conversation.id]).also { conversation.group.value = it }
    }

    fun getConversationAvatar(
        conversation: VkConversation,
        conversationUser: VkUser?,
        conversationGroup: VkGroup?
    ): String? {
        return when {
            conversation.isAccount() -> null
            conversation.isUser() -> conversationUser?.photo200
            conversation.isGroup() -> conversationGroup?.photo200
            conversation.isChat() -> conversation.photo200
            else -> null
        }
    }

    fun getConversationTitle(
        context: Context,
        conversation: VkConversation,
        conversationUser: VkUser?,
        conversationGroup: VkGroup?
    ): String? {
        return when {
            conversation.isAccount() -> context.getString(R.string.favorites)
            conversation.isChat() -> conversation.title
            conversation.isUser() -> conversationUser?.fullName
            conversation.isGroup() -> conversationGroup?.name
            else -> null
        }
    }

    fun getConversationUserGroup(
        conversation: VkConversation,
        profiles: Map<Int, VkUser>,
        groups: Map<Int, VkGroup>
    ): Pair<VkUser?, VkGroup?> {
        val user: VkUser? = getConversationUser(conversation, profiles)
        val group: VkGroup? = getConversationGroup(conversation, groups)

        return user to group
    }

    fun getMessageUserGroup(
        message: VkMessage,
        profiles: Map<Int, VkUser>,
        groups: Map<Int, VkGroup>
    ): Pair<VkUser?, VkGroup?> {
        val user: VkUser? = getMessageUser(message, profiles)
        val group: VkGroup? = getMessageGroup(message, groups)

        return user to group
    }

    fun prepareMessageText(text: String, forConversations: Boolean? = null): String {
        return text.apply {
            if (forConversations == true) replace("\n", "")

            replace("&amp", "&")
        }
    }

    fun isPreviousMessageSentFiveMinutesAgo(prevMessage: VkMessage?, message: VkMessage?) =
        prevMessage != null && message != null && (message.date - prevMessage.date >= 300)

    fun isPreviousMessageFromDifferentSender(prevMessage: VkMessage?, message: VkMessage?) =
        prevMessage != null && message != null && prevMessage.fromId != message.fromId

    fun parseForwards(baseForwards: List<BaseVkMessage>?): List<VkMessage>? {
        if (baseForwards.isNullOrEmpty()) return null

        val forwards = mutableListOf<VkMessage>()

        for (baseForward in baseForwards) {
            forwards += baseForward.asVkMessage()
        }

        return forwards
    }

    fun parseReplyMessage(baseReplyMessage: BaseVkMessage?): VkMessage? {
        if (baseReplyMessage == null) return null

        return baseReplyMessage.asVkMessage()
    }

    fun parseAttachments(baseAttachments: List<BaseVkAttachmentItem>?): List<VkAttachment>? {
        if (baseAttachments.isNullOrEmpty()) return null

        val attachments = mutableListOf<VkAttachment>()

        for (baseAttachment in baseAttachments) {
            when (baseAttachment.getPreparedType()) {
                BaseVkAttachmentItem.AttachmentType.Photo -> {
                    val photo = baseAttachment.photo ?: continue
                    attachments += photo.asVkPhoto()
                }
                BaseVkAttachmentItem.AttachmentType.Video -> {
                    val video = baseAttachment.video ?: continue
                    attachments += video.asVkVideo()
                }
                BaseVkAttachmentItem.AttachmentType.Audio -> {
                    val audio = baseAttachment.audio ?: continue
                    attachments += audio.asVkAudio()
                }
                BaseVkAttachmentItem.AttachmentType.File -> {
                    val file = baseAttachment.file ?: continue
                    attachments += file.asVkFile()
                }
                BaseVkAttachmentItem.AttachmentType.Link -> {
                    val link = baseAttachment.link ?: continue
                    attachments += link.asVkLink()
                }
                BaseVkAttachmentItem.AttachmentType.MiniApp -> {
                    val miniApp = baseAttachment.miniApp ?: continue
                    attachments += miniApp.asVkMiniApp()
                }
                BaseVkAttachmentItem.AttachmentType.Voice -> {
                    val voiceMessage = baseAttachment.voiceMessage ?: continue
                    attachments += voiceMessage.asVkVoiceMessage()
                }
                BaseVkAttachmentItem.AttachmentType.Sticker -> {
                    val sticker = baseAttachment.sticker ?: continue
                    attachments += sticker.asVkSticker()
                }
                BaseVkAttachmentItem.AttachmentType.Gift -> {
                    val gift = baseAttachment.gift ?: continue
                    attachments += gift.asVkGift()
                }
                BaseVkAttachmentItem.AttachmentType.Wall -> {
                    val wall = baseAttachment.wall ?: continue
                    attachments += wall.asVkWall()
                }
                BaseVkAttachmentItem.AttachmentType.Graffiti -> {
                    val graffiti = baseAttachment.graffiti ?: continue
                    attachments += graffiti.asVkGraffiti()
                }
                BaseVkAttachmentItem.AttachmentType.Poll -> {
                    val poll = baseAttachment.poll ?: continue
                    attachments += poll.asVkPoll()
                }
                BaseVkAttachmentItem.AttachmentType.WallReply -> {
                    val wallReply = baseAttachment.wallReply ?: continue
                    attachments += wallReply.asVkWallReply()
                }
                BaseVkAttachmentItem.AttachmentType.Call -> {
                    val call = baseAttachment.call ?: continue
                    attachments += call.asVkCall()
                }
                BaseVkAttachmentItem.AttachmentType.GroupCallInProgress -> {
                    val groupCall = baseAttachment.groupCall ?: continue
                    attachments += groupCall.asVkGroupCall()
                }
                BaseVkAttachmentItem.AttachmentType.Curator -> {
                    val curator = baseAttachment.curator ?: continue
                    attachments += curator.asVkCurator()
                }
                BaseVkAttachmentItem.AttachmentType.Event -> {
                    val event = baseAttachment.event ?: continue
                    attachments += event.asVkEvent()
                }
                BaseVkAttachmentItem.AttachmentType.Story -> {
                    val story = baseAttachment.story ?: continue
                    attachments += story.asVkStory()
                }
                BaseVkAttachmentItem.AttachmentType.Widget -> {
                    val widget = baseAttachment.widget ?: continue
                    attachments += widget.asVkWidget()
                }
                else -> continue
            }
        }

        return attachments
    }

    fun getActionMessageText(
        context: Context,
        message: VkMessage,
        youPrefix: String,
        profiles: Map<Int, VkUser>? = null,
        groups: Map<Int, VkGroup>? = null,
        messageUser: VkUser? = null,
        messageGroup: VkGroup? = null
    ): SpannableString? {
        return when (message.getPreparedAction()) {
            VkMessage.Action.CHAT_CREATE -> {
                val text = message.actionText ?: return null

                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isGroup() -> messageGroup?.name
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                val spanText =
                    context.getString(R.string.message_action_chat_created, prefix, text)

                val startIndex = spanText.indexOf(text, startIndex = prefix.length)

                SpannableString(spanText).also {
                    it.setSpan(StyleSpan(Typeface.BOLD), 0, prefix.length, 0)
                    it.setSpan(
                        StyleSpan(Typeface.BOLD),
                        startIndex,
                        startIndex + text.length, 0
                    )
                }
            }
            VkMessage.Action.CHAT_TITLE_UPDATE -> {
                val text = message.actionText ?: return null

                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isGroup() -> messageGroup?.name
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                val spanText =
                    context.getString(R.string.message_action_chat_renamed, prefix, text)
                val startIndex = spanText.indexOf(text)

                SpannableString(spanText).also {
                    it.setSpan(StyleSpan(Typeface.BOLD), 0, prefix.length, 0)
                    it.setSpan(
                        StyleSpan(Typeface.BOLD), startIndex, startIndex + text.length, 0
                    )
                }
            }
            VkMessage.Action.CHAT_PHOTO_UPDATE -> {
                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isGroup() -> messageGroup?.name
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                val spanText =
                    context.getString(R.string.message_action_chat_photo_update, prefix)

                SpannableString(spanText).also {
                    it.setSpan(StyleSpan(Typeface.BOLD), 0, prefix.length, 0)
                }
            }
            VkMessage.Action.CHAT_PHOTO_REMOVE -> {
                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isGroup() -> messageGroup?.name
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                val spanText =
                    context.getString(R.string.message_action_chat_photo_remove, prefix)

                SpannableString(spanText).also {
                    it.setSpan(StyleSpan(Typeface.BOLD), 0, prefix.length, 0)
                }
            }
            VkMessage.Action.CHAT_KICK_USER -> {
                val memberId = message.actionMemberId ?: return null
                val isUser = memberId > 0
                val isGroup = memberId < 0

                val actionUser = profiles?.get(memberId)
                val actionGroup = groups?.get(memberId)

                if (isUser && actionUser == null) return null
                if (isGroup && actionGroup == null) return null

                if (memberId == message.fromId) {
                    val prefix = if (memberId == UserConfig.userId) youPrefix
                    else actionUser.toString()

                    val spanText =
                        context.getString(R.string.message_action_chat_user_left, prefix)

                    SpannableString(spanText).also {
                        it.setSpan(StyleSpan(Typeface.BOLD), 0, prefix.length, 0)
                    }
                } else {
                    val prefix =
                        if (message.fromId == UserConfig.userId) youPrefix
                        else messageUser?.toString() ?: messageGroup?.toString().orDots()

                    val postfix =
                        if (memberId == UserConfig.userId) youPrefix.lowercase()
                        else actionUser.toString()

                    val spanText =
                        context.getString(
                            R.string.message_action_chat_user_kicked,
                            prefix,
                            postfix
                        )
                    val startIndex = spanText.indexOf(postfix)

                    SpannableString(spanText).also {
                        it.setSpan(StyleSpan(Typeface.BOLD), 0, prefix.length, 0)
                        it.setSpan(
                            StyleSpan(Typeface.BOLD), startIndex, startIndex + postfix.length, 0
                        )
                    }
                }
            }
            VkMessage.Action.CHAT_INVITE_USER -> {
                val memberId = message.actionMemberId ?: 0
                val isUser = memberId > 0
                val isGroup = memberId < 0

                val actionUser = profiles?.get(memberId)
                val actionGroup = groups?.get(memberId)

                if (isUser && actionUser == null) return null
                if (isGroup && actionGroup == null) return null

                if (memberId == message.fromId) {
                    val prefix = if (memberId == UserConfig.userId) youPrefix
                    else actionUser.toString()

                    val spanText =
                        context.getString(R.string.message_action_chat_user_returned, prefix)

                    SpannableString(spanText).also {
                        it.setSpan(StyleSpan(Typeface.BOLD), 0, prefix.length, 0)
                    }
                } else {
                    val prefix = if (message.fromId == UserConfig.userId) youPrefix
                    else messageUser?.toString() ?: messageGroup?.toString().orDots()

                    val postfix =
                        if (memberId == UserConfig.userId) youPrefix.lowercase()
                        else actionUser.toString()

                    val spanText =
                        context.getString(
                            R.string.message_action_chat_user_invited,
                            prefix,
                            postfix
                        )
                    val startIndex = spanText.indexOf(postfix)

                    SpannableString(spanText).also {
                        it.setSpan(StyleSpan(Typeface.BOLD), 0, prefix.length, 0)
                        it.setSpan(
                            StyleSpan(Typeface.BOLD), startIndex, startIndex + postfix.length, 0
                        )
                    }
                }
            }
            VkMessage.Action.CHAT_INVITE_USER_BY_LINK -> {
                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                val spanText =
                    context.getString(R.string.message_action_chat_user_joined_by_link, prefix)

                SpannableString(spanText).also {
                    it.setSpan(StyleSpan(Typeface.BOLD), 0, prefix.length, 0)
                }
            }
            VkMessage.Action.CHAT_INVITE_USER_BY_CALL_LINK -> {
                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                val spanText =
                    context.getString(R.string.message_action_chat_user_joined_by_call_link, prefix)

                SpannableString(spanText).also {
                    it.setSpan(StyleSpan(Typeface.BOLD), 0, prefix.length, 0)
                }
            }
            VkMessage.Action.CHAT_PIN_MESSAGE -> {
                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isGroup() -> messageGroup?.name
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                val spanText =
                    context.getString(R.string.message_action_chat_pin_message, prefix).trim()

                SpannableString(spanText).also {
                    it.setSpan(StyleSpan(Typeface.BOLD), 0, prefix.length, 0)
                }
            }
            VkMessage.Action.CHAT_UNPIN_MESSAGE -> {
                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isGroup() -> messageGroup?.name
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                val spanText =
                    context.getString(R.string.message_action_chat_unpin_message, prefix)

                SpannableString(spanText).also {
                    it.setSpan(StyleSpan(Typeface.BOLD), 0, prefix.length, 0)
                }
            }
            VkMessage.Action.CHAT_SCREENSHOT -> {
                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isGroup() -> messageGroup?.name
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                val spanText =
                    context.getString(R.string.message_action_chat_screenshot, prefix)

                SpannableString(spanText).also {
                    it.setSpan(StyleSpan(Typeface.BOLD), 0, prefix.length, 0)
                }
            }
            VkMessage.Action.CHAT_STYLE_UPDATE -> {
                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                val spanText =
                    context.getString(R.string.message_action_chat_style_update, prefix)

                SpannableString(spanText).also {
                    it.setSpan(StyleSpan(Typeface.BOLD), 0, prefix.length, 0)
                }
            }
            null -> null
            else -> SpannableString("[${message.action}]")
        }
    }

    fun getActionConversationText(
        context: Context,
        message: VkMessage,
        youPrefix: String,
        profiles: HashMap<Int, VkUser>? = null,
        groups: HashMap<Int, VkGroup>? = null,
        messageUser: VkUser? = null,
        messageGroup: VkGroup? = null
    ): String? {
        return getActionMessageText(
            context = context,
            message = message,
            youPrefix = youPrefix,
            profiles = profiles,
            groups = groups,
            messageUser = messageUser,
            messageGroup = messageGroup
        )?.toString()
    }

    fun getForwardsText(context: Context, message: VkMessage): String? {
        if (message.forwards.isNullOrEmpty()) return null

        return message.forwards?.let { forwards ->
            context.getString(
                if (forwards.size == 1) R.string.forwarded_message
                else R.string.forwarded_messages
            )
        }
    }

    fun getAttachmentText(context: Context, message: VkMessage): String? {
        message.geo?.let {
            return when (it.type) {
                "point" -> context.getString(R.string.message_geo_point)
                else -> context.getString(R.string.message_geo)
            }
        }
        if (message.attachments.isNullOrEmpty()) return null

        return message.attachments?.let { attachments ->
            if (attachments.size == 1) {
                getAttachmentTypeByClass(attachments[0])?.let {
                    getAttachmentTextByType(
                        context,
                        it
                    )
                }
            } else {
                if (isAttachmentsHaveOneType(attachments)) {
                    getAttachmentTypeByClass(attachments[0])?.let {
                        getAttachmentTextByType(
                            context, it, attachments.size
                        )
                    }
                } else {
                    context.getString(R.string.message_attachments_many)
                }
            }
        }
    }

    fun getAttachmentConversationIcon(context: Context, message: VkMessage): Drawable? {
        return message.attachments?.let { attachments ->
            if (attachments.size == 1 || isAttachmentsHaveOneType(attachments)) {
                message.geo?.let {
                    return ContextCompat.getDrawable(context, R.drawable.ic_map_marker)
                }

                if (attachments.isEmpty()) return null

                getAttachmentTypeByClass(attachments[0])?.let {
                    getAttachmentIconByType(
                        context,
                        it
                    )
                }
            } else {
                ContextCompat.getDrawable(context, R.drawable.ic_baseline_attach_file_24)
            }
        }
    }

    fun getAttachmentIconByType(
        context: Context,
        attachmentType: BaseVkAttachmentItem.AttachmentType
    ): Drawable? {
        val resId = when (attachmentType) {
            BaseVkAttachmentItem.AttachmentType.Photo -> R.drawable.ic_attachment_photo
            BaseVkAttachmentItem.AttachmentType.Video -> R.drawable.ic_attachment_video
            BaseVkAttachmentItem.AttachmentType.Audio -> R.drawable.ic_attachment_audio
            BaseVkAttachmentItem.AttachmentType.File -> R.drawable.ic_attachment_file
            BaseVkAttachmentItem.AttachmentType.Link -> R.drawable.ic_attachment_link
            BaseVkAttachmentItem.AttachmentType.Voice -> R.drawable.ic_attachment_voice
            BaseVkAttachmentItem.AttachmentType.MiniApp -> R.drawable.ic_attachment_mini_app
            BaseVkAttachmentItem.AttachmentType.Sticker -> R.drawable.ic_attachment_sticker
            BaseVkAttachmentItem.AttachmentType.Gift -> R.drawable.ic_attachment_gift
            BaseVkAttachmentItem.AttachmentType.Wall -> R.drawable.ic_attachment_wall
            BaseVkAttachmentItem.AttachmentType.Graffiti -> R.drawable.ic_attachment_graffiti
            BaseVkAttachmentItem.AttachmentType.Poll -> R.drawable.ic_attachment_poll
            BaseVkAttachmentItem.AttachmentType.WallReply -> R.drawable.ic_attachment_wall_reply
            BaseVkAttachmentItem.AttachmentType.Call -> R.drawable.ic_attachment_call
            BaseVkAttachmentItem.AttachmentType.GroupCallInProgress -> R.drawable.ic_attachment_group_call
            BaseVkAttachmentItem.AttachmentType.Story -> R.drawable.ic_attachment_story
            else -> return null
        }

        return ContextCompat.getDrawable(context, resId)
    }

    fun isAttachmentsHaveOneType(attachments: List<VkAttachment>): Boolean {
        if (attachments.isEmpty()) return true
        if (attachments.size == 1) return true

        val firstType = getAttachmentTypeByClass(attachments[0])
        for (i in 1 until attachments.size) {
            val type = getAttachmentTypeByClass(attachments[i])
            if (type != firstType) return false
        }

        return true
    }

    fun getAttachmentTypeByClass(attachment: VkAttachment): BaseVkAttachmentItem.AttachmentType? {
        return when (attachment) {
            is VkPhoto -> BaseVkAttachmentItem.AttachmentType.Photo
            is VkVideo -> BaseVkAttachmentItem.AttachmentType.Video
            is VkAudio -> BaseVkAttachmentItem.AttachmentType.Audio
            is VkFile -> BaseVkAttachmentItem.AttachmentType.File
            is VkLink -> BaseVkAttachmentItem.AttachmentType.Link
            is VkMiniApp -> BaseVkAttachmentItem.AttachmentType.MiniApp
            is VkVoiceMessage -> BaseVkAttachmentItem.AttachmentType.Voice
            is VkSticker -> BaseVkAttachmentItem.AttachmentType.Sticker
            is VkGift -> BaseVkAttachmentItem.AttachmentType.Gift
            is VkWall -> BaseVkAttachmentItem.AttachmentType.Wall
            is VkGraffiti -> BaseVkAttachmentItem.AttachmentType.Graffiti
            is VkPoll -> BaseVkAttachmentItem.AttachmentType.Poll
            is VkWallReply -> BaseVkAttachmentItem.AttachmentType.WallReply
            is VkCall -> BaseVkAttachmentItem.AttachmentType.Call
            is VkGroupCall -> BaseVkAttachmentItem.AttachmentType.GroupCallInProgress
            is VkEvent -> BaseVkAttachmentItem.AttachmentType.Event
            is VkCurator -> BaseVkAttachmentItem.AttachmentType.Curator
            is VkStory -> BaseVkAttachmentItem.AttachmentType.Story
            is VkWidget -> BaseVkAttachmentItem.AttachmentType.Widget
            else -> null
        }
    }

    fun getAttachmentTextByType(
        context: Context,
        attachmentType: BaseVkAttachmentItem.AttachmentType,
        size: Int = 1
    ): String {
        return when (attachmentType) {
            BaseVkAttachmentItem.AttachmentType.Photo ->
                context.resources.getQuantityString(R.plurals.attachment_photos, size, size)
            BaseVkAttachmentItem.AttachmentType.Video ->
                context.resources.getQuantityString(R.plurals.attachment_videos, size, size)
            BaseVkAttachmentItem.AttachmentType.Audio ->
                context.resources.getQuantityString(R.plurals.attachment_audios, size, size)
            BaseVkAttachmentItem.AttachmentType.File ->
                context.resources.getQuantityString(R.plurals.attachment_files, size, size)
            BaseVkAttachmentItem.AttachmentType.Link ->
                context.resources.getString(R.string.message_attachments_link)
            BaseVkAttachmentItem.AttachmentType.Voice ->
                context.resources.getString(R.string.message_attachments_voice)
            BaseVkAttachmentItem.AttachmentType.MiniApp ->
                context.resources.getString(R.string.message_attachments_mini_app)
            BaseVkAttachmentItem.AttachmentType.Sticker ->
                context.resources.getString(R.string.message_attachments_sticker)
            BaseVkAttachmentItem.AttachmentType.Gift ->
                context.resources.getString(R.string.message_attachments_gift)
            BaseVkAttachmentItem.AttachmentType.Wall ->
                context.resources.getString(R.string.message_attachments_wall)
            BaseVkAttachmentItem.AttachmentType.Graffiti ->
                context.resources.getString(R.string.message_attachments_graffiti)
            BaseVkAttachmentItem.AttachmentType.Poll ->
                context.resources.getString(R.string.message_attachments_poll)
            BaseVkAttachmentItem.AttachmentType.WallReply ->
                context.resources.getString(R.string.message_attachments_wall_reply)
            BaseVkAttachmentItem.AttachmentType.Call ->
                context.resources.getString(R.string.message_attachments_call)
            BaseVkAttachmentItem.AttachmentType.GroupCallInProgress ->
                context.resources.getString(R.string.message_attachments_call_in_progress)
            BaseVkAttachmentItem.AttachmentType.Event ->
                context.resources.getString(R.string.message_attachments_event)
            BaseVkAttachmentItem.AttachmentType.Curator ->
                context.resources.getString(R.string.message_attachments_curator)
            BaseVkAttachmentItem.AttachmentType.Story ->
                context.resources.getString(R.string.message_attachments_story)
            BaseVkAttachmentItem.AttachmentType.Widget ->
                context.resources.getString(R.string.message_attachments_widget)
            else -> attachmentType.value
        }
    }

    fun getApiError(gson: Gson, errorString: String?): ApiAnswer.Error {
        try {
            val defaultError = gson.fromJson(errorString, ApiError::class.java)

            val error: ApiError =
                when (defaultError.error) {
                    VkErrorCodes.UserAuthorizationFailed.toString() -> {
                        val authorizationError =
                            gson.fromJson(errorString, AuthorizationError::class.java)

                        authorizationError
                    }
                    VkErrors.NeedValidation -> {
                        val validationError =
                            gson.fromJson(errorString, ValidationRequiredError::class.java)

                        validationError
                    }
                    VkErrors.NeedCaptcha -> {
                        val captchaRequiredError =
                            gson.fromJson(errorString, CaptchaRequiredError::class.java)

                        captchaRequiredError
                    }
                    else -> defaultError
                }

            return ApiAnswer.Error(error)
        } catch (e: Exception) {
            return ApiAnswer.Error(ApiError(throwable = e))
        }
    }
}