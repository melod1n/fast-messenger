package com.meloda.fast.api

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.meloda.fast.R
import com.meloda.fast.api.base.ApiError
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.model.attachments.VkAttachment
import com.meloda.fast.api.model.attachments.VkAudio
import com.meloda.fast.api.model.attachments.VkCall
import com.meloda.fast.api.model.attachments.VkCurator
import com.meloda.fast.api.model.attachments.VkEvent
import com.meloda.fast.api.model.attachments.VkFile
import com.meloda.fast.api.model.attachments.VkGift
import com.meloda.fast.api.model.attachments.VkGraffiti
import com.meloda.fast.api.model.attachments.VkGroupCall
import com.meloda.fast.api.model.attachments.VkLink
import com.meloda.fast.api.model.attachments.VkMiniApp
import com.meloda.fast.api.model.attachments.VkPhoto
import com.meloda.fast.api.model.attachments.VkPoll
import com.meloda.fast.api.model.attachments.VkSticker
import com.meloda.fast.api.model.attachments.VkStory
import com.meloda.fast.api.model.attachments.VkVideo
import com.meloda.fast.api.model.attachments.VkVoiceMessage
import com.meloda.fast.api.model.attachments.VkWall
import com.meloda.fast.api.model.attachments.VkWallReply
import com.meloda.fast.api.model.attachments.VkWidget
import com.meloda.fast.api.model.base.BaseVkMessage
import com.meloda.fast.api.model.base.attachments.BaseVkAttachmentItem
import com.meloda.fast.api.model.domain.VkConversationDomain
import com.meloda.fast.api.network.ApiAnswer
import com.meloda.fast.api.network.AuthorizationError
import com.meloda.fast.api.network.CaptchaRequiredError
import com.meloda.fast.api.network.TokenExpiredError
import com.meloda.fast.api.network.UserBannedError
import com.meloda.fast.api.network.ValidationRequiredError
import com.meloda.fast.api.network.VkErrorCodes
import com.meloda.fast.api.network.VkErrorMessages
import com.meloda.fast.api.network.VkErrorTypes
import com.meloda.fast.api.network.VkErrors
import com.meloda.fast.api.network.WrongTwoFaCodeError
import com.meloda.fast.api.network.WrongTwoFaCodeFormatError
import com.meloda.fast.ext.orDots
import com.meloda.fast.model.base.UiImage
import com.meloda.fast.model.base.UiText

@Suppress("MemberVisibilityCanBePrivate")
object VkUtils {

