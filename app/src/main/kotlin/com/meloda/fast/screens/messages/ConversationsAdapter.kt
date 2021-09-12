package com.meloda.fast.screens.messages

import android.content.Context
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import coil.load
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VkUtils
import com.meloda.fast.api.model.VkConversation
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.base.adapter.BaseAdapter
import com.meloda.fast.base.adapter.BindingHolder
import com.meloda.fast.databinding.ItemConversationBinding
import java.text.SimpleDateFormat

class ConversationsAdapter constructor(
    context: Context,
    values: MutableList<VkConversation>,
    val profiles: HashMap<Int, VkUser> = hashMapOf(),
    val groups: HashMap<Int, VkGroup> = hashMapOf()
) : BaseAdapter<VkConversation, ConversationsAdapter.ItemHolder>(
    context, values, COMPARATOR
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ItemHolder(ItemConversationBinding.inflate(inflater, parent, false))

    inner class ItemHolder(binding: ItemConversationBinding) :
        BindingHolder<ItemConversationBinding>(binding) {

        private val dateColor = ContextCompat.getColor(context, R.color.n2_500)
        private val youPrefix = context.getString(R.string.you_message_prefix)

        override fun bind(position: Int) {
            val conversation = getItem(position)

            binding.service.isVisible = conversation.isPhantom || conversation.callInProgress
            binding.callIcon.isVisible = conversation.callInProgress
            binding.phantomIcon.isVisible = conversation.isPhantom

            val message = if (conversation.lastMessage != null) conversation.lastMessage!!
            else {
                binding.title.text = conversation.title
                val text = context.getString(
                    if (conversation.isPhantom) R.string.messages_self_destructed
                    else R.string.no_messages
                )

                val span = SpannableString(text)
                span.setSpan(ForegroundColorSpan(dateColor), 0, text.length, 0)

                binding.message.text = span
                return
            }

            val chatUser: VkUser? = if (conversation.isUser()) {
                profiles[conversation.id]
            } else null

            val messageUser: VkUser? = if (message.isUser()) {
                profiles[message.fromId]
            } else null

            val chatGroup: VkGroup? = if (conversation.isGroup()) {
                groups[conversation.id]
            } else null

            val messageGroup: VkGroup? = if (message.isGroup()) {
                groups[message.fromId]
            } else null

            val avatar = when {
                chatUser != null && !chatUser.photo200.isNullOrBlank() -> chatUser.photo200
                chatGroup != null && !chatGroup.photo200.isNullOrBlank() -> chatGroup.photo200
                !conversation.photo200.isNullOrBlank() -> conversation.photo200
                else -> null
            }

            binding.avatar.isVisible = avatar != null
            binding.avatarPlaceholder.isVisible = avatar == null

            if (avatar == null) {
                binding.avatar.setImageDrawable(null)
            } else {
                binding.avatar.load(avatar) { crossfade(200) }
            }

            binding.online.isVisible = chatUser?.online == true

            val actionMessage = VkUtils.getActionConversationText(
                message = message,
                youPrefix = youPrefix,
                profiles = profiles,
                groups = groups,
                messageUser = messageUser,
                messageGroup = messageGroup
            )

            val attachmentIcon =
                if (message.text == null) null
                else if (!message.forwards.isNullOrEmpty()) ContextCompat.getDrawable(
                    context,
                    if (message.forwards?.size == 1) R.drawable.ic_attachment_forwarded_message
                    else R.drawable.ic_attachment_forwarded_messages
                )
                else VkUtils.getAttachmentConversationIcon(
                    context = context,
                    message = message
                )

            binding.textAttachment.isVisible = attachmentIcon != null
            binding.textAttachment.setImageDrawable(attachmentIcon)

            val attachmentText = if (attachmentIcon == null) VkUtils.getAttachmentConversationText(
                context = context,
                message = message
            ) else null

            val forwardsMessage = if (message.text == null) VkUtils.getForwardsConversationText(
                context = context,
                message = message
            ) else null

            val messageText = if (actionMessage != null ||
                forwardsMessage != null ||
                attachmentText != null
            ) ""
            else message.text ?: "[no_message]"

            val coloredMessage = actionMessage ?: attachmentText ?: forwardsMessage ?: ""

            var prefix = when {
                actionMessage != null -> ""
                message.isOut -> "$youPrefix: "
                else -> {
                    if (message.isUser() && messageUser != null && messageUser.firstName.isNotBlank()) "${messageUser.firstName}: "
                    else if (message.isGroup() && messageGroup != null && messageGroup.name.isNotBlank()) "${messageGroup.name}: "
                    else ""
                }
            }

            if ((!conversation.isChat() && !message.isOut) || conversation.id == UserConfig.userId)
                prefix = ""

//            if (conversation.isChat() || message.isOut) {
            val spanText = "$prefix$coloredMessage$messageText"

            val spanMessage = SpannableString(spanText)
            spanMessage.setSpan(
                ForegroundColorSpan(dateColor), 0,
                prefix.length + coloredMessage.length,
                0
            )
            binding.message.text = spanMessage
//            } else {
//                binding.message.text = messageText
//            }

            binding.title.text =
                getItem(position).title ?: chatUser?.toString() ?: chatGroup?.name ?: "..."

            binding.date.text = SimpleDateFormat("HH:mm").format(message.date * 1000)

            binding.container.background = if (conversation.isUnread()) ContextCompat.getDrawable(
                context,
                R.drawable.ic_message_unread
            ) else null


            binding.counter.isVisible = conversation.isInUnread()
            if (conversation.isInUnread()) {
                conversation.unreadCount?.let {
                    val count = if (it > 999) "${it / 1000}K" else it.toString()
                    binding.counter.text = count
                }
            } else {
                binding.counter.text = ""
            }
        }
    }

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<VkConversation>() {
            override fun areItemsTheSame(
                oldItem: VkConversation,
                newItem: VkConversation
            ) = false

            override fun areContentsTheSame(
                oldItem: VkConversation,
                newItem: VkConversation
            ) = false
        }
    }

}