package com.meloda.fast.screens.messages

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.util.ObjectsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import com.meloda.fast.R
import com.meloda.fast.api.VkUtils
import com.meloda.fast.api.model.VkConversation
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.model.attachments.VkPhoto
import com.meloda.fast.base.adapter.BaseAdapter
import com.meloda.fast.base.adapter.BaseHolder
import com.meloda.fast.databinding.ItemMessageInBinding
import com.meloda.fast.databinding.ItemMessageOutBinding
import com.meloda.fast.databinding.ItemMessageServiceBinding
import com.meloda.fast.extensions.ImageLoader.loadWithGlide
import com.meloda.fast.extensions.dpToPx

class MessagesHistoryAdapter constructor(
    context: Context,
    val conversation: VkConversation,
    val profiles: HashMap<Int, VkUser> = hashMapOf(),
    val groups: HashMap<Int, VkGroup> = hashMapOf(),
) : BaseAdapter<VkMessage, MessagesHistoryAdapter.BasicHolder>(
    context,
    Comparator
) {

    constructor(
        fragment: MessagesHistoryFragment,
        conversation: VkConversation,
        profiles: HashMap<Int, VkUser> = hashMapOf(),
        groups: HashMap<Int, VkGroup> = hashMapOf(),
    ) : this(fragment.requireContext(), conversation, profiles, groups) {
        this.messagesHistoryFragment = fragment
    }

    constructor(
        fragment: ForwardedMessagesFragment,
        conversation: VkConversation,
        profiles: HashMap<Int, VkUser> = hashMapOf(),
        groups: HashMap<Int, VkGroup> = hashMapOf()
    ) : this(fragment.requireContext(), conversation, profiles, groups) {
        this.isForwards = true
        this.forwardedMessagesFragment = fragment
    }

    private var isForwards: Boolean = false

    private var messagesHistoryFragment: MessagesHistoryFragment? = null
    private var forwardedMessagesFragment: ForwardedMessagesFragment? = null

    var avatarLongClickListener: ((position: Int) -> Unit)? = null

    override fun getItemViewType(position: Int): Int {
        return when (val item = getItem(position)) {
            is VkMessage -> {
                return when {
                    item.action != null -> TypeService
                    item.isOut -> TypeOutgoing
                    !item.isOut -> TypeIncoming
                    else -> -1
                }
            }
            else -> -1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BasicHolder {
        return when (viewType) {
            TypeService -> ServiceMessage(
                ItemMessageServiceBinding.inflate(inflater, parent, false)
            )
            TypeOutgoing -> OutgoingMessage(
                ItemMessageOutBinding.inflate(inflater, parent, false)
            )
            TypeIncoming -> IncomingMessage(
                ItemMessageInBinding.inflate(inflater, parent, false)
            )
            else -> throw IllegalStateException("Wrong viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: BasicHolder, position: Int) {
        if (holder is Header || holder is Footer) {
            Log.d(
                "MessagesHistoryAdapter",
                "onBindViewHolder: index $position, holder: ${holder.javaClass.simpleName}. Skip"
            )
            return
        }

        Log.d(
            "MessagesHistoryAdapter",
            "onBindViewHolder: index $position, holder: ${holder.javaClass.simpleName}. Bind"
        )

        initListeners(holder.itemView, position)
        holder.bind(position)
    }

    open inner class BasicHolder(v: View = View(context)) : BaseHolder(v)

    inner class Header(v: View) : BasicHolder(v)

    inner class Footer(v: View) : BasicHolder(v)

    inner class IncomingMessage(
        private val binding: ItemMessageInBinding
    ) : BasicHolder(binding.root) {

        override fun bind(position: Int, payloads: MutableList<Any>?) {
            val message = getItem(position) as VkMessage

            val prevMessage = getOrNull(position - 1)
            val nextMessage = getOrNull(position + 1)

            MessagesPreparator(
                context = context,
                payloads = payloads,

                root = binding.root,

                conversation = conversation,
                message = message,
                prevMessage = prevMessage,
                nextMessage = nextMessage,

                title = binding.title,

                avatar = binding.avatar,
                bubble = binding.bubble,
                text = binding.text,
                spacer = binding.spacer,
                messageState = binding.messageState,
                time = binding.time,

                replyContainer = binding.replyContainer,
                attachmentContainer = binding.attachmentContainer,
                timeReadContainer = binding.timeReadContainer,

                profiles = profiles,
                groups = groups,

                isForwards = isForwards
            )
                .withPhotoClickListener {
                    Intent(Intent.ACTION_VIEW, Uri.parse(it)).run {
                        context.startActivity(this)
                    }
                }
                .withReplyClickListener {
                    messagesHistoryFragment?.scrollToMessage(it.id)
                    forwardedMessagesFragment?.scrollToMessage(it.id)
                }
                .withForwardsClickListener { messages ->
                    messagesHistoryFragment?.openForwardsScreen(
                        conversation, messages, profiles, groups
                    )
                    forwardedMessagesFragment?.openForwardsScreen(
                        conversation, messages, profiles, groups
                    )
                }
                .prepare()

            binding.avatar.setOnLongClickListener {
                avatarLongClickListener?.invoke(position)
                true
            }
        }
    }

    inner class OutgoingMessage(
        private val binding: ItemMessageOutBinding
    ) : BasicHolder(binding.root) {

        override fun bind(position: Int, payloads: MutableList<Any>?) {
            val message = getItem(position)
            val prevMessage = getOrNull(position - 1)

            MessagesPreparator(
                context = context,
                payloads = payloads,
                root = binding.root,
                conversation = conversation,
                message = message,
                prevMessage = prevMessage,

                bubble = binding.bubble,
                text = binding.text,
                spacer = binding.spacer,
                messageState = binding.messageState,
                time = binding.time,

                timeReadContainer = binding.timeReadContainer,
                replyContainer = binding.replyContainer,
                attachmentContainer = binding.attachmentContainer,

                profiles = profiles,
                groups = groups,

                isForwards = isForwards
            )
                .withPhotoClickListener {
                    Intent(Intent.ACTION_VIEW, Uri.parse(it)).run {
                        context.startActivity(this)
                    }
                }
                .withReplyClickListener {
                    messagesHistoryFragment?.scrollToMessage(it.id)
                    forwardedMessagesFragment?.scrollToMessage(it.id)
                }
                .withForwardsClickListener { messages ->
                    messagesHistoryFragment?.openForwardsScreen(
                        conversation, messages, profiles, groups
                    )
                    forwardedMessagesFragment?.openForwardsScreen(
                        conversation, messages, profiles, groups
                    )
                }
                .prepare()
        }
    }

    inner class ServiceMessage(
        private val binding: ItemMessageServiceBinding
    ) : BasicHolder(binding.root) {

        private val youPrefix = context.getString(R.string.you_message_prefix)

        init {
            binding.photo.shapeAppearanceModel =
                binding.photo.shapeAppearanceModel.withCornerSize(4.dpToPx().toFloat())
        }

        override fun bind(position: Int) {
            val message = getItem(position) as VkMessage

            val messageUser =
                if (message.isUser()) profiles[message.fromId]
                else null

            val messageGroup =
                if (message.isGroup()) groups[message.fromId]
                else null

            message.action ?: return

            binding.message.text = VkUtils.getActionMessageText(
                context = context,
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

                val size = attachment.getSizeOrSmaller('y') ?: return@let

                binding.photo.layoutParams = LinearLayoutCompat.LayoutParams(
                    size.width,
                    size.height
                )

                binding.photo.loadWithGlide(
                    url = size.url,
                    crossFade = true,
                    placeholderDrawable = ColorDrawable(Color.LTGRAY)
                )

                binding.photo.setOnClickListener {
                    Intent(Intent.ACTION_VIEW, Uri.parse(size.url)).run {
                        context.startActivity(this)
                    }
                }
            }
        }
    }

    fun containsUnreadMessages(isOutgoingMessages: Boolean = false): Boolean {
        for (i in indices) {
            val item = getItem(i)
            if (item !is VkMessage) continue

            if (item.isOut == isOutgoingMessages && !item.isRead(conversation)) {
                return true
            }
        }
        return false
    }

    fun containsRandomId(randomId: Int): Boolean {
        if (randomId == 0) return false
        for (i in indices) {
            val item = getItem(i)
            if (item !is VkMessage) continue

            if (item.randomId == randomId) return true
        }

        return false
    }

    fun containsId(id: Int): Boolean {
        for (i in indices) {
            val item = getItem(i)
            if (item !is VkMessage) continue

            if (item.id == id) return true
        }

        return false
    }

    fun searchMessageIndex(messageId: Int): Int? {
        for (i in indices) {
            val message = getItem(i)
            if (message is VkMessage && message.id == messageId) return i
        }

        return null
    }

    fun searchMessageById(messageId: Int): VkMessage? {
        for (i in indices) {
            val message = getItem(i)
            if (message is VkMessage && message.id == messageId) return message
        }

        return null
    }

    companion object {
        private const val TypeService = 1
        private const val TypeHeader = 0
        private const val TypeFooter = 2
        private const val TypeIncoming = 3
        private const val TypeOutgoing = 4

        private val Comparator = object : DiffUtil.ItemCallback<VkMessage>() {
            override fun areItemsTheSame(
                oldItem: VkMessage,
                newItem: VkMessage
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: VkMessage,
                newItem: VkMessage
            ): Boolean {
                return ObjectsCompat.equals(oldItem, newItem) && (oldItem.state == newItem.state)
            }
        }
    }
}