    fun <T> attachmentToString(
        attachmentClass: Class<T>,
        id: Int,
        ownerId: Int,
        withAccessKey: Boolean,
        accessKey: String?,
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


    fun getMessageUser(
        message: VkMessage,
        profiles: Map<Int, VkUser>
    ): VkUser? {
        return (if (!message.isUser()) null
        else profiles[message.fromId])
    }

    fun getMessageActionUser(message: VkMessage, profiles: Map<Int, VkUser>): VkUser? {
        return if (message.actionMemberId == null || message.actionMemberId <= 0) null
        else profiles[message.actionMemberId]
    }

    fun getMessageGroup(message: VkMessage, groups: Map<Int, VkGroup>): VkGroup? {
        return (if (!message.isGroup()) null
        else groups[message.fromId])
    }

    fun getMessageActionGroup(message: VkMessage, groups: Map<Int, VkGroup>): VkGroup? {
        return if (message.actionMemberId == null || message.actionMemberId >= 0) null
        else groups[message.actionMemberId]
    }

    fun getMessageAvatar(
        message: VkMessage,
        messageUser: VkUser?,
        messageGroup: VkGroup?,
    ): String? {
        return when {
            message.isUser() -> messageUser?.photo200
            message.isGroup() -> messageGroup?.photo200
            else -> null
        }
    }

    fun getMessageTitle(
        message: VkMessage,
        defMessageUser: VkUser? = null,
        defMessageGroup: VkGroup? = null,
        profiles: Map<Int, VkUser>? = null,
        groups: Map<Int, VkGroup>? = null,
    ): String? {
        val messageUser: VkUser? =
            defMessageUser ?: if (profiles == null) null
            else profiles[message.fromId]

        val messageGroup: VkGroup? =
            defMessageGroup ?: if (groups == null) null
            else groups[message.fromId]

        return when {
            message.isUser() -> messageUser?.fullName
            message.isGroup() -> messageGroup?.name
            else -> null
        }
    }

    fun getConversationUser(
        conversation: VkConversationDomain,
        profiles: Map<Int, VkUser>
    ): VkUser? {
        return if (!conversation.isUser()) null
        else profiles[conversation.id]
    }

    fun getConversationGroup(
        conversation: VkConversationDomain,
        groups: Map<Int, VkGroup>
    ): VkGroup? {
        return if (!conversation.isGroup()) null
        else groups[conversation.id]
    }

    fun getConversationAvatar(
        conversation: VkConversationDomain,
        conversationUser: VkUser?,
        conversationGroup: VkGroup?,
    ): String? {
        return when {
            conversation.isAccount() -> null
            conversation.isUser() -> conversationUser?.photo200
            conversation.isGroup() -> conversationGroup?.photo200
            conversation.isChat() -> conversation.conversationPhoto
            else -> null
        }
    }

    fun getConversationTitle(
        context: Context,
        conversation: VkConversationDomain,
        defConversationUser: VkUser? = null,
        defConversationGroup: VkGroup? = null,
        profiles: Map<Int, VkUser>? = null,
        groups: Map<Int, VkGroup>? = null,
    ): String? {
        val conversationUser: VkUser? =
            defConversationUser ?: if (profiles == null) null
            else getConversationUser(conversation, profiles)

        val conversationGroup: VkGroup? =
            defConversationGroup ?: if (groups == null) null
            else getConversationGroup(conversation, groups)

        return when {
            conversation.isAccount() -> context.getString(R.string.favorites)
            conversation.isChat() -> conversation.conversationTitle
            conversation.isUser() -> conversationUser?.fullName
            conversation.isGroup() -> conversationGroup?.name
            else -> null
        }
    }

    fun getConversationUserGroup(
        conversation: VkConversationDomain,
        profiles: Map<Int, VkUser>,
        groups: Map<Int, VkGroup>,
    ): Pair<VkUser?, VkGroup?> {
        val user: VkUser? = getConversationUser(conversation, profiles)
        val group: VkGroup? = getConversationGroup(conversation, groups)

        return user to group
    }

    fun getMessageUserGroup(
        message: VkMessage?,
        profiles: Map<Int, VkUser>,
        groups: Map<Int, VkGroup>,
    ): Pair<VkUser?, VkGroup?> {
        if (message == null) return null to null

        val user: VkUser? = getMessageUser(message, profiles)
        val group: VkGroup? = getMessageGroup(message, groups)

        return user to group
    }

    fun getMessageActionUserGroup(
        message: VkMessage?,
        profiles: Map<Int, VkUser>,
        groups: Map<Int, VkGroup>,
    ): Pair<VkUser?, VkGroup?> {
        if (message == null) return null to null

        val user: VkUser? = getMessageActionUser(message, profiles)
        val group: VkGroup? = getMessageActionGroup(message, groups)

        return user to group
    }

    fun prepareMessageText(text: String, forConversations: Boolean = false): String {
        return text.apply {
            if (forConversations) {
                replace("\n", "")
            }

            replace("&amp;", "&")
            replace("&quot;", "\"")
            replace("<br>", "\n")
            replace("&gt;", ">")
            replace("&lt;", "<")
            replace("<br/>", "\n")
            replace("&ndash;", "-")
            trim()
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
        message: VkMessage?,
        youPrefix: String,
        messageUser: VkUser?,
        messageGroup: VkGroup?,
        action: VkMessage.Action?,
        actionUser: VkUser?,
        actionGroup: VkGroup?,
    ): UiText? {
        if (message == null) return null

        return when (action) {
            VkMessage.Action.CHAT_CREATE -> {
                val text = message.actionText ?: return null

                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isGroup() -> messageGroup?.name
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                UiText.ResourceParams(
                    R.string.message_action_chat_created,
                    listOf(prefix, text)
                )
            }

            VkMessage.Action.CHAT_TITLE_UPDATE -> {
                val text = message.actionText ?: return null

                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isGroup() -> messageGroup?.name
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                UiText.ResourceParams(
                    R.string.message_action_chat_renamed,
                    listOf(prefix, text)
                )
            }

            VkMessage.Action.CHAT_PHOTO_UPDATE -> {
                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isGroup() -> messageGroup?.name
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                UiText.ResourceParams(R.string.message_action_chat_photo_update, listOf(prefix))
            }

            VkMessage.Action.CHAT_PHOTO_REMOVE -> {
                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isGroup() -> messageGroup?.name
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                UiText.ResourceParams(R.string.message_action_chat_photo_remove, listOf(prefix))
            }

            VkMessage.Action.CHAT_KICK_USER -> {
                val memberId = message.actionMemberId ?: return null
                val isUser = memberId > 0
                val isGroup = memberId < 0

                if (isUser && actionUser == null) return null
                if (isGroup && actionGroup == null) return null

                if (memberId == message.fromId) {
                    val prefix = if (memberId == UserConfig.userId) youPrefix
                    else actionUser.toString()

                    UiText.ResourceParams(R.string.message_action_chat_user_left, listOf(prefix))
                } else {
                    val prefix =
                        if (message.fromId == UserConfig.userId) youPrefix
                        else messageUser?.toString() ?: messageGroup?.toString().orDots()

                    val postfix =
                        if (memberId == UserConfig.userId) youPrefix.lowercase()
                        else actionUser.toString()

                    UiText.ResourceParams(
                        R.string.message_action_chat_user_kicked, listOf(prefix, postfix)
                    )
                }
            }

            VkMessage.Action.CHAT_INVITE_USER -> {
                val memberId = message.actionMemberId ?: 0
                val isUser = memberId > 0
                val isGroup = memberId < 0

                if (isUser && actionUser == null) return null
                if (isGroup && actionGroup == null) return null

                if (memberId == message.fromId) {
                    val prefix = if (memberId == UserConfig.userId) youPrefix
                    else actionUser.toString()

                    UiText.ResourceParams(
                        R.string.message_action_chat_user_returned,
                        listOf(prefix)
                    )
                } else {
                    val prefix = if (message.fromId == UserConfig.userId) youPrefix
                    else messageUser?.toString() ?: messageGroup?.toString().orDots()

                    val postfix =
                        if (memberId == UserConfig.userId) youPrefix.lowercase()
                        else actionUser.toString()

                    UiText.ResourceParams(
                        R.string.message_action_chat_user_invited,
                        listOf(prefix, postfix)
                    )
                }
            }

            VkMessage.Action.CHAT_INVITE_USER_BY_LINK -> {
                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                UiText.ResourceParams(
                    R.string.message_action_chat_user_joined_by_link,
                    listOf(prefix)
                )
            }

            VkMessage.Action.CHAT_INVITE_USER_BY_CALL -> {
                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                UiText.ResourceParams(
                    R.string.message_action_chat_user_joined_by_call,
                    listOf(prefix)
                )
            }

            VkMessage.Action.CHAT_INVITE_USER_BY_CALL_LINK -> {
                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                UiText.ResourceParams(
                    R.string.message_action_chat_user_joined_by_call_link,
                    listOf(prefix)
                )
            }

            VkMessage.Action.CHAT_PIN_MESSAGE -> {
                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isGroup() -> messageGroup?.name
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                UiText.ResourceParams(R.string.message_action_chat_pin_message, listOf(prefix))
            }

            VkMessage.Action.CHAT_UNPIN_MESSAGE -> {
                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isGroup() -> messageGroup?.name
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                UiText.ResourceParams(R.string.message_action_chat_unpin_message, listOf(prefix))
            }

            VkMessage.Action.CHAT_SCREENSHOT -> {
                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isGroup() -> messageGroup?.name
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                UiText.ResourceParams(R.string.message_action_chat_screenshot, listOf(prefix))
            }

            VkMessage.Action.CHAT_STYLE_UPDATE -> {
                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                UiText.ResourceParams(R.string.message_action_chat_style_update, listOf(prefix))
            }

            null -> null
        }
    }

    fun getActionMessageText(
        context: Context,
        message: VkMessage?,
        youPrefix: String,
        messageUser: VkUser?,
        messageGroup: VkGroup?,
        action: VkMessage.Action?,
        actionUser: VkUser?,
        actionGroup: VkGroup?,
    ): SpannableString? {
        if (message == null) return null

        return when (action) {
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

            VkMessage.Action.CHAT_INVITE_USER_BY_CALL -> {
                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                val spanText =
                    context.getString(R.string.message_action_chat_user_joined_by_call, prefix)

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
        }
    }

    fun getActionConversationText(
        message: VkMessage?,
        youPrefix: String,
        messageUser: VkUser? = null,
        messageGroup: VkGroup? = null,
        action: VkMessage.Action?,
        actionUser: VkUser?,
        actionGroup: VkGroup?,
    ): UiText? {
        return getActionMessageText(
            message = message,
            youPrefix = youPrefix,
            messageUser = messageUser,
            messageGroup = messageGroup,
            action = action,
            actionUser = actionUser,
            actionGroup = actionGroup,
        )
    }

    fun getActionConversationText(
        context: Context,
        message: VkMessage?,
        youPrefix: String,
        messageUser: VkUser? = null,
        messageGroup: VkGroup? = null,
        action: VkMessage.Action?,
        actionUser: VkUser?,
        actionGroup: VkGroup?,
    ): String? {
        return getActionMessageText(
            context = context,
            message = message,
            youPrefix = youPrefix,
            messageUser = messageUser,
            messageGroup = messageGroup,
            action = action,
            actionUser = actionUser,
            actionGroup = actionGroup,
        )?.toString()
    }

    fun getForwardsText(message: VkMessage?): UiText? {
        if (message?.forwards.isNullOrEmpty()) return null

        return message?.forwards?.let { forwards ->
            UiText.Resource(
                if (forwards.size == 1) R.string.forwarded_message
                else R.string.forwarded_messages
            )
        }
    }

    fun getAttachmentText(message: VkMessage?): UiText? {
        message?.geo?.let {
            return when (it.type) {
                "point" -> UiText.Resource(R.string.message_geo_point)
                else -> UiText.Resource(R.string.message_geo)
            }
        }
        if (message?.attachments.isNullOrEmpty()) return null

        return message?.attachments?.let { attachments ->
            if (attachments.size == 1) {
                getAttachmentTypeByClass(attachments[0])?.let {
                    getAttachmentTextByType(it)
                }
            } else {
                if (isAttachmentsHaveOneType(attachments)) {
                    getAttachmentTypeByClass(attachments[0])?.let {
                        getAttachmentTextByType(it, attachments.size)
                    }
                } else {
                    UiText.Resource(R.string.message_attachments_many)
                }
            }
        }
    }

    fun getAttachmentConversationIcon(message: VkMessage?): UiImage? {
        return message?.attachments?.let { attachments ->
            if (attachments.isEmpty()) return null
            if (attachments.size == 1 || isAttachmentsHaveOneType(attachments)) {
                message.geo?.let {
                    return UiImage.Resource(R.drawable.ic_map_marker)
                }

                getAttachmentTypeByClass(attachments[0])?.let {
                    getAttachmentIconByType(it)
                }
            } else {
                UiImage.Resource(R.drawable.ic_baseline_attach_file_24)
            }
        }
    }

    fun getAttachmentConversationIcon(
        context: Context,
        message: VkMessage?,
    ): Drawable? {
        if (message == null) return null
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

    fun getAttachmentIconByType(attachmentType: BaseVkAttachmentItem.AttachmentType): UiImage? {
        return when (attachmentType) {
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
            else -> null
        }?.let(UiImage::Resource)
    }

    @Deprecated("Use new with UiImage")
    fun getAttachmentIconByType(
        context: Context,
        attachmentType: BaseVkAttachmentItem.AttachmentType,
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
        attachmentType: BaseVkAttachmentItem.AttachmentType,
        size: Int = 1,
    ): UiText {
        return when (attachmentType) {
            BaseVkAttachmentItem.AttachmentType.Photo ->
                UiText.QuantityResource(R.plurals.attachment_photos, size)

            BaseVkAttachmentItem.AttachmentType.Video ->
                UiText.QuantityResource(R.plurals.attachment_videos, size)

            BaseVkAttachmentItem.AttachmentType.Audio ->
                UiText.QuantityResource(R.plurals.attachment_audios, size)

            BaseVkAttachmentItem.AttachmentType.File ->
                UiText.QuantityResource(R.plurals.attachment_files, size)

            BaseVkAttachmentItem.AttachmentType.Link ->
                UiText.Resource(R.string.message_attachments_link)

            BaseVkAttachmentItem.AttachmentType.Voice ->
                UiText.Resource(R.string.message_attachments_voice)

            BaseVkAttachmentItem.AttachmentType.MiniApp ->
                UiText.Resource(R.string.message_attachments_mini_app)

            BaseVkAttachmentItem.AttachmentType.Sticker ->
                UiText.Resource(R.string.message_attachments_sticker)

            BaseVkAttachmentItem.AttachmentType.Gift ->
                UiText.Resource(R.string.message_attachments_gift)

            BaseVkAttachmentItem.AttachmentType.Wall ->
                UiText.Resource(R.string.message_attachments_wall)

            BaseVkAttachmentItem.AttachmentType.Graffiti ->
                UiText.Resource(R.string.message_attachments_graffiti)

            BaseVkAttachmentItem.AttachmentType.Poll ->
                UiText.Resource(R.string.message_attachments_poll)

            BaseVkAttachmentItem.AttachmentType.WallReply ->
                UiText.Resource(R.string.message_attachments_wall_reply)

            BaseVkAttachmentItem.AttachmentType.Call ->
                UiText.Resource(R.string.message_attachments_call)

            BaseVkAttachmentItem.AttachmentType.GroupCallInProgress ->
                UiText.Resource(R.string.message_attachments_call_in_progress)

            BaseVkAttachmentItem.AttachmentType.Event ->
                UiText.Resource(R.string.message_attachments_event)

            BaseVkAttachmentItem.AttachmentType.Curator ->
                UiText.Resource(R.string.message_attachments_curator)

            BaseVkAttachmentItem.AttachmentType.Story ->
                UiText.Resource(R.string.message_attachments_story)

            BaseVkAttachmentItem.AttachmentType.Widget ->
                UiText.Resource(R.string.message_attachments_widget)

            else -> UiText.Simple(attachmentType.value)
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

                    VkErrorCodes.AccessTokenExpired.toString() -> {
                        val tokenExpiredError =
                            gson.fromJson(errorString, TokenExpiredError::class.java)

                        tokenExpiredError
                    }

                    VkErrors.NeedValidation -> {
                        val validationError =
                            gson.fromJson(
                                errorString,
                                if (defaultError.errorMessage == VkErrorMessages.UserBanned) {
                                    UserBannedError::class.java
                                } else {
                                    ValidationRequiredError::class.java
                                }
                            )

                        validationError
                    }

                    VkErrors.NeedCaptcha -> {
                        val captchaRequiredError =
                            gson.fromJson(errorString, CaptchaRequiredError::class.java)

                        captchaRequiredError
                    }

                    VkErrors.InvalidRequest -> {
                        when (defaultError.errorType) {
                            VkErrorTypes.OtpFormatIncorrect -> WrongTwoFaCodeFormatError
                            VkErrorTypes.WrongOtp -> WrongTwoFaCodeError
                            else -> defaultError
                        }
                    }

                    else -> defaultError
                }

            return ApiAnswer.Error(error)
        } catch (e: Exception) {
            return ApiAnswer.Error(ApiError(throwable = e))
        }
    }

    fun visualizeMentions(
        messageText: String,
        mentionColor: Int,
        onMentionClick: ((id: Int) -> Unit)? = null,
    ): SpannableStringBuilder {
        if (messageText.isEmpty()) {
            return SpannableStringBuilder("")
        }

        var newMessageText = messageText

        val idsIndexes = mutableListOf<Triple<Int, Int, Int>>()
        val mentions = mutableListOf<Pair<String, String>>()

        var startFrom = 0

        while (true) {
            val leftBracketIndex = newMessageText.indexOf('[', startFrom)
            val verticalLineIndex = newMessageText.indexOf('|', startFrom)
            val rightBracketIndex = newMessageText.indexOf(']', startFrom)

            if (leftBracketIndex == -1 ||
                verticalLineIndex == -1 ||
                rightBracketIndex == -1
            ) break

            val idPart = newMessageText.substring(leftBracketIndex + 1, verticalLineIndex)
            val idPrefixStartIndex = 0
            var idPrefixEndIndex = 0

            for (i in idPart.indices) {
                idPrefixEndIndex = i

                val char = idPart[i]
                if (char.isDigit()) {
                    break
                }
            }
            val idPrefix = idPart.substring(idPrefixStartIndex, idPrefixEndIndex)
            val actualId = idPart.substring(idPrefix.length, idPart.length).toIntOrNull() ?: -1

            if (!idPart.matches(Regex("^${idPrefix}(\\d+)\$")) || rightBracketIndex - verticalLineIndex < 2) {
                break
            }

            val text = newMessageText.substring(verticalLineIndex + 1, rightBracketIndex)

            val str = "[$idPart|$text]"

            mentions += str to text

            idsIndexes += Triple(actualId, leftBracketIndex, leftBracketIndex + text.length)

            startFrom = rightBracketIndex + 1
        }

        idsIndexes.reverse()

        mentions.forEachIndexed { index, pair ->
            val old = pair.first
            val new = pair.second

            val oldIndexStart = newMessageText.indexOf(old)

            idsIndexes[index].copy(
                second = oldIndexStart,
                third = oldIndexStart + new.length
            ).let { idsIndexes[index] = it }

            newMessageText = newMessageText.replace(old, new)
        }

        val spanBuilder = SpannableStringBuilder(newMessageText)

        idsIndexes.forEach { triple ->
            val id = triple.first
            val start = triple.second
            val end = triple.third

            spanBuilder.setSpan(
                createClickableSpan(id, mentionColor, onMentionClick),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        return spanBuilder
    }

    private fun createClickableSpan(
        id: Int,
        mentionColor: Int,
        onMentionClick: ((id: Int) -> Unit)? = null,
    ): ClickableSpan {
        return object : ClickableSpan() {
            override fun onClick(widget: View) {
                widget.cancelPendingInputEvents()

                onMentionClick?.invoke(id)
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.color = mentionColor
//                ds.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            }
        }
    }

    fun VkConversationDomain.fill(
        lastMessage: VkMessage?,
        profiles: HashMap<Int, VkUser> = hashMapOf(),
        groups: HashMap<Int, VkGroup> = hashMapOf()
    ): VkConversationDomain {
        val conversation = this

        val userGroup = getConversationUserGroup(conversation, profiles, groups)
        val actionUserGroup = getMessageActionUserGroup(lastMessage, profiles, groups)
        val messageUserGroup = getMessageUserGroup(lastMessage, profiles, groups)

        conversation.conversationUser = userGroup.first
        conversation.conversationGroup = userGroup.second

        val newMessage = lastMessage?.copy(
            user = messageUserGroup.first,
            group = messageUserGroup.second,
            actionUser = actionUserGroup.first,
            actionGroup = actionUserGroup.second
        ) ?: return conversation

        conversation.lastMessage = newMessage

        return conversation
    }
}
