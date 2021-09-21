package com.meloda.fast.api

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.style.StyleSpan
import androidx.core.content.ContextCompat
import com.meloda.fast.R
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.model.attachments.*
import com.meloda.fast.api.model.base.BaseVkMessage
import com.meloda.fast.api.model.base.attachments.BaseVkAttachmentItem
import com.meloda.fast.api.network.VkErrors

object VkUtils {

    fun isValidationRequired(throwable: Throwable): Boolean {
        if (throwable !is VKException) return false
        return throwable.error == VkErrors.NEED_VALIDATION
    }

    fun isCaptchaRequired(throwable: Throwable): Boolean {
        if (throwable !is VKException) return false
        return throwable.error == VkErrors.NEED_CAPTCHA
    }

    fun prepareMessageText(text: String?): String? {
        if (text == null) return null

        return text
            .replace("\n", " ")
            .replace("&amp", "&")
    }

    fun parseForwards(baseForwards: List<BaseVkMessage>?): List<VkMessage>? {
        if (baseForwards.isNullOrEmpty()) return null

        val forwards = mutableListOf<VkMessage>()

        for (baseForward in baseForwards) {
            forwards += baseForward.asVkMessage()
        }

        return forwards
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
                    attachments += VkAudio(
                        link = audio.url
                    )
                }
                BaseVkAttachmentItem.AttachmentType.FILE -> {
                    val file = baseAttachment.file ?: continue
                    attachments += VkFile(
                        link = file.url
                    )
                }
                BaseVkAttachmentItem.AttachmentType.LINK -> {
                    val link = baseAttachment.link ?: continue
                    attachments += VkLink(
                        link = link.url
                    )
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
                        link = voiceMessage.linkMp3
                    )
                }
                BaseVkAttachmentItem.AttachmentType.STICKER -> {
                    val sticker = baseAttachment.sticker ?: continue
                    attachments += sticker.asVkSticker()
                }
                BaseVkAttachmentItem.AttachmentType.GIFT -> {
                    val gift = baseAttachment.gift ?: continue
                    attachments += VkGift(
                        link = gift.thumb48
                    )
                }
                BaseVkAttachmentItem.AttachmentType.WALL -> {
                    val wall = baseAttachment.wall ?: continue
                    attachments += VkWall(
                        id = wall.id
                    )
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
                        initiatorId = call.initiatorId
                    )
                }
                BaseVkAttachmentItem.AttachmentType.GROUP_CALL_IN_PROGRESS -> {
                    val groupCall = baseAttachment.groupCall ?: continue
                    attachments += VkGroupCall(
                        initiatorId = groupCall.initiatorId
                    )
                }
                else -> continue
            }
        }

        return attachments
    }

    fun getActionConversationText(
        message: VkMessage,
        youPrefix: String,
        profiles: HashMap<Int, VkUser>? = null,
        groups: HashMap<Int, VkGroup>? = null,
        messageUser: VkUser? = null,
        messageGroup: VkGroup? = null
    ): String? {
        return when (message.getPreparedAction()) {
            VkMessage.Action.CHAT_CREATE -> {
                val text = message.actionText ?: return null

                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isGroup() -> messageGroup?.name
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                "$prefix created «$text»"
            }
            VkMessage.Action.CHAT_TITLE_UPDATE -> {
                val text = message.actionText ?: return null

                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isGroup() -> messageGroup?.name
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                "$prefix renamed chat to «$text»"
            }
            VkMessage.Action.CHAT_PHOTO_UPDATE -> {
                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isGroup() -> messageGroup?.name
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                "$prefix updated the chat photo"
            }
            VkMessage.Action.CHAT_PHOTO_REMOVE -> {
                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isGroup() -> messageGroup?.name
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                "$prefix deleted the chat photo"
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
                    "$prefix left the chat"
                } else {
                    val prefix =
                        if (message.fromId == UserConfig.userId) youPrefix
                        else messageUser?.toString() ?: messageGroup?.toString() ?: "..."
                    val postfix =
                        if (memberId == UserConfig.userId) youPrefix.lowercase()
                        else actionUser.toString()
                    "$prefix kicked $postfix"
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
                    "$prefix returned the chat"
                } else {
                    val prefix = if (message.fromId == UserConfig.userId) youPrefix
                    else messageUser?.toString() ?: messageGroup?.toString() ?: "..."
                    val postfix =
                        if (memberId == UserConfig.userId) youPrefix.lowercase()
                        else actionUser.toString()
                    "$prefix invited $postfix"
                }
            }
            VkMessage.Action.CHAT_INVITE_USER_BY_LINK -> {
                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                "$prefix joined the chat via link"
            }
            VkMessage.Action.CHAT_INVITE_USER_BY_CALL_LINK -> {
                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                "$prefix joined the call via link"
            }
            VkMessage.Action.CHAT_PIN_MESSAGE -> {
                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isGroup() -> messageGroup?.name
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                val actionMessage = message.actionMessage

                "$prefix pinned message ${if (actionMessage == null) "" else "«$actionMessage»"}".trim()
            }
            VkMessage.Action.CHAT_UNPIN_MESSAGE -> {
                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isGroup() -> messageGroup?.name
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                "$prefix unpinned message"
            }
            VkMessage.Action.CHAT_SCREENSHOT -> {
                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isGroup() -> messageGroup?.name
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                "$prefix took a screenshot"
            }
            VkMessage.Action.CHAT_STYLE_UPDATE -> {
                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                "$prefix changed chat theme"
            }
            null -> null
            else -> "[${message.action}]"
        }
    }

    fun getActionMessageText(
        message: VkMessage,
        youPrefix: String,
        profiles: HashMap<Int, VkUser>? = null,
        groups: HashMap<Int, VkGroup>? = null,
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

                val spanText = "$prefix created «$text»"

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

                val spanText = "$prefix renamed chat to «$text»"

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

                val spanText = "$prefix updated the chat photo"
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

                val spanText = "$prefix deleted the chat photo"
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
                    val spanText = "$prefix left the chat"
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

                    val spanText = "$prefix kicked $postfix"
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
                    val spanText = "$prefix returned the chat"
                    SpannableString(spanText).also {
                        it.setSpan(StyleSpan(Typeface.BOLD), 0, prefix.length, 0)
                    }
                } else {
                    val prefix = if (message.fromId == UserConfig.userId) youPrefix
                    else messageUser?.toString() ?: messageGroup?.toString() ?: "..."
                    val postfix =
                        if (memberId == UserConfig.userId) youPrefix.lowercase()
                        else actionUser.toString()

                    val spanText = "$prefix invited $postfix"
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

                val spanText = "$prefix joined the chat via link"
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

                val spanText = "$prefix joined the call via link"
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

                val actionMessage = message.actionMessage ?: return null

                val spanText = "$prefix pinned message «$actionMessage»"
                val startIndex = spanText.indexOf(actionMessage)

                SpannableString(spanText).also {
                    it.setSpan(StyleSpan(Typeface.BOLD), 0, prefix.length, 0)
                    it.setSpan(
                        StyleSpan(Typeface.BOLD), startIndex, startIndex + actionMessage.length, 0
                    )
                }
            }
            VkMessage.Action.CHAT_UNPIN_MESSAGE -> {
                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isGroup() -> messageGroup?.name
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                val spanText = "$prefix unpinned message"
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

                val spanText = "$prefix took a screenshot"
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

                val spanText = "$prefix changed chat theme"
                SpannableString(spanText).also {
                    it.setSpan(StyleSpan(Typeface.BOLD), 0, prefix.length, 0)
                }
            }
            null -> null
            else -> SpannableString("[${message.action}]")
        }
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
                getAttachmentTypeByClass(attachments[0])?.let { getAttachmentTextByType(it) }
            } else {
                if (isAttachmentsHaveOneType(attachments)) {
                    getAttachmentTypeByClass(attachments[0])?.let { getAttachmentTextByType(it) }
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
            else -> null
        }
    }

    fun getAttachmentTextByType(attachmentType: BaseVkAttachmentItem.AttachmentType): String? {
        return when (attachmentType) {
            else -> attachmentType.value
        }
    }

}