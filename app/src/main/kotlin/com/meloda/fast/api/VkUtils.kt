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
import com.meloda.fast.R
import com.meloda.fast.api.base.ApiError
import com.meloda.fast.api.model.VkGroupDomain
import com.meloda.fast.api.model.VkMessageDomain
import com.meloda.fast.api.model.VkUserDomain
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
import com.meloda.fast.api.model.base.VkMessageData
import com.meloda.fast.api.model.base.attachments.BaseVkAttachmentItem
import com.meloda.fast.api.model.domain.VkConversationDomain
import com.meloda.fast.api.network.BaseOauthError
import com.meloda.fast.ext.orDots
import com.meloda.fast.model.base.UiImage
import com.meloda.fast.model.base.UiText
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi

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
        message: VkMessageDomain,
        profiles: Map<Int, VkUserDomain>
    ): VkUserDomain? {
        return (if (!message.isUser()) null
        else profiles[message.fromId])
    }

    fun getMessageActionUser(message: VkMessageDomain, profiles: Map<Int, VkUserDomain>): VkUserDomain? {
        return if (message.actionMemberId == null || message.actionMemberId <= 0) null
        else profiles[message.actionMemberId]
    }

    fun getMessageGroup(message: VkMessageDomain, groups: Map<Int, VkGroupDomain>): VkGroupDomain? {
        return (if (!message.isGroup()) null
        else groups[message.fromId])
    }

    fun getMessageActionGroup(message: VkMessageDomain, groups: Map<Int, VkGroupDomain>): VkGroupDomain? {
        return if (message.actionMemberId == null || message.actionMemberId >= 0) null
        else groups[message.actionMemberId]
    }

    fun getMessageAvatar(
        message: VkMessageDomain,
        messageUser: VkUserDomain?,
        messageGroup: VkGroupDomain?,
    ): String? {
        return when {
            message.isUser() -> messageUser?.photo200
            message.isGroup() -> messageGroup?.photo200
            else -> null
        }
    }

    fun getMessageTitle(
        message: VkMessageDomain,
        defMessageUser: VkUserDomain? = null,
        defMessageGroup: VkGroupDomain? = null,
        profiles: Map<Int, VkUserDomain>? = null,
        groups: Map<Int, VkGroupDomain>? = null,
    ): String? {
        val messageUser: VkUserDomain? =
            defMessageUser ?: if (profiles == null) null
            else profiles[message.fromId]

        val messageGroup: VkGroupDomain? =
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
        profiles: Map<Int, VkUserDomain>
    ): VkUserDomain? {
        return if (!conversation.isUser()) null
        else profiles[conversation.id]
    }

    fun getConversationGroup(
        conversation: VkConversationDomain,
        groups: Map<Int, VkGroupDomain>
    ): VkGroupDomain? {
        return if (!conversation.isGroup()) null
        else groups[conversation.id]
    }

    fun getConversationAvatar(
        conversation: VkConversationDomain,
        conversationUser: VkUserDomain?,
        conversationGroup: VkGroupDomain?,
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
        defConversationUser: VkUserDomain? = null,
        defConversationGroup: VkGroupDomain? = null,
        profiles: Map<Int, VkUserDomain>? = null,
        groups: Map<Int, VkGroupDomain>? = null,
    ): String? {
        val conversationUser: VkUserDomain? =
            defConversationUser ?: if (profiles == null) null
            else getConversationUser(conversation, profiles)

        val conversationGroup: VkGroupDomain? =
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
        profiles: Map<Int, VkUserDomain>,
        groups: Map<Int, VkGroupDomain>,
    ): Pair<VkUserDomain?, VkGroupDomain?> {
        val user: VkUserDomain? = getConversationUser(conversation, profiles)
        val group: VkGroupDomain? = getConversationGroup(conversation, groups)

        return user to group
    }

    fun getMessageUserGroup(
        message: VkMessageDomain?,
        profiles: Map<Int, VkUserDomain>,
        groups: Map<Int, VkGroupDomain>,
    ): Pair<VkUserDomain?, VkGroupDomain?> {
        if (message == null) return null to null

        val user: VkUserDomain? = getMessageUser(message, profiles)
        val group: VkGroupDomain? = getMessageGroup(message, groups)

        return user to group
    }

    fun getMessageActionUserGroup(
        message: VkMessageDomain?,
        profiles: Map<Int, VkUserDomain>,
        groups: Map<Int, VkGroupDomain>,
    ): Pair<VkUserDomain?, VkGroupDomain?> {
        if (message == null) return null to null

        val user: VkUserDomain? = getMessageActionUser(message, profiles)
        val group: VkGroupDomain? = getMessageActionGroup(message, groups)

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

    fun isPreviousMessageSentFiveMinutesAgo(prevMessage: VkMessageDomain?, message: VkMessageDomain?) =
        prevMessage != null && message != null && (message.date - prevMessage.date >= 300)

    fun isPreviousMessageFromDifferentSender(prevMessage: VkMessageDomain?, message: VkMessageDomain?) =
        prevMessage != null && message != null && prevMessage.fromId != message.fromId

    fun parseForwards(baseForwards: List<VkMessageData>?): List<VkMessageDomain>? {
        if (baseForwards.isNullOrEmpty()) return null

        val forwards = mutableListOf<VkMessageDomain>()

        for (baseForward in baseForwards) {
            forwards += baseForward.asVkMessage()
        }

        return forwards
    }

    fun parseReplyMessage(baseReplyMessage: VkMessageData?): VkMessageDomain? {
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
        message: VkMessageDomain?,
        youPrefix: String,
        messageUser: VkUserDomain?,
        messageGroup: VkGroupDomain?,
        action: VkMessageDomain.Action?,
        actionUser: VkUserDomain?,
        actionGroup: VkGroupDomain?,
    ): UiText? {
        if (message == null) return null

        return when (action) {
            VkMessageDomain.Action.CHAT_CREATE -> {
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

            VkMessageDomain.Action.CHAT_TITLE_UPDATE -> {
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

            VkMessageDomain.Action.CHAT_PHOTO_UPDATE -> {
                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isGroup() -> messageGroup?.name
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                UiText.ResourceParams(R.string.message_action_chat_photo_update, listOf(prefix))
            }

            VkMessageDomain.Action.CHAT_PHOTO_REMOVE -> {
                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isGroup() -> messageGroup?.name
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                UiText.ResourceParams(R.string.message_action_chat_photo_remove, listOf(prefix))
            }

            VkMessageDomain.Action.CHAT_KICK_USER -> {
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

            VkMessageDomain.Action.CHAT_INVITE_USER -> {
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

            VkMessageDomain.Action.CHAT_INVITE_USER_BY_LINK -> {
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

            VkMessageDomain.Action.CHAT_INVITE_USER_BY_CALL -> {
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

            VkMessageDomain.Action.CHAT_INVITE_USER_BY_CALL_LINK -> {
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

            VkMessageDomain.Action.CHAT_PIN_MESSAGE -> {
                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isGroup() -> messageGroup?.name
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                UiText.ResourceParams(R.string.message_action_chat_pin_message, listOf(prefix))
            }

            VkMessageDomain.Action.CHAT_UNPIN_MESSAGE -> {
                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isGroup() -> messageGroup?.name
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                UiText.ResourceParams(R.string.message_action_chat_unpin_message, listOf(prefix))
            }

            VkMessageDomain.Action.CHAT_SCREENSHOT -> {
                val prefix = when {
                    message.fromId == UserConfig.userId -> youPrefix
                    message.isGroup() -> messageGroup?.name
                    message.isUser() -> messageUser?.toString()
                    else -> return null
                } ?: return null

                UiText.ResourceParams(R.string.message_action_chat_screenshot, listOf(prefix))
            }

            VkMessageDomain.Action.CHAT_STYLE_UPDATE -> {
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
        message: VkMessageDomain?,
        youPrefix: String,
        messageUser: VkUserDomain?,
        messageGroup: VkGroupDomain?,
        action: VkMessageDomain.Action?,
        actionUser: VkUserDomain?,
        actionGroup: VkGroupDomain?,
    ): SpannableString? {
        if (message == null) return null

        return when (action) {
            VkMessageDomain.Action.CHAT_CREATE -> {
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

            VkMessageDomain.Action.CHAT_TITLE_UPDATE -> {
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

            VkMessageDomain.Action.CHAT_PHOTO_UPDATE -> {
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

            VkMessageDomain.Action.CHAT_PHOTO_REMOVE -> {
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

            VkMessageDomain.Action.CHAT_KICK_USER -> {
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

            VkMessageDomain.Action.CHAT_INVITE_USER -> {
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

            VkMessageDomain.Action.CHAT_INVITE_USER_BY_LINK -> {
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

            VkMessageDomain.Action.CHAT_INVITE_USER_BY_CALL -> {
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

            VkMessageDomain.Action.CHAT_INVITE_USER_BY_CALL_LINK -> {
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

            VkMessageDomain.Action.CHAT_PIN_MESSAGE -> {
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

            VkMessageDomain.Action.CHAT_UNPIN_MESSAGE -> {
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

            VkMessageDomain.Action.CHAT_SCREENSHOT -> {
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

            VkMessageDomain.Action.CHAT_STYLE_UPDATE -> {
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
        message: VkMessageDomain?,
        youPrefix: String,
        messageUser: VkUserDomain? = null,
        messageGroup: VkGroupDomain? = null,
        action: VkMessageDomain.Action?,
        actionUser: VkUserDomain?,
        actionGroup: VkGroupDomain?,
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
        message: VkMessageDomain?,
        youPrefix: String,
        messageUser: VkUserDomain? = null,
        messageGroup: VkGroupDomain? = null,
        action: VkMessageDomain.Action?,
        actionUser: VkUserDomain?,
        actionGroup: VkGroupDomain?,
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

    fun getForwardsText(message: VkMessageDomain?): UiText? {
        if (message?.forwards.isNullOrEmpty()) return null

        return message?.forwards?.let { forwards ->
            UiText.Resource(
                if (forwards.size == 1) R.string.forwarded_message
                else R.string.forwarded_messages
            )
        }
    }

    fun getAttachmentText(message: VkMessageDomain?): UiText? {
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

    fun getAttachmentConversationIcon(message: VkMessageDomain?): UiImage? {
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
        message: VkMessageDomain?,
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

//    fun getError(moshi: Moshi, jsonString: String?): ApiAnswer.Error {
//        var isOauthError = false
//
//         TODO: 30/12/2023, Danil Nikolaev: write parsing error and deciding whether it is from oauth or api
//
//        try {
//            val oauthBaseErrorAdapter: JsonAdapter<BaseOauthError> =
//                moshi.adapter(BaseOauthError::class.java)
//
//            val oauthBaseError = oauthBaseErrorAdapter.fromJson(jsonString)

//            val defaultError = moshi.fromJson(errorString, ApiError::class.java)
//
//            val error: ApiError =
//                when (defaultError.error) {
//                    VkErrorCodes.UserAuthorizationFailed.toString() -> {
//                        val authorizationError =
//                            moshi.fromJson(errorString, AuthorizationError::class.java)
//
//                        authorizationError
//                    }
//
//                    VkErrorCodes.AccessTokenExpired.toString() -> {
//                        val tokenExpiredError =
//                            moshi.fromJson(errorString, TokenExpiredError::class.java)
//
//                        tokenExpiredError
//                    }
//
//                    VkErrors.NeedValidation -> {
//                        val validationError =
//                            moshi.fromJson(
//                                errorString,
////                                if (defaultError.errorMessage == VkErrorMessages.UserBanned) {
//                                UserBannedError::class.java
////                                } else {
////                                    ValidationRequiredError::class.java
////                                }
//                            )
//
//                        validationError
//                    }
//
////                    VkErrors.NeedCaptcha -> {
////                        val captchaRequiredError =
////                            gson.fromJson(errorString, CaptchaRequiredError::class.java)
////
////                        captchaRequiredError
////                    }
//
//                    VkErrors.InvalidRequest -> {
//                        when (defaultError.errorType) {
//                            VkErrorTypes.OtpFormatIncorrect -> WrongTwoFaCodeFormatError
//                            VkErrorTypes.WrongOtp -> WrongTwoFaCodeError
//                            else -> defaultError
//                        }
//                    }
//
//                    else -> defaultError
//                }

//            return ApiAnswer.Error(ApiError())
//        } catch (e: Exception) {
//            return ApiAnswer.Error(ApiError(throwable = e))
//        }
//    }

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
        lastMessage: VkMessageDomain?,
        profiles: HashMap<Int, VkUserDomain> = hashMapOf(),
        groups: HashMap<Int, VkGroupDomain> = hashMapOf()
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
