package com.meloda.fast.screens.messages

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import coil.load
import com.meloda.fast.R
import com.meloda.fast.api.VkUtils
import com.meloda.fast.api.model.VkConversation
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.model.attachments.VkPhoto
import com.meloda.fast.api.model.attachments.VkSticker
import com.meloda.fast.base.adapter.BaseAdapter
import com.meloda.fast.base.adapter.BaseHolder
import com.meloda.fast.databinding.*
import com.meloda.fast.util.AndroidUtils
import kotlin.math.roundToInt

class MessagesHistoryAdapter constructor(
    context: Context,
    values: MutableList<VkMessage>,
    val conversation: VkConversation,
    val profiles: HashMap<Int, VkUser> = hashMapOf(),
    val groups: HashMap<Int, VkGroup> = hashMapOf()
) : BaseAdapter<VkMessage, MessagesHistoryAdapter.Holder>(context, values, COMPARATOR) {

    override fun getItemViewType(position: Int): Int {
        when {
            isPositionHeader(position) -> return HEADER
            isPositionFooter(position) -> return FOOTER
        }

        getItem(position).let { message ->
            if (message.action != null) return SERVICE

            if (!message.attachments.isNullOrEmpty()) {
                val attachments = message.attachments ?: return@let
                if (VkUtils.isAttachmentsHaveOneType(attachments) &&
                    attachments[0] is VkPhoto
                ) return if (message.isOut) ATTACHMENT_PHOTOS_OUT
                else ATTACHMENT_PHOTOS_IN


                if (attachments[0] is VkSticker) return if (message.isOut) ATTACHMENT_STICKER_OUT
                else ATTACHMENT_STICKER_IN
            }

            if (message.isOut) return OUTGOING
            if (!message.isOut) return INCOMING
        }

        return -1
    }

    private fun isPositionHeader(position: Int) = position == 0
    private fun isPositionFooter(position: Int) = position >= actualSize

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return when (viewType) {
            HEADER -> Header(createEmptyView(60))
            FOOTER -> Footer(createEmptyView(36))
            SERVICE -> ServiceMessage(
                ItemMessageServiceBinding.inflate(inflater, parent, false)
            )
            ATTACHMENT_STICKER_IN -> AttachmentStickerIncoming(
                ItemMessageAttachmentStickerInBinding.inflate(inflater, parent, false)
            )
            ATTACHMENT_STICKER_OUT -> AttachmentStickerOutgoing(
                ItemMessageAttachmentStickerOutBinding.inflate(inflater, parent, false)
            )
            ATTACHMENT_PHOTOS_IN -> AttachmentPhotosIncoming(
                ItemMessageAttachmentPhotosInBinding.inflate(inflater, parent, false)
            )
            ATTACHMENT_PHOTOS_OUT -> AttachmentPhotosOutgoing(
                ItemMessageAttachmentPhotosOutBinding.inflate(inflater, parent, false)
            )
            OUTGOING -> OutgoingMessage(
                ItemMessageOutBinding.inflate(inflater, parent, false)
            )
            INCOMING -> IncomingMessage(
                ItemMessageInBinding.inflate(inflater, parent, false)
            )
            else -> throw IllegalStateException("Wrong viewType: $viewType")
        }
    }

    private fun createEmptyView(size: Int) = View(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            AndroidUtils.px(size).roundToInt()
        )

        isEnabled = false
        isClickable = false
        isFocusable = false
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        if (holder is Header || holder is Footer) return

        initListeners(holder.itemView, position)
        holder.bind(position)
    }

    open inner class Holder(v: View = View(context)) : BaseHolder(v)

    inner class Header(v: View) : Holder(v)

    inner class Footer(v: View) : Holder(v)

    inner class IncomingMessage(
        private val binding: ItemMessageInBinding
    ) : Holder(binding.root) {

        private val backgroundNormal =
            ContextCompat.getDrawable(context, R.drawable.ic_message_in_background)
        private val backgroundMiddle =
            ContextCompat.getDrawable(context, R.drawable.ic_message_in_background_middle)

        init {
            MessagesManager.setRootMaxWidth(binding.bubble)
        }

        override fun bind(position: Int) {
            val message = getItem(position)

            val prevMessage = getOrNull(position - 1)
            val nextMessage = getOrNull(position + 1)

            binding.unread.isVisible = message.isRead(conversation)

            binding.bubble.background =
                if (prevMessage == null || prevMessage.fromId != message.fromId) backgroundNormal
                else if (prevMessage.fromId == message.fromId && message.date - prevMessage.date < 60) backgroundMiddle
                else backgroundNormal

            if (!message.isPeerChat()) {
                binding.title.isVisible = false
                binding.avatar.isVisible = false

                binding.spacer.isVisible =
                    !(prevMessage != null && prevMessage.fromId == message.fromId && message.date - prevMessage.date < 60)
            } else {
                binding.title.isVisible =
                    if (prevMessage == null || prevMessage.fromId != message.fromId) message.isPeerChat()
                    else message.date - prevMessage.date >= 60

                binding.spacer.isVisible = binding.title.isVisible

                binding.avatar.visibility =
                    if (nextMessage == null || nextMessage.fromId != message.fromId) if (message.isPeerChat()) View.VISIBLE else View.GONE
                    else if (nextMessage.date - message.date >= 60) View.VISIBLE
                    else View.INVISIBLE
            }

            val messageUser: VkUser? = if (message.isUser()) {
                profiles[message.fromId]
            } else null

            val messageGroup: VkGroup? = if (message.isGroup()) {
                groups[message.fromId]
            } else null

            MessagesManager.loadMessageAvatar(
                message = message,
                messageUser = messageUser,
                messageGroup = messageGroup,
                imageView = binding.avatar
            )

            val title = when {
                message.isUser() && messageUser != null -> messageUser.firstName
                message.isGroup() && messageGroup != null -> messageGroup.name
                else -> null
            }

            binding.title.text = title
            binding.title.measure(0, 0)

            if (binding.title.isVisible) {
                binding.bubble.minimumWidth = binding.title.measuredWidth + 60
            } else {
                binding.bubble.minimumWidth = 0
            }

            MessagesManager.setMessageText(
                message = message,
                textView = binding.text
            )
        }
    }

    inner class OutgoingMessage(
        private val binding: ItemMessageOutBinding
    ) : Holder(binding.root) {

        private val backgroundNormal =
            ContextCompat.getDrawable(context, R.drawable.ic_message_out_background)
        private val backgroundMiddle =
            ContextCompat.getDrawable(context, R.drawable.ic_message_out_background_middle)
        private val backgroundStroke =
            ContextCompat.getDrawable(context, R.drawable.ic_message_out_background_stroke)
        private val backgroundMiddleStroke =
            ContextCompat.getDrawable(context, R.drawable.ic_message_out_background_middle_stroke)

        init {
            MessagesManager.setRootMaxWidth(binding.bubble)
        }

        override fun bind(position: Int) {
            val message = getItem(position)

            val prevMessage = getOrNull(position - 1)

            binding.text.text = message.text ?: "[no_message]"

            binding.unread.isVisible = message.isRead(conversation)

            binding.spacer.isVisible =
                !(prevMessage != null && prevMessage.fromId == message.fromId && message.date - prevMessage.date < 60)

            binding.bubble.background =
                if (prevMessage == null || prevMessage.fromId != message.fromId) backgroundNormal
                else if (prevMessage.fromId == message.fromId && message.date - prevMessage.date < 60) backgroundMiddle
                else backgroundNormal

            binding.bubbleStroke.background =
                if (prevMessage == null || prevMessage.fromId != message.fromId) backgroundStroke
                else if (prevMessage.fromId == message.fromId && message.date - prevMessage.date < 60) backgroundMiddleStroke
                else backgroundStroke
        }

    }

    inner class ServiceMessage(
        private val binding: ItemMessageServiceBinding
    ) : Holder(binding.root) {

        private val youPrefix = context.getString(R.string.you_message_prefix)

        init {
            binding.photo.shapeAppearanceModel.run {
                withCornerSize { AndroidUtils.px(4) }
            }
        }

        override fun bind(position: Int) {
            val message = getItem(position)

            val messageUser =
                if (message.isUser()) profiles[message.fromId]
                else null

            val messageGroup =
                if (message.isGroup()) groups[message.fromId]
                else null

            message.action ?: return

            binding.message.text = VkUtils.getActionMessageText(
                message = message,
                youPrefix = youPrefix,
                profiles = profiles,
                groups = groups,
                messageUser = messageUser,
                messageGroup = messageGroup
            )

            val attachments = message.attachments ?: return
            attachments[0].let { attachment ->
                if (attachment !is VkPhoto) return@let

                binding.photo.isVisible = true

                val size = attachment.sizeOfType('m') ?: return@let

                binding.photo.layoutParams = LinearLayoutCompat.LayoutParams(
                    size.width,
                    size.height
                )

                binding.photo.load(size.url) {
                    crossfade(150)
                    fallback(ColorDrawable(Color.LTGRAY))
                }
            }
        }
    }

    inner class AttachmentPhotosIncoming(
        private val binding: ItemMessageAttachmentPhotosInBinding
    ) : Holder(binding.root) {

        override fun bind(position: Int) {
            val message = getItem(position)

            MessagesManager.loadPhotos(
                context = context,
                message = message,
                binding.photosContainer
            )
        }
    }

    inner class AttachmentPhotosOutgoing(
        private val binding: ItemMessageAttachmentPhotosOutBinding
    ) : Holder(binding.root) {

        override fun bind(position: Int) {
            val message = getItem(position)

            MessagesManager.loadPhotos(
                context = context,
                message = message,
                photosContainer = binding.photosContainer
            )
        }
    }

    inner class AttachmentStickerOutgoing(
        private val binding: ItemMessageAttachmentStickerOutBinding
    ) : Holder(binding.root) {

        override fun bind(position: Int) {
            val message = getItem(position)
            val prevMessage = getOrNull(position - 1)
            val nextMessage = getOrNull(position + 1)

            if (!message.isPeerChat()) {
                binding.spacer.isVisible =
                    !(prevMessage != null && prevMessage.fromId == message.fromId && message.date - prevMessage.date < 60)
            } else {
                binding.spacer.isVisible =
                    if (prevMessage == null || prevMessage.fromId != message.fromId) message.isPeerChat()
                    else message.date - prevMessage.date >= 60
            }

            val sticker = message.attachments?.get(0) as? VkSticker ?: return
            val url = sticker.urlForSize(352)!!

            binding.photo.layoutParams.also {
                it.width = 352
                it.height = 352
            }

            binding.photo.load(url) { crossfade(150) }
        }
    }

    inner class AttachmentStickerIncoming(
        private val binding: ItemMessageAttachmentStickerInBinding
    ) : Holder(binding.root) {

        override fun bind(position: Int) {
            val message = getItem(position)
            val prevMessage = getOrNull(position - 1)
            val nextMessage = getOrNull(position + 1)

            if (!message.isPeerChat()) {
                binding.avatar.isVisible = false

                binding.spacer.isVisible =
                    !(prevMessage != null && prevMessage.fromId == message.fromId && message.date - prevMessage.date < 60)
            } else {
                binding.spacer.isVisible =
                    if (prevMessage == null || prevMessage.fromId != message.fromId) message.isPeerChat()
                    else message.date - prevMessage.date >= 60

                binding.avatar.visibility =
                    if (nextMessage == null || nextMessage.fromId != message.fromId) if (message.isPeerChat()) View.VISIBLE else View.GONE
                    else if (nextMessage.date - message.date >= 60) View.VISIBLE
                    else View.INVISIBLE
            }

            val messageUser: VkUser? = if (message.isUser()) {
                profiles[message.fromId]
            } else null

            val messageGroup: VkGroup? = if (message.isGroup()) {
                groups[message.fromId]
            } else null

            val avatar = when {
                message.isUser() && messageUser != null && !messageUser.photo200.isNullOrBlank() -> messageUser.photo200
                message.isGroup() && messageGroup != null && !messageGroup.photo200.isNullOrBlank() -> messageGroup.photo200
                else -> null
            }

            binding.avatar.load(avatar) { crossfade(100) }

            val title = when {
                message.isUser() && messageUser != null -> messageUser.fullName
                message.isGroup() && messageGroup != null -> messageGroup.name
                else -> null
            }

            binding.avatar.setOnLongClickListener {
                Toast.makeText(context, title, Toast.LENGTH_SHORT).apply {
                    setGravity(
                        Gravity.START or Gravity.BOTTOM,
                        0,
                        -50
                    )
                }.show()
                true
            }

            val sticker = message.attachments?.get(0) as? VkSticker ?: return
            val url = sticker.urlForSize(352)!!

            binding.photo.layoutParams.also {
                it.width = 352
                it.height = 352
            }

            binding.photo.load(url) { crossfade(150) }
        }
    }

    private val actualSize get() = values.size

    override fun getItemCount(): Int {
        if (actualSize == 0) return 2
        return super.getItemCount() + 2
    }

    companion object {
        private const val SERVICE = 1
        private const val HEADER = 0
        private const val FOOTER = 2
        private const val INCOMING = 3
        private const val OUTGOING = 4


        private const val ATTACHMENT_PHOTOS_IN = 101
        private const val ATTACHMENT_PHOTOS_OUT = 102
        private const val ATTACHMENT_STICKER_IN = 111
        private const val ATTACHMENT_STICKER_OUT = 112

        private val COMPARATOR = object : DiffUtil.ItemCallback<VkMessage>() {
            override fun areItemsTheSame(
                oldItem: VkMessage,
                newItem: VkMessage
            ) = false

            override fun areContentsTheSame(
                oldItem: VkMessage,
                newItem: VkMessage
            ) = false
        }
    }
}