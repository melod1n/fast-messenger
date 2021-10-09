package com.meloda.fast.api

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.style.StyleSpan
import androidx.core.content.ContextCompat
import com.meloda.fast.R
import com.meloda.fast.api.model.VkConversation
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.model.attachments.*
import com.meloda.fast.api.model.base.BaseVkMessage
import com.meloda.fast.api.model.base.attachments.BaseVkAttachmentItem

object VkUtils {

    fun getMessageUser(message: VkMessage, profiles: Map<Int, VkUser>): VkUser? {
        return (if (!message.isUser()) null
        else profiles[message.fromId]).also { message.user.value = it }
    }

    fun getMessageGroup(message: VkMessage, groups: Map<Int, VkGroup>): VkGroup? {
        return (if (!message.isGroup()) null
        else groups[message.fromId]).also { message.group.value = it }
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
            conversation.ownerId == VKConstants.FAST_GROUP_ID -> null
            conversation.isUser() -> conversationUser?.photo200
            conversation.isGroup() -> conversationGroup?.photo200
            conversation.isChat() -> conversation.photo200
            else -> null
        }
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
                BaseVkAttachmentItem.AttachmentType.PHOTO -> {
                    val photo = baseAttachment.photo ?: continue
                    attachments += photo.asVkPhoto()
                }
                BaseVkAttachmentItem.AttachmentType.VIDEO -> {
                    val video = baseAttachment.video ?: continue
                    attachments += video.asVkVideo()
                }
                BaseVkAttachmentItem.AttachmentType.AUDIO -> {
                    val audio = baseAttachment.audio ?: continue
                    attachments += audio.asVkAudio()
                }
                BaseVkAttachmentItem.AttachmentType.FILE -> {
                    val file = baseAttachment.file ?: continue
                    attachments += file.asVkFile()
                }
                BaseVkAttachmentItem.AttachmentType.LINK -> {
                    val link = baseAttachment.link ?: continue
                    attachments += link.asVkLink()
                }
                BaseVkAttachmentItem.AttachmentType.MINI_APP -> {
                    val miniApp = baseAttachment.miniApp ?: continue
                    attachments += VkMiniApp(
                        link = miniApp.app.shareUrl
                    )
                }
                BaseVkAttachmentItem.AttachmentType.VOICE -> {
                    val voiceMessage = baseAttachment.voiceMessage ?: continue
                    attachments += VkVoiceMessage(
                        link = voiceMessage.link_mp3
                    )
                }
                BaseVkAttachmentItem.AttachmentType.STICKER -> {
                    val sticker = baseAttachment.sticker ?: continue
                    attachments += sticker.asVkSticker()
                }
                BaseVkAttachmentItem.AttachmentType.GIFT -> {
                    val gift = baseAttachment.gift ?: continue
                    attachments += VkGift(
                        link = gift.thumb_48
                    )
                }
                BaseVkAttachmentItem.AttachmentType.WALL -> {
                    val wall = baseAttachment.wall ?: continue
                    attachments += wall.asVkWall()
                }
                BaseVkAttachmentItem.AttachmentType.GRAFFITI -> {
                    val graffiti = baseAttachment.graffiti ?: continue
                    attachments += VkGraffiti(
                        link = graffiti.url
                    )
                }
                BaseVkAttachmentItem.AttachmentType.POLL -> {
                    val poll = baseAttachment.poll ?: continue
                    attachments += VkPoll(
                        id = poll.id
                    )
                }
                BaseVkAttachmentItem.AttachmentType.WALL_REPLY -> {
                    val wallReply = baseAttachment.wallReply ?: continue
                    attachments += VkWallReply(
                        id = wallReply.id
                    )
                }
                BaseVkAttachmentItem.AttachmentType.CALL -> {
                    val call = baseAttachment.call ?: continue
                    attachments += VkCall(
                        initiatorId = call.initiator_id
                    )
                }
                BaseVkAttachmentItem.AttachmentType.GROUP_CALL_IN_PROGRESS -> {
                    val groupCall = baseAttachment.groupCall ?: continue
                    attachments += VkGroupCall(
                        initiatorId = groupCall.initiator_id
                    )
                }
                BaseVkAttachmentItem.AttachmentType.CURATOR -> {
                    val curator = baseAttachment.curator ?: continue
                    attachments += curator.asVkCurator()
                }
                BaseVkAttachmentItem.AttachmentType.EVENT -> {
                    val event = baseAttachment.event ?: continue
                    attachments += event.asVkEvent()
                }
                BaseVkAttachmentItem.AttachmentType.STORY -> {
                    val story = baseAttachment.story ?: continue
                    attachments += story.asVkStory()
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

                SpannableString(spanText).also {
                    it.setSpan(StyleSpan(Typeface.BOLD), 0, prefix.length, 0)
                    it.setSpan(
                        StyleSpan(Typeface.BOLD),
                        spanText.indexOf(text, startIndex = prefix.length),
                        text.length, 0
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
                        else messageUser?.toString() ?: messageGroup?.toString() ?: "..."

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
                    else messageUser?.toString() ?: messageGroup?.toString() ?: "..."

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

    fun getForwardsConversationText(context: Context, message: VkMessage): String? {
        if (message.forwards.isNullOrEmpty()) return null

        return message.forwards?.let { forwards ->
            context.getString(
                if (forwards.size == 1) R.string.forwarded_message
                else R.string.forwarded_messages
            )
        }
    }

    fun getAttachmentConversationText(context: Context, message: VkMessage): String? {
        message.geoType?.let {
            return when (it) {
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
        message.geoType?.let {
            return ContextCompat.getDrawable(context, R.drawable.ic_map_marker)
        }

        if (message.attachments.isNullOrEmpty()) return null

        return message.attachments?.let { attachments ->
            if (attachments.size == 1 || isAttachmentsHaveOneType(attachments)) {
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
            BaseVkAttachmentItem.AttachmentType.PHOTO -> R.drawable.ic_attachment_photo
            BaseVkAttachmentItem.AttachmentType.VIDEO -> R.drawable.ic_attachment_video
            BaseVkAttachmentItem.AttachmentType.AUDIO -> R.drawable.ic_attachment_audio
            BaseVkAttachmentItem.AttachmentType.FILE -> R.drawable.ic_attachment_file
            BaseVkAttachmentItem.AttachmentType.LINK -> R.drawable.ic_attachment_link
            BaseVkAttachmentItem.AttachmentType.VOICE -> R.drawable.ic_attachment_voice
            BaseVkAttachmentItem.AttachmentType.MINI_APP -> R.drawable.ic_attachment_mini_app
            BaseVkAttachmentItem.AttachmentType.STICKER -> R.drawable.ic_attachment_sticker
            BaseVkAttachmentItem.AttachmentType.GIFT -> R.drawable.ic_attachment_gift
            BaseVkAttachmentItem.AttachmentType.WALL -> R.drawable.ic_attachment_wall
            BaseVkAttachmentItem.AttachmentType.GRAFFITI -> R.drawable.ic_attachment_graffiti
            BaseVkAttachmentItem.AttachmentType.POLL -> R.drawable.ic_attachment_poll
            BaseVkAttachmentItem.AttachmentType.WALL_REPLY -> R.drawable.ic_attachment_wall_reply
            BaseVkAttachmentItem.AttachmentType.CALL -> R.drawable.ic_attachment_call
            BaseVkAttachmentItem.AttachmentType.GROUP_CALL_IN_PROGRESS -> R.drawable.ic_attachment_group_call
            BaseVkAttachmentItem.AttachmentType.STORY -> R.drawable.ic_attachment_story
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
            is VkPhoto -> BaseVkAttachmentItem.AttachmentType.PHOTO
            is VkVideo -> BaseVkAttachmentItem.AttachmentType.VIDEO
            is VkAudio -> BaseVkAttachmentItem.AttachmentType.AUDIO
            is VkFile -> BaseVkAttachmentItem.AttachmentType.FILE
            is VkLink -> BaseVkAttachmentItem.AttachmentType.LINK
            is VkMiniApp -> BaseVkAttachmentItem.AttachmentType.MINI_APP
            is VkVoiceMessage -> BaseVkAttachmentItem.AttachmentType.VOICE
            is VkSticker -> BaseVkAttachmentItem.AttachmentType.STICKER
            is VkGift -> BaseVkAttachmentItem.AttachmentType.GIFT
            is VkWall -> BaseVkAttachmentItem.AttachmentType.WALL
            is VkGraffiti -> BaseVkAttachmentItem.AttachmentType.GRAFFITI
            is VkPoll -> BaseVkAttachmentItem.AttachmentType.POLL
            is VkWallReply -> BaseVkAttachmentItem.AttachmentType.WALL_REPLY
            is VkCall -> BaseVkAttachmentItem.AttachmentType.CALL
            is VkGroupCall -> BaseVkAttachmentItem.AttachmentType.GROUP_CALL_IN_PROGRESS
            is VkEvent -> BaseVkAttachmentItem.AttachmentType.EVENT
            is VkCurator -> BaseVkAttachmentItem.AttachmentType.CURATOR
            is VkStory -> BaseVkAttachmentItem.AttachmentType.STORY
            else -> null
        }
    }

    fun getAttachmentTextByType(
        context: Context,
        attachmentType: BaseVkAttachmentItem.AttachmentType,
        size: Int = 1
    ): String {
        return when (attachmentType) {
            BaseVkAttachmentItem.AttachmentType.PHOTO ->
                context.resources.getQuantityString(R.plurals.attachment_photos, size, size)
            BaseVkAttachmentItem.AttachmentType.VIDEO ->
                context.resources.getQuantityString(R.plurals.attachment_videos, size, size)
            BaseVkAttachmentItem.AttachmentType.AUDIO ->
                context.resources.getQuantityString(R.plurals.attachment_audios, size, size)
            BaseVkAttachmentItem.AttachmentType.FILE ->
                context.resources.getQuantityString(R.plurals.attachment_files, size, size)
            BaseVkAttachmentItem.AttachmentType.LINK ->
                context.resources.getString(R.string.message_attachments_link)
            BaseVkAttachmentItem.AttachmentType.VOICE ->
                context.resources.getString(R.string.message_attachments_voice)
            BaseVkAttachmentItem.AttachmentType.MINI_APP ->
                context.resources.getString(R.string.message_attachments_mini_app)
            BaseVkAttachmentItem.AttachmentType.STICKER ->
                context.resources.getString(R.string.message_attachments_sticker)
            BaseVkAttachmentItem.AttachmentType.GIFT ->
                context.resources.getString(R.string.message_attachments_gift)
            BaseVkAttachmentItem.AttachmentType.WALL ->
                context.resources.getString(R.string.message_attachments_wall)
            BaseVkAttachmentItem.AttachmentType.GRAFFITI ->
                context.resources.getString(R.string.message_attachments_graffiti)
            BaseVkAttachmentItem.AttachmentType.POLL ->
                context.resources.getString(R.string.message_attachments_poll)
            BaseVkAttachmentItem.AttachmentType.WALL_REPLY ->
                context.resources.getString(R.string.message_attachments_wall_reply)
            BaseVkAttachmentItem.AttachmentType.CALL ->
                context.resources.getString(R.string.message_attachments_call)
            BaseVkAttachmentItem.AttachmentType.GROUP_CALL_IN_PROGRESS ->
                context.resources.getString(R.string.message_attachments_call_in_progress)
            BaseVkAttachmentItem.AttachmentType.EVENT ->
                context.resources.getString(R.string.message_attachments_event)
            BaseVkAttachmentItem.AttachmentType.CURATOR ->
                context.resources.getString(R.string.message_attachments_curator)
            BaseVkAttachmentItem.AttachmentType.STORY ->
                context.resources.getString(R.string.message_attachments_story)
            else -> attachmentType.value
        }
    }
}