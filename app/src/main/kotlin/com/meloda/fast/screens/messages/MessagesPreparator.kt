package com.meloda.fast.screens.messages

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Space
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import coil.load
import com.meloda.fast.R
import com.meloda.fast.api.VkUtils
import com.meloda.fast.api.model.VkConversation
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.model.attachments.VkSticker
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.util.AndroidUtils
import com.meloda.fast.widget.BoundedLinearLayout
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


class MessagesPreparator constructor(
    private val context: Context,

    private val conversation: VkConversation,
    private val message: VkMessage,
    private val prevMessage: VkMessage? = null,
    private val nextMessage: VkMessage? = null,

    private val bubble: BoundedLinearLayout? = null,
    private val bubbleStroke: View? = null,
    private val text: TextView? = null,
    private val avatar: ImageView? = null,
    private val title: TextView? = null,
    private val spacer: Space? = null,
    private val unread: ImageView? = null,
    private val time: TextView? = null,
    private val attachmentContainer: LinearLayoutCompat? = null,
    private val attachmentSpacer: Space? = null,

    private val profiles: Map<Int, VkUser>,
    private val groups: Map<Int, VkGroup>
) {

    init {
        val maxWidth = (AppGlobal.screenWidth * 0.7).roundToInt()

        if (bubble != null) bubble.maxWidth = maxWidth
    }

    private val backgroundNormalIn =
        ContextCompat.getDrawable(context, R.drawable.ic_message_in_background)
    private val backgroundMiddleIn =
        ContextCompat.getDrawable(context, R.drawable.ic_message_in_background_middle)

    private val backgroundNormalOut =
        ContextCompat.getDrawable(context, R.drawable.ic_message_out_background)
    private val backgroundMiddleOut =
        ContextCompat.getDrawable(context, R.drawable.ic_message_out_background_middle)
    private val backgroundStrokeOut =
        ContextCompat.getDrawable(context, R.drawable.ic_message_out_background_stroke)
    private val backgroundMiddleStrokeOut =
        ContextCompat.getDrawable(context, R.drawable.ic_message_out_background_middle_stroke)

    fun prepare() {
        val messageUser: VkUser? = if (message.isUser()) {
            profiles[message.fromId]
        } else null

        val messageGroup: VkGroup? = if (message.isGroup()) {
            groups[message.fromId]
        } else null

        if (unread != null) {
            unread.isVisible = message.isRead(conversation)
        }

        if (bubble != null && time != null) {
            bubble.setOnClickListener { time.isVisible = !time.isVisible }
        }

        if (attachmentContainer != null) {
            if (message.attachments.isNullOrEmpty()) {
                attachmentContainer.isVisible = false
                attachmentContainer.removeAllViews()
            } else {
                attachmentContainer.isVisible = true
                AttachmentInflater(
                    context = context,
                    container = attachmentContainer,
                    message = message,
                    groups = groups,
                    profiles = profiles
                ).inflate()
            }
        }

        if (bubble != null) {
            val padding =
                AndroidUtils.px(if (!message.attachments.isNullOrEmpty()) 4 else 15).roundToInt()

            bubble.setPadding(padding)


            // TODO: 9/23/2021 use external function
            bubble.background =
                if (!message.attachments.isNullOrEmpty() && message.attachments!![0] is VkSticker) null
                else {
                    if (message.isOut) {
                        if (prevMessage == null || prevMessage.fromId != message.fromId) backgroundNormalOut
                        else if (prevMessage.fromId == message.fromId && message.date - prevMessage.date < 60) backgroundMiddleOut
                        else backgroundNormalOut
                    } else {
                        if (prevMessage == null || prevMessage.fromId != message.fromId) backgroundNormalIn
                        else if (prevMessage.fromId == message.fromId && message.date - prevMessage.date < 60) backgroundMiddleIn
                        else backgroundNormalIn
                    }
                }
        }

        // TODO: 9/23/2021 use external function
        bubbleStroke?.background =
            if (bubble?.background == null) null else {
                if (prevMessage == null || prevMessage.fromId != message.fromId) backgroundStrokeOut
                else if (prevMessage.fromId == message.fromId && message.date - prevMessage.date < 60) backgroundMiddleStrokeOut
                else backgroundStrokeOut
            }

        if (bubble != null && text != null) {
            if (message.text == null) {
                text.isVisible = false
                bubble.isVisible = !message.attachments.isNullOrEmpty()
                bubbleStroke?.isVisible = bubble.isVisible
            } else {
                text.isVisible = true
                bubble.isVisible = true
                bubbleStroke?.isVisible = true
                text.text = VkUtils.prepareMessageText(message.text)
            }
        }

        if (avatar != null) {
            val avatarUrl = when {
                message.isUser() && messageUser != null && !messageUser.photo200.isNullOrBlank() -> messageUser.photo200
                message.isGroup() && messageGroup != null && !messageGroup.photo200.isNullOrBlank() -> messageGroup.photo200
                else -> null
            }

            avatar.load(avatarUrl) { crossfade(100) }
        }

        spacer?.isVisible = VkUtils.isPreviousMessageSentFiveMinutesAgo(prevMessage, message)

        if (message.isPeerChat()) {

            val fromDiffSender = VkUtils.isPreviousMessageFromDifferentSender(prevMessage, message)
            val fiveMinAgo = VkUtils.isPreviousMessageSentFiveMinutesAgo(prevMessage, message)

            val change = (prevMessage?.date ?: 0) - message.date

            Log.d(
                "Fast::MessagesPreparator",
                "text: ${message.text}; prevText: ${prevMessage?.text}; time change: $change; fromDiffSender: $fromDiffSender; fiveMinAgo: $fiveMinAgo; "
            )

            title?.isVisible = fromDiffSender || fiveMinAgo

            avatar?.isInvisible = fromDiffSender && fiveMinAgo
        } else {
            title?.isVisible = false
            avatar?.isVisible = false
        }

        if (title != null) {
            val titleString = when {
                message.isUser() && messageUser != null -> messageUser.firstName
                message.isGroup() && messageGroup != null -> messageGroup.name
                else -> null
            }

            title.text = titleString
            title.measure(0, 0)

            if (bubble != null) {
                if (title.isVisible) {
                    bubble.minimumWidth = title.measuredWidth + 60
                } else {
                    bubble.minimumWidth = 0
                }
            }
        }

        attachmentSpacer?.isVisible =
            !message.attachments.isNullOrEmpty() && text?.isVisible == true

        time?.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(message.date * 1000L)
    }
}
