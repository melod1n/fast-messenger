package com.meloda.fast.screens.messages

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.LinearLayoutCompat
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
import com.meloda.fast.base.adapter.BaseAdapter
import com.meloda.fast.base.adapter.BaseHolder
import com.meloda.fast.common.AppGlobal
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
        var viewType: Int = when {
            isPositionHeader(position) -> HEADER
            isPositionFooter(position) -> FOOTER
            else -> -1
        }

        if (viewType == -1) {
            getItem(position).let {
                if (it.action != null) viewType = SERVICE

                val attachments = it.attachments ?: return@let
                if (attachments.isEmpty()) return@let
                if (VkUtils.isAttachmentsHaveOneType(attachments) &&
                    attachments[0] is VkPhoto
                ) {
                    return if (it.isOut) ATTACHMENT_PHOTOS_OUT else ATTACHMENT_PHOTOS_IN
                }

                if (it.isOut) viewType = OUTGOING
                if (!it.isOut) viewType = INCOMING
            }
        }

        return viewType
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
            ATTACHMENT_PHOTOS_IN -> AttachmentPhotosIncoming(
                ItemMessageAttachmentPhotoInBinding.inflate(inflater, parent, false)
            )
            ATTACHMENT_PHOTOS_OUT -> AttachmentPhotosOutgoing(
                ItemMessageAttachmentPhotoOutBinding.inflate(inflater, parent, false)
            )
            OUTGOING -> OutgoingMessage(
                ItemMessageOutBinding.inflate(inflater, parent, false)
            )
            INCOMING -> IncomingMessage(
                ItemMessageInBinding.inflate(inflater, parent, false)
            )
            else -> Holder()
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

    inner class AttachmentPhotosIncoming(
        private val binding: ItemMessageAttachmentPhotoInBinding
    ) : Holder(binding.root) {

        init {
            binding.photo.shapeAppearanceModel = binding.photo.shapeAppearanceModel.withCornerSize {
                AndroidUtils.px(12)
            }
        }

        override fun bind(position: Int) {
            val message = getItem(position)

            val photo = message.attachments?.get(0) as? VkPhoto ?: return

            val size = photo.sizeOfType('m') ?: return

            binding.photo.layoutParams = FrameLayout.LayoutParams(
                AndroidUtils.px(size.width).roundToInt(),
                AndroidUtils.px(size.height).roundToInt()
            )

            binding.photo.load(size.url)
        }

    }

    inner class AttachmentPhotosOutgoing(
        private val binding: ItemMessageAttachmentPhotoOutBinding
    ) : Holder(binding.root) {

        init {
            binding.photo.shapeAppearanceModel = binding.photo.shapeAppearanceModel.withCornerSize {
                AndroidUtils.px(12)
            }
        }

        override fun bind(position: Int) {
            val message = getItem(position)

            val photo = message.attachments?.get(0) as? VkPhoto ?: return

            val size = photo.sizeOfType('m') ?: return

            binding.photo.layoutParams = LinearLayoutCompat.LayoutParams(
                AndroidUtils.px(size.width).roundToInt(),
                AndroidUtils.px(size.height).roundToInt()
            )

            binding.photo.load(size.url)
        }

    }

    inner class ServiceMessage(
        private val binding: ItemMessageServiceBinding
    ) : Holder(binding.root) {

        private val youPrefix = context.getString(R.string.you_message_prefix)

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
        }
    }

    inner class OutgoingMessage(
        private val binding: ItemMessageOutBinding
    ) : Holder(binding.root) {

        init {
            binding.bubble.maxWidth = (AppGlobal.screenWidth * 0.75).roundToInt()
        }

        override fun bind(position: Int) {
            val message = getItem(position)

            binding.text.text = message.text ?: "[no_message]"

            binding.unread.isVisible = message.isRead(conversation)
        }

    }

    inner class IncomingMessage(
        private val binding: ItemMessageInBinding
    ) : Holder(binding.root) {

        init {
            binding.bubble.maxWidth = (AppGlobal.screenWidth * 0.7).roundToInt()
        }

        override fun bind(position: Int) {
            val message = getItem(position)

            val prevMessage = getOrNull(position - 1)
            val nextMessage = getOrNull(position + 1)

            binding.title.isVisible =
                if (prevMessage == null || prevMessage.fromId != message.fromId) message.isPeerChat()
                else message.date - prevMessage.date >= 60

            binding.avatar.visibility =
                if (nextMessage == null || nextMessage.fromId != message.fromId) if (message.isPeerChat()) View.VISIBLE else View.GONE
                else if (nextMessage.date - message.date >= 60) View.VISIBLE
                else View.INVISIBLE

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

            val title = when {
                message.isUser() && messageUser != null -> messageUser.firstName
                message.isGroup() && messageGroup != null -> messageGroup.name
                else -> null
            }

            binding.avatar.load(avatar) { crossfade(100) }

            binding.text.text = message.text ?: "[no_message]"

            binding.title.text = title
            binding.title.measure(0, 0)

            if (binding.title.isVisible) {
                binding.bubble.minimumWidth = binding.title.measuredWidth + 60
            } else {
                binding.bubble.minimumWidth = 0
            }
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
        private const val ATTACHMENT_PHOTOS_OUT = 1011

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