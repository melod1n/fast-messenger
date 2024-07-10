package com.meloda.app.fast.common.util

//import android.content.Context
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.AnnotatedString
//import androidx.compose.ui.text.SpanStyle
//import androidx.compose.ui.text.buildAnnotatedString
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.withStyle
//import com.meloda.app.fast.common.UiImage
//import com.meloda.app.fast.common.UiText
//import com.meloda.app.fast.common.extensions.orDots
//import com.meloda.app.fast.common.parseString
//
//
//@Suppress("MemberVisibilityCanBePrivate")
//object VkUtils {
//
//    fun prepareMessageText(text: String, forConversations: Boolean = false): String {
//        return text.apply {
//            if (forConversations) {
//                replace("\n", " ")
//            }
//
//            replace("&amp;", "&")
//            replace("&quot;", "\"")
//            replace("<br>", "\n")
//            replace("&gt;", ">")
//            replace("&lt;", "<")
//            replace("<br/>", "\n")
//            replace("&ndash;", "-")
//            trim()
//        }
//    }
//
//    fun parseAttachments(baseAttachments: List<VkAttachmentItemData>?): List<VkAttachment>? {
//        if (baseAttachments.isNullOrEmpty()) return null
//
//        val attachments = mutableListOf<VkAttachment>()
//
//        for (baseAttachment in baseAttachments) {
//            when (baseAttachment.getPreparedType()) {
//                AttachmentType.UNKNOWN -> continue
//
//                AttachmentType.PHOTO -> {
//                    val photo = baseAttachment.photo ?: continue
//                    attachments += photo.toDomain()
//                }
//
//                AttachmentType.VIDEO -> {
//                    val video = baseAttachment.video ?: continue
//                    attachments += video.toDomain()
//                }
//
//                AttachmentType.AUDIO -> {
//                    val audio = baseAttachment.audio ?: continue
//                    attachments += audio.toDomain()
//                }
//
//                AttachmentType.FILE -> {
//                    val file = baseAttachment.file ?: continue
//                    attachments += file.toDomain()
//                }
//
//                AttachmentType.LINK -> {
//                    val link = baseAttachment.link ?: continue
//                    attachments += link.toDomain()
//                }
//
//                AttachmentType.MINI_APP -> {
//                    val miniApp = baseAttachment.miniApp ?: continue
//                    attachments += miniApp.toDomain()
//                }
//
//                AttachmentType.AUDIO_MESSAGE -> {
//                    val voiceMessage = baseAttachment.voiceMessage ?: continue
//                    attachments += voiceMessage.toDomain()
//                }
//
//                AttachmentType.STICKER -> {
//                    val sticker = baseAttachment.sticker ?: continue
//                    attachments += sticker.toDomain()
//                }
//
//                AttachmentType.GIFT -> {
//                    val gift = baseAttachment.gift ?: continue
//                    attachments += gift.toDomain()
//                }
//
//                AttachmentType.WALL -> {
//                    val wall = baseAttachment.wall ?: continue
//                    attachments += wall.toDomain()
//                }
//
//                AttachmentType.GRAFFITI -> {
//                    val graffiti = baseAttachment.graffiti ?: continue
//                    attachments += graffiti.toDomain()
//                }
//
//                AttachmentType.POLL -> {
//                    val poll = baseAttachment.poll ?: continue
//                    attachments += poll.toDomain()
//                }
//
//                AttachmentType.WALL_REPLY -> {
//                    val wallReply = baseAttachment.wallReply ?: continue
//                    attachments += wallReply.toDomain()
//                }
//
//                AttachmentType.CALL -> {
//                    val call = baseAttachment.call ?: continue
//                    attachments += call.toDomain()
//                }
//
//                AttachmentType.GROUP_CALL_IN_PROGRESS -> {
//                    val groupCall = baseAttachment.groupCall ?: continue
//                    attachments += groupCall.toDomain()
//                }
//
//                AttachmentType.CURATOR -> {
//                    val curator = baseAttachment.curator ?: continue
//                    attachments += curator.toDomain()
//                }
//
//                AttachmentType.EVENT -> {
//                    val event = baseAttachment.event ?: continue
//                    attachments += event.toDomain()
//                }
//
//                AttachmentType.STORY -> {
//                    val story = baseAttachment.story ?: continue
//                    attachments += story.toDomain()
//                }
//
//                AttachmentType.WIDGET -> {
//                    val widget = baseAttachment.widget ?: continue
//                    attachments += widget.toDomain()
//                }
//
//                AttachmentType.ARTIST -> {
//                    val artist = baseAttachment.artist ?: continue
//                    attachments += artist.toDomain()
//
//                    val audios = baseAttachment.audios ?: continue
//                    audios.map(VkAudioData::toDomain).let(attachments::addAll)
//                }
//
//                AttachmentType.AUDIO_PLAYLIST -> {
//                    val audioPlaylist = baseAttachment.audioPlaylist ?: continue
//                    attachments += audioPlaylist.toDomain()
//                }
//
//                AttachmentType.PODCAST -> {
//                    val podcast = baseAttachment.podcast ?: continue
//                    attachments += podcast.toDomain()
//                }
//            }
//        }
//
//        return attachments
//    }
//
//    fun getActionMessageText(
//        context: Context,
//        message: VkMessage?,
//        youPrefix: String,
//        messageUser: VkUserDomain?,
//        messageGroup: VkGroupDomain?,
//        action: VkMessage.Action?,
//        actionUser: VkUserDomain?,
//        actionGroup: VkGroupDomain?,
//    ): AnnotatedString? {
//        return when {
//            message == null -> null
//            action == null -> null
//
//            else -> buildAnnotatedString {
//                when (action) {
//                    VkMessage.Action.CHAT_CREATE -> {
//                        val text = message.actionText ?: return null
//
//                        val prefix = when {
//                            message.fromId == UserConfig.userId -> youPrefix
//                            message.isGroup() -> messageGroup?.name
//                            message.isUser() -> messageUser?.toString()
//                            else -> return null
//                        } ?: return null
//
//                        val string = UiText.ResourceParams(
//                            UiR.string.message_action_chat_created,
//                            listOf(prefix, text)
//                        ).parseString(context).orEmpty()
//
//                        append(string)
//
//                        addStyle(
//                            style = SpanStyle(fontWeight = FontWeight.SemiBold),
//                            start = 0,
//                            end = prefix.length
//                        )
//
//                        val textStartIndex = string.indexOf(text)
//
//                        addStyle(
//                            style = SpanStyle(fontWeight = FontWeight.SemiBold),
//                            start = textStartIndex,
//                            end = textStartIndex + text.length
//                        )
//                    }
//
//                    VkMessage.Action.CHAT_TITLE_UPDATE -> {
//                        val text = message.actionText ?: return null
//
//                        val prefix = when {
//                            message.fromId == UserConfig.userId -> youPrefix
//                            message.isGroup() -> messageGroup?.name
//                            message.isUser() -> messageUser?.toString()
//                            else -> return null
//                        } ?: return null
//
//                        val string = UiText.ResourceParams(
//                            UiR.string.message_action_chat_renamed,
//                            listOf(prefix, text)
//                        ).parseString(context).orEmpty()
//
//                        append(string)
//
//                        addStyle(
//                            style = SpanStyle(fontWeight = FontWeight.SemiBold),
//                            start = 0,
//                            end = prefix.length
//                        )
//
//                        val textStartIndex = string.indexOf(text)
//
//                        addStyle(
//                            style = SpanStyle(fontWeight = FontWeight.SemiBold),
//                            start = textStartIndex,
//                            end = textStartIndex + text.length
//                        )
//                    }
//
//                    VkMessage.Action.CHAT_PHOTO_UPDATE -> {
//                        val prefix = when {
//                            message.fromId == UserConfig.userId -> youPrefix
//                            message.isGroup() -> messageGroup?.name
//                            message.isUser() -> messageUser?.toString()
//                            else -> return null
//                        } ?: return null
//
//                        UiText.ResourceParams(
//                            UiR.string.message_action_chat_photo_update,
//                            listOf(prefix)
//                        ).parseString(context).orEmpty().let(::append)
//
//                        addStyle(
//                            style = SpanStyle(fontWeight = FontWeight.SemiBold),
//                            start = 0,
//                            end = prefix.length
//                        )
//                    }
//
//                    VkMessage.Action.CHAT_PHOTO_REMOVE -> {
//                        val prefix = when {
//                            message.fromId == UserConfig.userId -> youPrefix
//                            message.isGroup() -> messageGroup?.name
//                            message.isUser() -> messageUser?.toString()
//                            else -> return null
//                        } ?: return null
//
//                        UiText.ResourceParams(
//                            UiR.string.message_action_chat_photo_remove,
//                            listOf(prefix)
//                        ).parseString(context).orEmpty().let(::append)
//
//                        addStyle(
//                            style = SpanStyle(fontWeight = FontWeight.SemiBold),
//                            start = 0,
//                            end = prefix.length
//                        )
//                    }
//
//                    VkMessage.Action.CHAT_KICK_USER -> {
//                        val memberId = message.actionMemberId ?: return null
//                        val isUser = memberId > 0
//                        val isGroup = memberId < 0
//
//                        if (isUser && actionUser == null) return null
//                        if (isGroup && actionGroup == null) return null
//
//                        if (memberId == message.fromId) {
//                            val prefix =
//                                if (memberId == UserConfig.userId) youPrefix
//                                else actionUser.toString()
//
//                            UiText.ResourceParams(
//                                UiR.string.message_action_chat_user_left,
//                                listOf(prefix)
//                            ).parseString(context).orEmpty().let(::append)
//
//                            addStyle(
//                                style = SpanStyle(fontWeight = FontWeight.SemiBold),
//                                start = 0,
//                                end = prefix.length
//                            )
//                        } else {
//                            val prefix =
//                                if (message.fromId == UserConfig.userId) youPrefix
//                                else messageUser?.toString() ?: messageGroup?.toString().orDots()
//
//                            val postfix =
//                                if (memberId == UserConfig.userId) youPrefix.lowercase()
//                                else actionUser.toString()
//
//                            val string = UiText.ResourceParams(
//                                UiR.string.message_action_chat_user_kicked,
//                                listOf(prefix, postfix)
//                            ).parseString(context).orEmpty()
//
//                            append(string)
//
//                            addStyle(
//                                style = SpanStyle(fontWeight = FontWeight.SemiBold),
//                                start = 0,
//                                end = prefix.length
//                            )
//
//                            val postfixStartIndex = string.indexOf(postfix)
//
//                            addStyle(
//                                style = SpanStyle(fontWeight = FontWeight.SemiBold),
//                                start = postfixStartIndex,
//                                end = postfixStartIndex + postfix.length
//                            )
//                        }
//                    }
//
//                    VkMessage.Action.CHAT_INVITE_USER -> {
//                        val memberId = message.actionMemberId ?: 0
//                        val isUser = memberId > 0
//                        val isGroup = memberId < 0
//
//                        if (isUser && actionUser == null) return null
//                        if (isGroup && actionGroup == null) return null
//
//                        if (memberId == message.fromId) {
//                            val prefix =
//                                if (memberId == UserConfig.userId) youPrefix
//                                else actionUser.toString()
//
//                            UiText.ResourceParams(
//                                UiR.string.message_action_chat_user_returned,
//                                listOf(prefix)
//                            ).parseString(context).orEmpty().let(::append)
//
//                            addStyle(
//                                style = SpanStyle(fontWeight = FontWeight.SemiBold),
//                                start = 0,
//                                end = prefix.length
//                            )
//                        } else {
//                            val prefix =
//                                if (message.fromId == UserConfig.userId) youPrefix
//                                else messageUser?.toString() ?: messageGroup?.toString().orDots()
//
//                            val postfix =
//                                if (memberId == UserConfig.userId) youPrefix.lowercase()
//                                else actionUser.toString()
//
//                            val string = UiText.ResourceParams(
//                                UiR.string.message_action_chat_user_invited,
//                                listOf(prefix, postfix)
//                            ).parseString(context).orEmpty()
//
//                            append(string)
//
//                            val postfixStartIndex = string.indexOf(postfix)
//
//                            addStyle(
//                                style = SpanStyle(fontWeight = FontWeight.SemiBold),
//                                start = postfixStartIndex,
//                                end = postfixStartIndex + postfix.length
//                            )
//                        }
//                    }
//
//                    VkMessage.Action.CHAT_INVITE_USER_BY_LINK -> {
//                        val prefix = when {
//                            message.fromId == UserConfig.userId -> youPrefix
//                            message.isUser() -> messageUser?.toString()
//                            else -> return null
//                        } ?: return null
//
//                        UiText.ResourceParams(
//                            UiR.string.message_action_chat_user_joined_by_link,
//                            listOf(prefix)
//                        ).parseString(context).orEmpty().let(::append)
//
//                        addStyle(
//                            style = SpanStyle(fontWeight = FontWeight.SemiBold),
//                            start = 0,
//                            end = prefix.length
//                        )
//                    }
//
//                    VkMessage.Action.CHAT_INVITE_USER_BY_CALL -> {
//                        val prefix = when {
//                            message.fromId == UserConfig.userId -> youPrefix
//                            message.isUser() -> messageUser?.toString()
//                            else -> return null
//                        } ?: return null
//
//                        UiText.ResourceParams(
//                            UiR.string.message_action_chat_user_joined_by_call,
//                            listOf(prefix)
//                        ).parseString(context).orEmpty().let(::append)
//
//                        addStyle(
//                            style = SpanStyle(fontWeight = FontWeight.SemiBold),
//                            start = 0,
//                            end = prefix.length
//                        )
//                    }
//
//                    VkMessage.Action.CHAT_INVITE_USER_BY_CALL_LINK -> {
//                        val prefix = when {
//                            message.fromId == UserConfig.userId -> youPrefix
//                            message.isUser() -> messageUser?.toString()
//                            else -> return null
//                        } ?: return null
//
//                        UiText.ResourceParams(
//                            UiR.string.message_action_chat_user_joined_by_call_link,
//                            listOf(prefix)
//                        ).parseString(context).orEmpty().let(::append)
//
//                        addStyle(
//                            style = SpanStyle(fontWeight = FontWeight.SemiBold),
//                            start = 0,
//                            end = prefix.length
//                        )
//                    }
//
//                    VkMessage.Action.CHAT_PIN_MESSAGE -> {
//                        val prefix = when {
//                            message.fromId == UserConfig.userId -> youPrefix
//                            message.isGroup() -> messageGroup?.name
//                            message.isUser() -> messageUser?.toString()
//                            else -> return null
//                        } ?: return null
//
//                        UiText.ResourceParams(
//                            UiR.string.message_action_chat_pin_message,
//                            listOf(prefix)
//                        ).parseString(context).orEmpty().let(::append)
//
//                        addStyle(
//                            style = SpanStyle(fontWeight = FontWeight.SemiBold),
//                            start = 0,
//                            end = prefix.length
//                        )
//                    }
//
//                    VkMessage.Action.CHAT_UNPIN_MESSAGE -> {
//                        val prefix = when {
//                            message.fromId == UserConfig.userId -> youPrefix
//                            message.isGroup() -> messageGroup?.name
//                            message.isUser() -> messageUser?.toString()
//                            else -> return null
//                        } ?: return null
//
//                        UiText.ResourceParams(
//                            UiR.string.message_action_chat_unpin_message,
//                            listOf(prefix)
//                        ).parseString(context).orEmpty().let(::append)
//
//                        addStyle(
//                            style = SpanStyle(fontWeight = FontWeight.SemiBold),
//                            start = 0,
//                            end = prefix.length
//                        )
//                    }
//
//                    VkMessage.Action.CHAT_SCREENSHOT -> {
//                        val prefix = when {
//                            message.fromId == UserConfig.userId -> youPrefix
//                            message.isGroup() -> messageGroup?.name
//                            message.isUser() -> messageUser?.toString()
//                            else -> return null
//                        } ?: return null
//
//                        UiText.ResourceParams(
//                            UiR.string.message_action_chat_screenshot,
//                            listOf(prefix)
//                        ).parseString(context).orEmpty().let(::append)
//
//                        addStyle(
//                            style = SpanStyle(fontWeight = FontWeight.SemiBold),
//                            start = 0,
//                            end = prefix.length
//                        )
//                    }
//
//                    VkMessage.Action.CHAT_STYLE_UPDATE -> {
//                        val prefix = when {
//                            message.fromId == UserConfig.userId -> youPrefix
//                            message.isUser() -> messageUser?.toString()
//                            else -> return null
//                        } ?: return null
//
//                        UiText.ResourceParams(
//                            UiR.string.message_action_chat_style_update,
//                            listOf(prefix)
//                        ).parseString(context).orEmpty().let(::append)
//
//                        addStyle(
//                            style = SpanStyle(fontWeight = FontWeight.SemiBold),
//                            start = 0,
//                            end = prefix.length
//                        )
//                    }
//                }
//            }
//        }
//    }
//
//    fun getForwardsText(context: Context, message: VkMessage?): AnnotatedString? {
//        return when {
//            message == null -> null
//
//            message.hasForwards() -> buildAnnotatedString {
//                val forwards = message.forwards.orEmpty()
//
//                withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
//                    append(
//                        UiText.Resource(
//                            if (forwards.size == 1) UiR.string.forwarded_message
//                            else UiR.string.forwarded_messages
//                        ).parseString(context)
//                    )
//                }
//            }
//
//            else -> null
//        }
//    }
//
//    fun getAttachmentText(
//        getText: (UiText) -> String,
//        message: VkMessage?
//    ): AnnotatedString? {
//        return when {
//            message == null -> null
//
//            message.geoType != null -> buildAnnotatedString {
//                withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
//                    when (message.geoType) {
//                        "point" -> getText(UiText.Resource(UiR.string.message_geo_point))
//                            .let(::append)
//
//                        else -> getText(UiText.Resource(UiR.string.message_geo))
//                            .let(::append)
//                    }
//                }
//            }
//
//            message.hasAttachments() -> buildAnnotatedString {
//                val attachments = message.attachments.orEmpty()
//
//                withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
//                    if (attachments.size == 1) {
//                        getText(getAttachmentUiText(attachments.first())).let(::append)
//                    } else {
//                        when {
//                            isAttachmentsHaveOneType(attachments) -> {
//                                getText(getAttachmentUiText(attachments.first(), attachments.size))
//                                    .let(::append)
//                            }
//
//                            attachments.any { it.type == AttachmentType.ARTIST } -> {
//                                getText(
//                                    getAttachmentUiText(attachments.first { it.type == AttachmentType.ARTIST })
//                                ).let(::append)
//                            }
//
//                            else -> {
//                                getText(UiText.Resource(UiR.string.message_attachments_many))
//                                    .let(::append)
//                            }
//                        }
//                    }
//                }
//            }
//
//            else -> null
//        }
//    }
//
//    fun getAttachmentConversationIcon(message: VkMessage?): UiImage? {
//        return message?.attachments?.let { attachments ->
//            if (attachments.isEmpty()) return null
//            if (attachments.size == 1 || isAttachmentsHaveOneType(attachments)) {
//                message.geoType?.let {
//                    return UiImage.Resource(UiR.drawable.ic_map_marker)
//                }
//
//                getAttachmentIconByType(attachments.first().type)
//            } else {
//                UiImage.Resource(UiR.drawable.ic_baseline_attach_file_24)
//            }
//        }
//    }
//
//
//
//    fun getAttachmentUiText(
//        attachment: VkAttachment,
//        size: Int = 1,
//    ): UiText {
//        if (attachment.type.isMultiple()) {
//            return when (attachment.type) {
//                AttachmentType.PHOTO -> UiR.plurals.attachment_photos
//                AttachmentType.VIDEO -> UiR.plurals.attachment_videos
//                AttachmentType.AUDIO -> UiR.plurals.attachment_audios
//                AttachmentType.FILE -> UiR.plurals.attachment_files
//                else -> throw IllegalArgumentException("Unknown multiple type: ${attachment.type}")
//            }.let { resId -> UiText.QuantityResource(resId, size) }
//        }
//
//        return when (attachment.type) {
//            AttachmentType.UNKNOWN,
//            AttachmentType.PHOTO,
//            AttachmentType.VIDEO,
//            AttachmentType.AUDIO,
//            AttachmentType.FILE -> {
//                throw IllegalArgumentException("Unknown multiple type: ${attachment.type}")
//            }
//
//            AttachmentType.LINK -> UiR.string.message_attachments_link
//            AttachmentType.AUDIO_MESSAGE -> UiR.string.message_attachments_audio_message
//            AttachmentType.MINI_APP -> UiR.string.message_attachments_mini_app
//            AttachmentType.STICKER -> UiR.string.message_attachments_sticker
//            AttachmentType.GIFT -> UiR.string.message_attachments_gift
//            AttachmentType.WALL -> UiR.string.message_attachments_wall
//            AttachmentType.GRAFFITI -> UiR.string.message_attachments_graffiti
//            AttachmentType.POLL -> UiR.string.message_attachments_poll
//            AttachmentType.WALL_REPLY -> UiR.string.message_attachments_wall_reply
//            AttachmentType.CALL -> UiR.string.message_attachments_call
//            AttachmentType.GROUP_CALL_IN_PROGRESS -> UiR.string.message_attachments_call_in_progress
//            AttachmentType.CURATOR -> UiR.string.message_attachments_curator
//            AttachmentType.EVENT -> UiR.string.message_attachments_event
//            AttachmentType.STORY -> UiR.string.message_attachments_story
//            AttachmentType.WIDGET -> UiR.string.message_attachments_widget
//            AttachmentType.ARTIST -> UiR.string.message_attachments_artist
//            AttachmentType.AUDIO_PLAYLIST -> UiR.string.message_attachments_audio_playlist
//            AttachmentType.PODCAST -> UiR.string.message_attachments_podcast
//        }.let(UiText::Resource)
//    }
//
//    fun getTextWithVisualizedMentions(
//        originalText: String,
//        mentionColor: Color,
//    ): AnnotatedString = buildAnnotatedString {
//        val regex = """\[(id|club)(\d+)\|([^]]+)]""".toRegex()
//
//        val mentions = mutableListOf<MentionIndex>()
//
//        var currentIndex = 0
//        val replacements = mutableListOf<Pair<IntRange, String>>()
//
//        // TODO: 25/04/2024, Danil Nikolaev: check why not working ([id279494346|@iworld2rist] да убери ты Елену Шлипс от меня)
//        val result = regex.replace(originalText) { matchResult ->
//            val idPrefix = matchResult.groups[1]?.value.orEmpty()
//            val startIndex = matchResult.range.first
//            val endIndex = matchResult.range.last
//
//            val id = matchResult.groups[2]?.value ?: ""
//            val text = matchResult.groups[3]?.value ?: ""
//
//            val replaced =
//                text.substring(startIndex, endIndex + 1)
//                    .replace("[$idPrefix$id|$text]", text)
//
//            val indexRange =
//                (startIndex + currentIndex)..startIndex + currentIndex + replaced.length
//
//            replacements.add(indexRange to replaced)
//
//            mentions += MentionIndex(
//                id = id.toIntOrNull() ?: -1,
//                idPrefix = idPrefix,
//                indexRange = indexRange
//            )
//
//            currentIndex += replaced.length - (endIndex - startIndex + 1)
//
//            replaced
//        }
//
//        append(result)
//
//        mentions.forEach { mention ->
//            val startIndex = mention.indexRange.first
//            val endIndex = mention.indexRange.last
//
//            addStyle(
//                style = SpanStyle(color = mentionColor),
//                start = startIndex,
//                end = endIndex
//            )
//            addStringAnnotation(
//                tag = mention.idPrefix,
//                annotation = mention.id.toString(),
//                start = startIndex,
//                end = endIndex
//            )
//        }
//    }
//
//
//}
