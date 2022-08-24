package com.meloda.fast.screens.messages

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Space
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.meloda.fast.R
import com.meloda.fast.api.VkUtils
import com.meloda.fast.api.model.VkConversation
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.extensions.*
import com.meloda.fast.extensions.ImageLoader.loadWithGlide
import java.text.SimpleDateFormat
import java.util.*


class MessagesPreparator constructor(
    private val context: Context,

    private val root: View? = null,

    private val conversation: VkConversation,
    private val message: VkMessage,
    private val prevMessage: VkMessage? = null,
    private val nextMessage: VkMessage? = null,

    private val bubble: ConstraintLayout,
    private val text: TextView? = null,
    private val avatar: ImageView? = null,
    private val title: TextView? = null,
    private val spacer: Space? = null,
    private val readState: ImageView? = null,
    private val time: TextView? = null,
    private val replyContainer: FrameLayout? = null,
    private val timeReadContainer: View,
    private val attachmentContainer: LinearLayoutCompat? = null,

    private val profiles: Map<Int, VkUser>,
    private val groups: Map<Int, VkGroup>
) {

    private val backgroundMiddleIn =
        ContextCompat.getDrawable(context, R.drawable.ic_message_in_background_middle)

    private val backgroundMiddleOut =
        ContextCompat.getDrawable(context, R.drawable.ic_message_out_background_middle)

    private val rootHighlightedColor =
        ContextCompat.getColor(context, R.color.n2_100)

    private var photoClickListener: ((url: String) -> Unit)? = null
    private var replyClickListener: ((replyMessage: VkMessage) -> Unit)? = null
    private var forwardsClickListener: ((forwards: List<VkMessage>) -> Unit)? = null

    fun withPhotoClickListener(block: ((url: String) -> Unit)?): MessagesPreparator {
        this.photoClickListener = block
        return this
    }

    fun withReplyClickListener(block: ((replyMessage: VkMessage) -> Unit)?): MessagesPreparator {
        this.replyClickListener = block
        return this
    }

    fun withForwardsClickListener(block: ((forwards: List<VkMessage>) -> Unit)?): MessagesPreparator {
        this.forwardsClickListener = block
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
            val nextMessageFiveMinAfter =
                VkUtils.isPreviousMessageSentFiveMinutesAgo(message, nextMessage)

            val change = (prevMessage?.date ?: 0) - message.date

            Log.d(
                "Fast::MessagesPreparator",
                "text: ${message.text}; prevText: ${prevMessage?.text}; time change: $change; fromDiffSender: $prevSenderDiff; fiveMinAgo: $fiveMinAgo; "
            )

            title?.toggleVisibility(prevSenderDiff || fiveMinAgo)

            avatar?.visibility =
                if (nextSenderDiff
                    || (fiveMinAgo && prevSenderDiff && nextMessageFiveMinAfter)
                    || nextMessageFiveMinAfter
                    || (!prevSenderDiff && nextSenderDiff)
                    || nextMessage == null
                ) View.VISIBLE else View.INVISIBLE
        } else {
            title?.gone()
            avatar?.gone()
        }


        bubble.run {
            updateLayoutParams<ConstraintLayout.LayoutParams> {
                matchConstraintMaxWidth =
                    if (avatar?.isVisible == true) AppGlobal.screenWidth80 - avatar.width - 6.dpToPx()
                    else AppGlobal.screenWidth80
            }
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
        val isMessageRead = message.isRead(conversation)

        readState?.run {
            toggleVisibility(!isMessageRead || message.isOut)
            setImageResource(
                if (isMessageRead) R.drawable.ic_round_done_all_24
                else R.drawable.ic_round_done_24
            )
        }
    }

    private fun prepareSpacer() {
        val fiveMinAgo = VkUtils.isPreviousMessageSentFiveMinutesAgo(prevMessage, message)
        val prevSenderDiff = VkUtils.isPreviousMessageFromDifferentSender(prevMessage, message)
        spacer?.toggleVisibility(fiveMinAgo || prevSenderDiff)
    }

    private fun prepareAttachments() {
        attachmentContainer?.removeAllViews()

        if (attachmentContainer != null && replyContainer != null) {
            if (
                !message.hasAttachments() &&
                !message.hasReply() &&
                !message.hasForwards() &&
                !message.hasGeo()
            ) {
                attachmentContainer.gone()
                replyContainer.gone()
            } else {
                AttachmentInflater(
                    context = context,
                    container = attachmentContainer,
                    replyContainer = replyContainer,
                    timeReadContainer = timeReadContainer,
                    message = message,
                    groups = groups,
                    profiles = profiles
                )
                    .withPhotoClickListener(photoClickListener)
                    .withReplyClickListener(replyClickListener)
                    .withForwardsClickListener(forwardsClickListener)
                    .inflate()
            }
        }
    }

    private fun prepareBubbleBackground() {
        bubble.background = if (message.isOut) backgroundMiddleOut else backgroundMiddleIn
    }

    private fun prepareText() {
        if (text != null) {
            if (message.text == null) {
                text.clear()
                text.gone()
            } else {
                text.visible()

                val preparedText =
                    VkUtils.prepareMessageText(message.text ?: "") + "\t\t\t\t\t\t\t\t\t"

                text.text = preparedText
            }
        }
    }

    private fun prepareAvatar(
        messageUser: VkUser? = null,
        messageGroup: VkGroup? = null
    ) {
        if (avatar != null) {
            val avatarUrl = VkUtils.getMessageAvatar(message, messageUser, messageGroup)

            avatar.loadWithGlide(
                url = avatarUrl,
                crossFade = true
            )
        }
    }
}
