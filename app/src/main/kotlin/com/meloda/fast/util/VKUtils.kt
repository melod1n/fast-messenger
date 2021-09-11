package com.meloda.fast.util

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.meloda.fast.R
import com.meloda.fast.api.oldVKUtil
import com.meloda.fast.api.model.old.*
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.extensions.ContextExtensions.color
import com.meloda.fast.extensions.ContextExtensions.drawable
import com.meloda.fast.extensions.DrawableExtensions.tint
import com.meloda.fast.extensions.StringExtensions.lowerCase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

object VKUtils {

    fun getUserOnline(user: oldVKUser): String {
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
                r.getString(
                    R.string.user_last_seen_at,
                    oldVKUtil.getLastSeenTime(user.lastSeen * 1000L)
                )
            }
        }
    }

    fun getUserOnlineIcon(
        context: Context,
        conversation: oldVKConversation?,
        peerUser: oldVKUser?
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

    fun getUserOnlineIcon(context: Context, user: oldVKUser): Drawable? {
        return getUserOnlineIcon(context, null, user)
    }

    //    fun getAvatarPlaceholder(context: Context, dialogTitle: String): TextDrawable {
//        return TextDrawable.builder().buildRound(
//            if (dialogTitle.isEmpty()) "" else {
//                TextUtils.getFirstLetterFromString(dialogTitle)
//            },
//            context.color(R.color.accent)
//        )
//    }


    fun getAttachmentText(context: Context, attachments: List<VKModel>): String {
        val resId: Int

        if (attachments.isNotEmpty()) {
            if (attachments.size > 1) {
                var oneType = true

                val firstType = attachments[0].attachmentType

//                val className = attachments[0].javaClass.simpleName

                for (model in attachments) {
//                    if (model.javaClass.simpleName != className) {
                    if (model.attachmentType != firstType) {
                        oneType = false
                        break
                    }
                }

                return if (oneType) {
//                    val objectClass: Class<VKModel> = attachments[0].javaClass

                    resId = when (firstType) {
                        VKAttachments.Type.PHOTO -> {
                            R.string.message_attachment_photos
                        }
                        VKAttachments.Type.VIDEO -> {
                            R.string.message_attachment_videos
                        }
                        VKAttachments.Type.AUDIO -> {
                            R.string.message_attachment_audios
                        }
                        VKAttachments.Type.DOCUMENT -> {
                            R.string.message_attachment_docs
                        }
                        else -> -1

                    }
                    if (resId == -1) "Unknown attachments" else context.getString(
                        resId,
                        attachments.size
                    ).lowerCase()
                } else {
                    context.getString(R.string.message_attachments_many)
                }
            } else {
//                val objectClass: Class<VKModel> = attachments[0].javaClass
                val firstType = attachments[0].attachmentType

                resId = when (firstType) {
                    VKAttachments.Type.PHOTO -> R.string.message_attachment_photo
                    VKAttachments.Type.AUDIO -> R.string.message_attachment_audio
                    VKAttachments.Type.VIDEO -> R.string.message_attachment_video
                    VKAttachments.Type.DOCUMENT -> R.string.message_attachment_doc
                    VKAttachments.Type.GRAFFITI -> R.string.message_attachment_graffiti
                    VKAttachments.Type.VOICE_MESSAGE -> R.string.message_attachment_voice
                    VKAttachments.Type.STICKER -> R.string.message_attachment_sticker
                    VKAttachments.Type.GIFT -> R.string.message_attachment_gift
                    VKAttachments.Type.LINK -> R.string.message_attachment_link
                    VKAttachments.Type.POLL -> R.string.message_attachment_poll
                    VKAttachments.Type.CALL -> R.string.message_attachment_call
                    VKAttachments.Type.WALL_POST -> R.string.message_attachment_wall_post
                    VKAttachments.Type.WALL_REPLY -> R.string.message_attachment_wall_reply
                    else -> return "Unknown"
                }
            }
        } else {
            return ""
        }
        return context.getString(resId)
    }

    fun getAttachmentDrawable(context: Context, attachments: List<VKModel>): Drawable? {
        if (attachments.isEmpty() || attachments.size > 1) return null

        val resId = when (attachments[0].attachmentType) {
            VKAttachments.Type.PHOTO -> R.drawable.ic_message_attachment_camera
            VKAttachments.Type.AUDIO -> R.drawable.ic_message_attachment_audio
            VKAttachments.Type.VIDEO -> R.drawable.ic_message_attachment_video
            VKAttachments.Type.DOCUMENT -> R.drawable.ic_message_attachment_doc
            VKAttachments.Type.GRAFFITI -> R.drawable.ic_message_attachment_graffiti
            VKAttachments.Type.VOICE_MESSAGE -> R.drawable.ic_message_attachment_audio_message
            VKAttachments.Type.STICKER -> R.drawable.ic_message_attachment_sticker
            VKAttachments.Type.GIFT -> R.drawable.ic_message_attachment_gift
            VKAttachments.Type.LINK -> R.drawable.ic_message_attachment_link
            VKAttachments.Type.POLL -> R.drawable.ic_message_attachment_poll
            VKAttachments.Type.CALL -> R.drawable.ic_message_attachment_call

            else -> null
        }

        resId?.let { return context.drawable(it).tint(context.color(R.color.accent)) }

        return null
    }

    fun getFwdText(context: Context, forwardedMessages: List<oldVKMessage>): String {
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
        lastMessage: oldVKMessage
    ) {

        lastMessage.action?.let {
            var result = ""

            when (it.type) {
                oldVKMessageAction.Type.CHAT_CREATE -> result = context.getString(
                    R.string.message_action_created_chat,
                    ""
                )
                oldVKMessageAction.Type.INVITE_USER -> result =
                    if (lastMessage.fromId == lastMessage.action!!.memberId) {
                        context.getString(R.string.message_action_returned_to_chat, "")
                    } else {
                        ""
//                            val invited = MemoryCache.getUserById(lastMessage.action!!.memberId)
//                            context.getString(R.string.message_action_invited_user, invited)
                    }
                oldVKMessageAction.Type.INVITE_USER_BY_LINK -> result = context.getString(
                    R.string.message_action_invited_by_link,
                    ""
                )
                oldVKMessageAction.Type.KICK_USER -> result =
                    if (lastMessage.fromId == lastMessage.action!!.memberId) {
                        context.getString(R.string.message_action_left_from_chat, "")
                    } else {
                        ""
//                            val kicked = MemoryCache.getUserById(lastMessage.action!!.memberId)
//                            context.getString(R.string.message_action_kicked_user, kicked)
                    }
                oldVKMessageAction.Type.PHOTO_REMOVE -> result = context.getString(
                    R.string.message_action_removed_photo,
                    ""
                )
                oldVKMessageAction.Type.PHOTO_UPDATE -> result = context.getString(
                    R.string.message_action_updated_photo,
                    ""
                )
                oldVKMessageAction.Type.PIN_MESSAGE -> result = context.getString(
                    R.string.message_action_pinned_message,
                    ""
                )
                oldVKMessageAction.Type.UNPIN_MESSAGE -> result = context.getString(
                    R.string.message_action_unpinned_message,
                    ""
                )
                oldVKMessageAction.Type.TITLE_UPDATE -> result = context.getString(
                    R.string.message_action_updated_title,
                    ""
                )
            }


        }
    }

    fun getTime(context: Context, lastMessage: oldVKMessage): String {
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


}