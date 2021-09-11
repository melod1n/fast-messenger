package com.meloda.fast.api

import android.content.Context
import com.meloda.fast.R
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.model.attachments.*
import com.meloda.fast.api.model.base.BaseVkMessage
import com.meloda.fast.api.model.base.attachments.BaseVkAttachmentItem

object VkUtils {

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
                    attachments += VkPhoto(
                        link = photo.sizes[0].url
                    )
                }
                BaseVkAttachmentItem.AttachmentType.VIDEO -> {
                    val video = baseAttachment.video ?: continue
                    attachments += VkVideo(
                        link = video.player
                    )
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
                    attachments += VkSticker(
                        link = sticker.images[0].url
                    )
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
//                val actionUser = profiles?.find { it.id == memberId }
//                val actionGroup = groups?.find { it.id == memberId }

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
//                val actionUser = profiles?.find { it.id == memberId }
//                val actionGroup = groups?.find { it.id == memberId }

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

                val actionMessage = message.actionMessage ?: return null

                "$prefix pinned message «$actionMessage»"
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
            null -> null
            else -> "[${message.action}]"
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
            else -> null
        }
    }

    fun getAttachmentTextByType(attachmentType: BaseVkAttachmentItem.AttachmentType): String? {
        return when (attachmentType) {
            else -> attachmentType.value
        }
    }

}