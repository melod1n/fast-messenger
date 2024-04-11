package com.meloda.fast.api

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.meloda.fast.R
import com.meloda.fast.api.model.data.AttachmentType
import com.meloda.fast.api.model.data.VkAttachmentItemData
import com.meloda.fast.api.model.data.VkMessageData
import com.meloda.fast.api.model.domain.VkAttachment
import com.meloda.fast.api.model.domain.VkAudioDomain
import com.meloda.fast.api.model.domain.VkFileDomain
import com.meloda.fast.api.model.domain.VkGroupDomain
import com.meloda.fast.api.model.domain.VkMessageDomain
import com.meloda.fast.api.model.domain.VkMultipleAttachment
import com.meloda.fast.api.model.domain.VkPhotoDomain
import com.meloda.fast.api.model.domain.VkUserDomain
import com.meloda.fast.api.model.domain.VkVideoDomain
import com.meloda.fast.api.model.domain.VkWallDomain
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.ext.orDots
import com.meloda.fast.model.base.UiImage
import com.meloda.fast.model.base.UiText
import com.meloda.fast.model.base.parseString

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
            VkAudioDomain::class.java -> "audio"
            VkFileDomain::class.java -> "doc"
            VkVideoDomain::class.java -> "video"
            VkPhotoDomain::class.java -> "photo"
            VkWallDomain::class.java -> "wall"
            else -> throw IllegalArgumentException("unknown attachment class: $attachmentClass")
        }

        val result = StringBuilder(type).append(ownerId).append('_').append(id)
        if (withAccessKey && !accessKey.isNullOrBlank()) {
            result.append('_')
            result.append(accessKey)
        }
        return result.toString()
    }

    fun prepareMessageText(text: String, forConversations: Boolean = false): String {
        return text.apply {
            if (forConversations) {
                replace("\n", " ")
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

//    fun isPreviousMessageSentFiveMinutesAgo(
//        prevMessage: VkMessageDomain?,
//        message: VkMessageDomain?
//    ) =
//        prevMessage != null && message != null && (message.date - prevMessage.date >= 300)

//    fun isPreviousMessageFromDifferentSender(
//        prevMessage: VkMessageDomain?,
//        message: VkMessageDomain?
//    ) =
//        prevMessage != null && message != null && prevMessage.fromId != message.fromId

    // TODO: 11/04/2024, Danil Nikolaev: fill
    fun parseForwards(baseForwards: List<VkMessageData>?): List<VkMessageDomain>? =
        baseForwards?.map(VkMessageData::mapToDomain)

    // TODO: 11/04/2024, Danil Nikolaev: fill
    fun parseReplyMessage(baseReplyMessage: VkMessageData?): VkMessageDomain? {
        if (baseReplyMessage == null) return null

        return baseReplyMessage.mapToDomain()
    }

    fun parseAttachments(baseAttachments: List<VkAttachmentItemData>?): List<VkAttachment>? {
        if (baseAttachments.isNullOrEmpty()) return null

        val attachments = mutableListOf<VkAttachment>()

        for (baseAttachment in baseAttachments) {
            when (baseAttachment.getPreparedType()) {
                AttachmentType.PHOTO -> {
                    val photo = baseAttachment.photo ?: continue
                    attachments += photo.toDomain()
                }

                AttachmentType.VIDEO -> {
                    val video = baseAttachment.video ?: continue
                    attachments += video.toDomain()
                }

                AttachmentType.AUDIO -> {
                    val audio = baseAttachment.audio ?: continue
                    attachments += audio.toDomain()
                }

                AttachmentType.FILE -> {
                    val file = baseAttachment.file ?: continue
                    attachments += file.toDomain()
                }

                AttachmentType.LINK -> {
                    val link = baseAttachment.link ?: continue
                    attachments += link.toDomain()
                }

                AttachmentType.MINI_APP -> {
                    val miniApp = baseAttachment.miniApp ?: continue
                    attachments += miniApp.toDomain()
                }

                AttachmentType.AUDIO_MESSAGE -> {
                    val voiceMessage = baseAttachment.voiceMessage ?: continue
                    attachments += voiceMessage.toDomain()
                }

                AttachmentType.STICKER -> {
                    val sticker = baseAttachment.sticker ?: continue
                    attachments += sticker.toDomain()
                }

                AttachmentType.GIFT -> {
                    val gift = baseAttachment.gift ?: continue
                    attachments += gift.toDomain()
                }

                AttachmentType.WALL -> {
                    val wall = baseAttachment.wall ?: continue
                    attachments += wall.toDomain()
                }

                AttachmentType.GRAFFITI -> {
                    val graffiti = baseAttachment.graffiti ?: continue
                    attachments += graffiti.toDomain()
                }

                AttachmentType.POLL -> {
                    val poll = baseAttachment.poll ?: continue
                    attachments += poll.toDomain()
                }

                AttachmentType.WALL_REPLY -> {
                    val wallReply = baseAttachment.wallReply ?: continue
                    attachments += wallReply.toDomain()
                }

                AttachmentType.CALL -> {
                    val call = baseAttachment.call ?: continue
                    attachments += call.toDomain()
                }

                AttachmentType.GROUP_CALL_IN_PROGRESS -> {
                    val groupCall = baseAttachment.groupCall ?: continue
                    attachments += groupCall.toDomain()
                }

                AttachmentType.CURATOR -> {
                    val curator = baseAttachment.curator ?: continue
                    attachments += curator.toDomain()
                }

                AttachmentType.EVENT -> {
                    val event = baseAttachment.event ?: continue
                    attachments += event.toDomain()
                }

                AttachmentType.STORY -> {
                    val story = baseAttachment.story ?: continue
                    attachments += story.toDomain()
                }

                AttachmentType.WIDGET -> {
                    val widget = baseAttachment.widget ?: continue
                    attachments += widget.toDomain()
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
    ): AnnotatedString? {
        return when {
            message == null -> null
            action == null -> null

            else -> buildAnnotatedString {
                val context: Context = AppGlobal.Instance

                when (action) {
                    VkMessageDomain.Action.CHAT_CREATE -> {
                        val text = message.actionText ?: return null

                        val prefix = when {
                            message.fromId == UserConfig.userId -> youPrefix
                            message.isGroup() -> messageGroup?.name
                            message.isUser() -> messageUser?.toString()
                            else -> return null
                        } ?: return null

                        val string = UiText.ResourceParams(
                            R.string.message_action_chat_created,
                            listOf(prefix, text)
                        ).parseString(context).orEmpty()

                        append(string)

                        addStyle(
                            style = SpanStyle(fontWeight = FontWeight.SemiBold),
                            start = 0,
                            end = prefix.length
                        )

                        val textStartIndex = string.indexOf(text)

                        addStyle(
                            style = SpanStyle(fontWeight = FontWeight.SemiBold),
                            start = textStartIndex,
                            end = textStartIndex + text.length
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

                        val string = UiText.ResourceParams(
                            R.string.message_action_chat_renamed,
                            listOf(prefix, text)
                        ).parseString(context).orEmpty()

                        append(string)

                        addStyle(
                            style = SpanStyle(fontWeight = FontWeight.SemiBold),
                            start = 0,
                            end = prefix.length
                        )

                        val textStartIndex = string.indexOf(text)

                        addStyle(
                            style = SpanStyle(fontWeight = FontWeight.SemiBold),
                            start = textStartIndex,
                            end = textStartIndex + text.length
                        )
                    }

                    VkMessageDomain.Action.CHAT_PHOTO_UPDATE -> {
                        val prefix = when {
                            message.fromId == UserConfig.userId -> youPrefix
                            message.isGroup() -> messageGroup?.name
                            message.isUser() -> messageUser?.toString()
                            else -> return null
                        } ?: return null

                        UiText.ResourceParams(
                            R.string.message_action_chat_photo_update,
                            listOf(prefix)
                        ).parseString(context).orEmpty().let(::append)

                        addStyle(
                            style = SpanStyle(fontWeight = FontWeight.SemiBold),
                            start = 0,
                            end = prefix.length
                        )
                    }

                    VkMessageDomain.Action.CHAT_PHOTO_REMOVE -> {
                        val prefix = when {
                            message.fromId == UserConfig.userId -> youPrefix
                            message.isGroup() -> messageGroup?.name
                            message.isUser() -> messageUser?.toString()
                            else -> return null
                        } ?: return null

                        UiText.ResourceParams(
                            R.string.message_action_chat_photo_remove,
                            listOf(prefix)
                        ).parseString(context).orEmpty().let(::append)

                        addStyle(
                            style = SpanStyle(fontWeight = FontWeight.SemiBold),
                            start = 0,
                            end = prefix.length
                        )
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

                            UiText.ResourceParams(
                                R.string.message_action_chat_user_left,
                                listOf(prefix)
                            ).parseString(context).orEmpty().let(::append)

                            addStyle(
                                style = SpanStyle(fontWeight = FontWeight.SemiBold),
                                start = 0,
                                end = prefix.length
                            )
                        } else {
                            val prefix =
                                if (message.fromId == UserConfig.userId) youPrefix
                                else messageUser?.toString() ?: messageGroup?.toString().orDots()

                            val postfix =
                                if (memberId == UserConfig.userId) youPrefix.lowercase()
                                else actionUser.toString()

                            val string = UiText.ResourceParams(
                                R.string.message_action_chat_user_kicked,
                                listOf(prefix, postfix)
                            ).parseString(context).orEmpty()

                            append(string)

                            addStyle(
                                style = SpanStyle(fontWeight = FontWeight.SemiBold),
                                start = 0,
                                end = prefix.length
                            )

                            val postfixStartIndex = string.indexOf(postfix)

                            addStyle(
                                style = SpanStyle(fontWeight = FontWeight.SemiBold),
                                start = postfixStartIndex,
                                end = postfixStartIndex + postfix.length
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
                            ).parseString(context).orEmpty().let(::append)

                            addStyle(
                                style = SpanStyle(fontWeight = FontWeight.SemiBold),
                                start = 0,
                                end = prefix.length
                            )
                        } else {
                            val prefix = if (message.fromId == UserConfig.userId) youPrefix
                            else messageUser?.toString() ?: messageGroup?.toString().orDots()

                            val postfix =
                                if (memberId == UserConfig.userId) youPrefix.lowercase()
                                else actionUser.toString()

                            val string = UiText.ResourceParams(
                                R.string.message_action_chat_user_invited,
                                listOf(prefix, postfix)
                            ).parseString(context).orEmpty()

                            append(string)

                            val postfixStartIndex = string.indexOf(postfix)

                            addStyle(
                                style = SpanStyle(fontWeight = FontWeight.SemiBold),
                                start = postfixStartIndex,
                                end = postfixStartIndex + postfix.length
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
                        ).parseString(context).orEmpty().let(::append)

                        addStyle(
                            style = SpanStyle(fontWeight = FontWeight.SemiBold),
                            start = 0,
                            end = prefix.length
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
                        ).parseString(context).orEmpty().let(::append)

                        addStyle(
                            style = SpanStyle(fontWeight = FontWeight.SemiBold),
                            start = 0,
                            end = prefix.length
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
                        ).parseString(context).orEmpty().let(::append)

                        addStyle(
                            style = SpanStyle(fontWeight = FontWeight.SemiBold),
                            start = 0,
                            end = prefix.length
                        )
                    }

                    VkMessageDomain.Action.CHAT_PIN_MESSAGE -> {
                        val prefix = when {
                            message.fromId == UserConfig.userId -> youPrefix
                            message.isGroup() -> messageGroup?.name
                            message.isUser() -> messageUser?.toString()
                            else -> return null
                        } ?: return null

                        UiText.ResourceParams(
                            R.string.message_action_chat_pin_message,
                            listOf(prefix)
                        ).parseString(context).orEmpty().let(::append)

                        addStyle(
                            style = SpanStyle(fontWeight = FontWeight.SemiBold),
                            start = 0,
                            end = prefix.length
                        )
                    }

                    VkMessageDomain.Action.CHAT_UNPIN_MESSAGE -> {
                        val prefix = when {
                            message.fromId == UserConfig.userId -> youPrefix
                            message.isGroup() -> messageGroup?.name
                            message.isUser() -> messageUser?.toString()
                            else -> return null
                        } ?: return null

                        UiText.ResourceParams(
                            R.string.message_action_chat_unpin_message,
                            listOf(prefix)
                        ).parseString(context).orEmpty().let(::append)

                        addStyle(
                            style = SpanStyle(fontWeight = FontWeight.SemiBold),
                            start = 0,
                            end = prefix.length
                        )
                    }

                    VkMessageDomain.Action.CHAT_SCREENSHOT -> {
                        val prefix = when {
                            message.fromId == UserConfig.userId -> youPrefix
                            message.isGroup() -> messageGroup?.name
                            message.isUser() -> messageUser?.toString()
                            else -> return null
                        } ?: return null

                        UiText.ResourceParams(
                            R.string.message_action_chat_screenshot,
                            listOf(prefix)
                        ).parseString(context).orEmpty().let(::append)

                        addStyle(
                            style = SpanStyle(fontWeight = FontWeight.SemiBold),
                            start = 0,
                            end = prefix.length
                        )
                    }

                    VkMessageDomain.Action.CHAT_STYLE_UPDATE -> {
                        val prefix = when {
                            message.fromId == UserConfig.userId -> youPrefix
                            message.isUser() -> messageUser?.toString()
                            else -> return null
                        } ?: return null

                        UiText.ResourceParams(
                            R.string.message_action_chat_style_update,
                            listOf(prefix)
                        ).parseString(context).orEmpty().let(::append)

                        addStyle(
                            style = SpanStyle(fontWeight = FontWeight.SemiBold),
                            start = 0,
                            end = prefix.length
                        )
                    }
                }
            }
        }
    }

    fun getForwardsText(message: VkMessageDomain?): AnnotatedString? {
        return when {
            message == null -> null

            message.hasForwards() -> buildAnnotatedString {
                val forwards = message.forwards.orEmpty()

                withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                    append(
                        UiText.Resource(
                            if (forwards.size == 1) R.string.forwarded_message
                            else R.string.forwarded_messages
                        ).parseString(AppGlobal.Instance)
                    )
                }
            }

            else -> null
        }
    }

    fun getAttachmentText(message: VkMessageDomain?): AnnotatedString? {
        val context: Context = AppGlobal.Instance
        return when {
            message == null -> null

            message.geo != null -> buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                    when (message.geo.type) {
                        "point" -> UiText.Resource(R.string.message_geo_point)
                            .parseString(context)
                            .let(::append)

                        else -> UiText.Resource(R.string.message_geo)
                            .parseString(context)
                            .let(::append)
                    }
                }
            }

            message.hasAttachments() -> buildAnnotatedString {
                val attachments = message.attachments.orEmpty()

                withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                    if (attachments.size == 1) {
                        getAttachmentUiText(attachments.first())
                            .parseString(context)
                            .let(::append)
                    } else {
                        if (isAttachmentsHaveOneType(attachments)) {
                            getAttachmentUiText(attachments.first(), attachments.size)
                                .parseString(context)
                                .let(::append)
                        } else {
                            UiText.Resource(R.string.message_attachments_many)
                                .parseString(context)
                                .let(::append)
                        }
                    }
                }
            }

            else -> null
        }
    }

    fun getAttachmentConversationIcon(message: VkMessageDomain?): UiImage? {
        return message?.attachments?.let { attachments ->
            if (attachments.isEmpty()) return null
            if (attachments.size == 1 || isAttachmentsHaveOneType(attachments)) {
                message.geo?.let {
                    return UiImage.Resource(R.drawable.ic_map_marker)
                }

                getAttachmentIconByType(attachments.first().type)
            } else {
                UiImage.Resource(R.drawable.ic_baseline_attach_file_24)
            }
        }
    }

    fun getAttachmentIconByType(attachmentType: AttachmentType): UiImage? {
        return when (attachmentType) {
            AttachmentType.PHOTO -> R.drawable.ic_attachment_photo
            AttachmentType.VIDEO -> R.drawable.ic_attachment_video
            AttachmentType.AUDIO -> R.drawable.ic_attachment_audio
            AttachmentType.FILE -> R.drawable.ic_attachment_file
            AttachmentType.LINK -> R.drawable.ic_attachment_link
            AttachmentType.AUDIO_MESSAGE -> R.drawable.ic_attachment_voice
            AttachmentType.MINI_APP -> R.drawable.ic_attachment_mini_app
            AttachmentType.STICKER -> R.drawable.ic_attachment_sticker
            AttachmentType.GIFT -> R.drawable.ic_attachment_gift
            AttachmentType.WALL -> R.drawable.ic_attachment_wall
            AttachmentType.GRAFFITI -> R.drawable.ic_attachment_graffiti
            AttachmentType.POLL -> R.drawable.ic_attachment_poll
            AttachmentType.WALL_REPLY -> R.drawable.ic_attachment_wall_reply
            AttachmentType.CALL -> R.drawable.ic_attachment_call
            AttachmentType.GROUP_CALL_IN_PROGRESS -> R.drawable.ic_attachment_group_call
            AttachmentType.STORY -> R.drawable.ic_attachment_story
            else -> null
        }?.let(UiImage::Resource)
    }

    fun isAttachmentsHaveOneType(attachments: List<VkAttachment>): Boolean {
        if (attachments.isEmpty()) return true
        if (attachments.size == 1) return true

        val firstType = attachments.first().type

        for (attachment in attachments) {
            if (firstType != attachment.type) return false
        }

        return true
    }

    fun getAttachmentUiText(
        attachment: VkAttachment,
        size: Int = 1,
    ): UiText {
        return when {
            attachment is VkMultipleAttachment -> {
                attachment.getUiText(size)
            }

            else -> attachment.getUiText()
        }
    }

    fun getTextWithVisualizedMentions(
        originalText: String,
        mentionColor: Color,
    ): AnnotatedString = buildAnnotatedString {
        val regex = """\[(id|club)(\d+)\|([^]]+)]""".toRegex()

        val mentions = mutableListOf<MentionIndex>()

        var currentIndex = 0
        val replacements = mutableListOf<Pair<IntRange, String>>()

        val result = regex.replace(originalText) { matchResult ->
            val idPrefix = matchResult.groups[1]?.value.orEmpty()
            val startIndex = matchResult.range.first
            val endIndex = matchResult.range.last

            val id = matchResult.groups[2]?.value ?: ""
            val text = matchResult.groups[3]?.value ?: ""

            val replaced =
                text.substring(startIndex, endIndex + 1)
                    .replace("[$idPrefix$id|$text]", text)

            val indexRange =
                (startIndex + currentIndex)..startIndex + currentIndex + replaced.length

            replacements.add(indexRange to replaced)

            mentions += MentionIndex(
                id = id.toIntOrNull() ?: -1,
                idPrefix = idPrefix,
                indexRange = indexRange
            )

            currentIndex += replaced.length - (endIndex - startIndex + 1)

            replaced
        }

        append(result)

        mentions.forEach { mention ->
            val startIndex = mention.indexRange.first
            val endIndex = mention.indexRange.last

            addStyle(
                style = SpanStyle(color = mentionColor),
                start = startIndex,
                end = endIndex
            )
            addStringAnnotation(
                tag = mention.idPrefix,
                annotation = mention.id.toString(),
                start = startIndex,
                end = endIndex
            )
        }
    }

    private data class MentionIndex(
        val id: Int,
        val idPrefix: String,
        val indexRange: IntRange
    )
}
