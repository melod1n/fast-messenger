package com.meloda.fast.screens.messages

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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

        private val dateColor = ContextCompat.getColor(context, R.color.date)
        private val youPrefix = context.getString(R.string.you_message_prefix)

        override fun bind(position: Int) {
            val conversation = getItem(position)
            val message = conversation.lastMessage ?: return

            val chatUser: VkUser? = if (conversation.isUser()) {
                profiles[conversation.id]
//                profiles.find { it.id == conversation.id }
            } else null

            val messageUser: VkUser? = if (message.isUser()) {
                profiles[message.fromId]
//                profiles.find { it.id == message.fromId }
            } else null

            val chatGroup: VkGroup? = if (conversation.isGroup()) {
                groups[conversation.id]
//                groups.find { it.id == conversation.id }
            } else null

            val messageGroup: VkGroup? = if (message.isGroup()) {
                groups[message.fromId]
//                groups.find { it.id == message.fromId }
            } else null

            val avatar = when {
                chatUser != null && !chatUser.photo200.isNullOrBlank() -> chatUser.photo200
                chatGroup != null && !chatGroup.photo200.isNullOrBlank() -> chatGroup.photo200
                !conversation.photo200.isNullOrBlank() -> conversation.photo200
                else -> null
            }

            if (avatar == null) {
                binding.avatar.setImageDrawable(ColorDrawable(Color.RED))
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

            val attachmentsMessage = VkUtils.getAttachmentConversationText(
                context = context,
                message = message
            )

            val forwardsMessage = VkUtils.getForwardsConversationText(
                context = context,
                message = message
            )

            val messageText = if (actionMessage != null ||
                attachmentsMessage != null ||
                forwardsMessage != null
            ) ""
            else message.text ?: "no_message"

            val coloredMessage = actionMessage ?: attachmentsMessage ?: forwardsMessage ?: ""

            var prefix = when {
                actionMessage != null -> ""
                message.isOut -> "$youPrefix: "
                messageUser != null && messageUser.firstName.isNotBlank() -> "${messageUser.firstName}: "
                messageGroup != null && messageGroup.toString()
                    .isNotBlank() -> "${messageGroup.name}: "
                else -> ""
            }

            if (!conversation.isChat() && !message.isOut || conversation.id == UserConfig.userId) prefix =
                ""

//            if (conversation.isChat() || message.isOut) {
            val spanText = "$prefix$coloredMessage $messageText".trim()

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