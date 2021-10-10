package com.meloda.fast.screens.messages

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import com.meloda.fast.databinding.*
import com.meloda.fast.util.AndroidUtils
import java.util.*
import kotlin.math.roundToInt

class MessagesHistoryAdapter constructor(
    context: Context,
    values: MutableList<VkMessage>,
    val conversation: VkConversation,
    val profiles: HashMap<Int, VkUser> = hashMapOf(),
    val groups: HashMap<Int, VkGroup> = hashMapOf()
) : BaseAdapter<VkMessage, MessagesHistoryAdapter.BasicHolder>(context, values, COMPARATOR) {

    var avatarLongClickListener: ((position: Int) -> Unit)? = null

    override fun getItemViewType(position: Int): Int {
        when {
            isPositionHeader(position) -> return HEADER
            isPositionFooter(position) -> return FOOTER
        }

        getItem(position).let { message ->
            if (message.action != null) return SERVICE
            if (message.isOut) return OUTGOING
            if (!message.isOut) return INCOMING
        }

        return -1
    }

    private fun isPositionHeader(position: Int) = position == 0
    private fun isPositionFooter(position: Int) = position >= actualSize

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BasicHolder {
        return when (viewType) {
            // magick numbers is great!
            HEADER -> Header(createEmptyView(60))
            FOOTER -> Footer(createEmptyView(36))
            SERVICE -> ServiceMessage(
                ItemMessageServiceBinding.inflate(inflater, parent, false)
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

//    override fun initListeners(itemView: View, position: Int) {
//        if (itemView is AdapterView<*>) return
//
//        itemView.setOnClickListener { onItemClickListener?.invoke(position, itemView) }
//        itemView.setOnLongClickListener { itemLongClickListener.invoke(position) }
//    }


    val actualSize get() = values.size

    override fun getItemCount(): Int {
        if (actualSize == 0) return 2
        return super.getItemCount() + 2
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

    override fun onBindViewHolder(holder: BasicHolder, position: Int) {
        if (holder is Header || holder is Footer) return

        initListeners(holder.itemView, position)
        holder.bind(position)
    }

    open inner class BasicHolder(v: View = View(context)) : BaseHolder(v)

    inner class Header(v: View) : BasicHolder(v)

    inner class Footer(v: View) : BasicHolder(v)

    inner class IncomingMessage(
        private val binding: ItemMessageInBinding
    ) : BasicHolder(binding.root) {

        override fun bind(position: Int) {
            val message = getItem(position)

            val prevMessage = getOrNull(position - 1)
            val nextMessage = getOrNull(position + 1)

            MessagesPreparator(
                context = context,

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
                unread = binding.unread,

                textContainer = binding.textContainer,
                attachmentContainer = binding.attachmentContainer,
                attachmentSpacer = binding.attachmentSpacer,

                profiles = profiles,
                groups = groups
            ).setPhotoClickListener {
                Toast.makeText(context, "Photo url: $it", Toast.LENGTH_LONG).show()
            }.prepare()

            binding.avatar.setOnLongClickListener() {
                avatarLongClickListener?.invoke(position)
                true
            }
        }
    }

    inner class OutgoingMessage(
        private val binding: ItemMessageOutBinding
    ) : BasicHolder(binding.root) {

        override fun bind(position: Int) {
            val message = getItem(position)

            val prevMessage = getOrNull(position - 1)

            MessagesPreparator(
                context = context,
                root = binding.root,
                conversation = conversation,
                message = message,
                prevMessage = prevMessage,

                bubble = binding.bubble,
                text = binding.text,
                spacer = binding.spacer,
                unread = binding.unread,

                textContainer = binding.textContainer,
                attachmentContainer = binding.attachmentContainer,
                attachmentSpacer = binding.attachmentSpacer,

                profiles = profiles,
                groups = groups
            ).prepare()
        }
    }

    inner class ServiceMessage(
        private val binding: ItemMessageServiceBinding
    ) : BasicHolder(binding.root) {

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

                binding.photo.load(size.url) {
                    crossfade(150)
                    fallback(ColorDrawable(Color.LTGRAY))
                }
            }
        }
    }

    fun removeMessageById(id: Int): Int? {
        for (i in values.indices) {
            val message = values[i]
            if (message.id == id) {
                values.removeAt(i)
                return i
            }
        }

        return null
    }

    fun removeMessagesByIds(ids: List<Int>): List<Int> {
        val positions = mutableListOf<Int>()

        for (i in values.indices) {
            val message = values[i]
            if (ids.contains(message.id)) {
                values.removeAt(i)
                positions += i
            }
        }

        return positions
    }

    companion object {
        private const val SERVICE = 1
        private const val HEADER = 0
        private const val FOOTER = 2
        private const val INCOMING = 3
        private const val OUTGOING = 4


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