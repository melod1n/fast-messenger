package com.meloda.fast.screens.messages

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Space
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import coil.load
import com.meloda.fast.R
import com.meloda.fast.api.VkUtils
import com.meloda.fast.api.model.VkConversation
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.model.attachments.VkSticker
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.widget.BoundedLinearLayout
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


class MessagesPreparator constructor(
    private val context: Context,

    private val root: View? = null,

    private val conversation: VkConversation,
    private val message: VkMessage,
    private val prevMessage: VkMessage? = null,
    private val nextMessage: VkMessage? = null,

    private val bubble: BoundedLinearLayout? = null,
    private val text: TextView? = null,
    private val avatar: ImageView? = null,
    private val title: TextView? = null,
    private val spacer: Space? = null,
    private val unread: ImageView? = null,
    private val time: TextView? = null,
    private val textContainer: LinearLayoutCompat? = null,
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

    private val rootHighlightedColor =
        ContextCompat.getColor(context, R.color.n2_100)

    private var photoClickListener: ((url: String) -> Unit)? = null

    fun setPhotoClickListener(unit: ((url: String) -> Unit)?): MessagesPreparator {
        this.photoClickListener = unit
        return this
    }

    fun prepare() {
        val messageUser = VkUtils.getMessageUser(message, profiles)
        val messageGroup = VkUtils.getMessageGroup(message, groups)

        prepareRootBackground()

        prepareTime()

        prepareUnreadIndicator()

        prepareSpacer()

        prepareAttachments()

        prepareAttachmentsSpacer()

        prepareBubbleBackground()

        prepareText()

        prepareAvatar(
            messageUser = messageUser,
            messageGroup = messageGroup
        )

        if (message.isPeerChat()) {
            val prevSenderDiff = VkUtils.isPreviousMessageFromDifferentSender(prevMessage, message)
            val nextSenderDiff = VkUtils.isPreviousMessageFromDifferentSender(message, nextMessage)
            val fiveMinAgo = VkUtils.isPreviousMessageSentFiveMinutesAgo(prevMessage, message)

            val change = (prevMessage?.date ?: 0) - message.date

            Log.d(
                "Fast::MessagesPreparator",
                "text: ${message.text}; prevText: ${prevMessage?.text}; time change: $change; fromDiffSender: $prevSenderDiff; fiveMinAgo: $fiveMinAgo; "
            )

            title?.isVisible = prevSenderDiff || fiveMinAgo

            avatar?.visibility =
                if (nextSenderDiff
                    || (fiveMinAgo && prevSenderDiff)
                    || !prevSenderDiff
                    || nextMessage == null
                ) View.VISIBLE else View.INVISIBLE
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
        }
    }

    private fun prepareRootBackground() {
        if (root != null) {
            root.background =
                if (message.isSelected) ColorDrawable(rootHighlightedColor)
                else null
        }
    }

    private fun prepareTime() {
        time?.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(message.date * 1000L)
    }

    private fun prepareUnreadIndicator() {
        if (unread != null) {
            unread.isVisible = message.isRead(conversation)
        }
    }

    private fun prepareSpacer() {
        spacer?.isVisible = VkUtils.isPreviousMessageSentFiveMinutesAgo(prevMessage, message)
    }

    private fun prepareAttachments() {
        if (attachmentContainer != null && textContainer != null) {
            if (message.attachments.isNullOrEmpty()) {
                attachmentContainer.isVisible = false
                attachmentContainer.removeAllViews()
            } else {
                attachmentContainer.isVisible = true

                AttachmentInflater(
                    context = context,
                    container = attachmentContainer,
                    textContainer = textContainer,
                    message = message,
                    groups = groups,
                    profiles = profiles
                )
                    .setPhotoClickListener(photoClickListener)
                    .inflate()
            }
        }
    }

    private fun prepareAttachmentsSpacer() {
        attachmentSpacer?.isVisible =
            !message.attachments.isNullOrEmpty() && text?.isVisible == true
    }

    private fun prepareBubbleBackground() {
        if (bubble != null) {
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
    }

    private fun prepareText() {
        if (bubble != null && text != null) {
            if (message.text == null) {
                text.isVisible = false
                bubble.isVisible = !message.attachments.isNullOrEmpty()
            } else {
                text.isVisible = true
                bubble.isVisible = true
                text.text = VkUtils.prepareMessageText(message.text)
            }
        }
    }

    private fun prepareAvatar(
        messageUser: VkUser? = null,
        messageGroup: VkGroup? = null
    ) {
        if (avatar != null) {
            val avatarUrl = VkUtils.getMessageAvatar(message, messageUser, messageGroup)

            avatar.load(avatarUrl) { crossfade(100) }
        }
    }
}
