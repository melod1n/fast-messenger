package com.meloda.fast.screens.messages

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import coil.load
import com.meloda.fast.api.model.VkConversation
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.base.adapter.BaseAdapter
import com.meloda.fast.base.adapter.BaseHolder
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.databinding.ItemMessageInBinding
import com.meloda.fast.databinding.ItemMessageOutBinding
import com.meloda.fast.databinding.ItemMessageServiceBinding
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
            SERVICE -> ServiceMessage(ItemMessageServiceBinding.inflate(inflater, parent, false))
            OUTGOING -> OutgoingMessage(ItemMessageOutBinding.inflate(inflater, parent, false))
            INCOMING -> IncomingMessage(ItemMessageInBinding.inflate(inflater, parent, false))
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

    inner class ServiceMessage(
        private val binding: ItemMessageServiceBinding
    ) : Holder(binding.root) {

        override fun bind(position: Int) {

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
        private const val INCOMING = 1001
        private const val OUTGOING = 1002
        private const val SERVICE = 1003
        private const val HEADER = 0
        private const val FOOTER = 2

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