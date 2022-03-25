package com.meloda.fast.screens.conversations

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.util.ObjectsCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.recyclerview.widget.DiffUtil
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.VkUtils
import com.meloda.fast.api.model.VkConversation
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.base.adapter.BaseAdapter
import com.meloda.fast.base.adapter.BindingHolder
import com.meloda.fast.databinding.ItemConversationBinding
import com.meloda.fast.extensions.ImageLoader
import com.meloda.fast.extensions.ImageLoader.clear
import com.meloda.fast.extensions.ImageLoader.loadWithGlide
import com.meloda.fast.extensions.gone
import com.meloda.fast.extensions.toggleVisibility
import com.meloda.fast.extensions.visible
import com.meloda.fast.util.TimeUtils

class ConversationsAdapter constructor(
    context: Context,
    private val resourceManager: ConversationsResourceManager,
    var isMultilineEnabled: Boolean = true,
    val profiles: HashMap<Int, VkUser> = hashMapOf(),
    val groups: HashMap<Int, VkGroup> = hashMapOf(),
) : BaseAdapter<VkConversation, ConversationsAdapter.ItemHolder>(context, Comparator) {

    var pinnedCount = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(
            ItemConversationBinding.inflate(inflater, parent, false),
            resourceManager
        )
    }

    inner class ItemHolder(
        binding: ItemConversationBinding,
        private val resourceManager: ConversationsResourceManager
    ) : BindingHolder<ItemConversationBinding>(binding) {

        init {
            binding.title.ellipsize = TextUtils.TruncateAt.END
            binding.message.ellipsize = TextUtils.TruncateAt.END
        }

        override fun bind(position: Int) {
            val conversation = getItem(position)

            binding.service.isVisible = conversation.isPhantom || conversation.callInProgress
            binding.callIcon.isVisible = conversation.callInProgress
            binding.phantomIcon.isVisible = conversation.isPhantom

            val maxLines = if (isMultilineEnabled) 2 else 1

            binding.title.maxLines = maxLines
            binding.message.maxLines = maxLines

            val message = if (conversation.lastMessage != null) conversation.lastMessage!!
            else {
                binding.title.text = conversation.title
                val text = context.getString(
                    if (conversation.isPhantom) R.string.messages_self_destructed
                    else R.string.no_messages
                )

                val span = SpannableString(text)
                span.setSpan(ForegroundColorSpan(resourceManager.colorOutline), 0, text.length, 0)

                binding.message.text = span
                return
            }

            val conversationUser = VkUtils.getConversationUser(conversation, profiles)
            val conversationGroup = VkUtils.getConversationGroup(conversation, groups)

            val messageUser = VkUtils.getMessageUser(message, profiles)
            val messageGroup = VkUtils.getMessageGroup(message, groups)

            val avatar = VkUtils.getConversationAvatar(
                conversation = conversation,
                conversationUser = conversationUser,
                conversationGroup = conversationGroup
            )

            binding.avatar.toggleVisibility(avatar != null)

            if (avatar == null) {
                binding.avatarPlaceholder.visible()

                if (conversation.ownerId == VKConstants.FAST_GROUP_ID) {
                    binding.placeholderBack.loadWithGlide(
                        drawable = ColorDrawable(resourceManager.icLauncherColor),
                        transformations = ImageLoader.userAvatarTransformations
                    )
                    binding.placeholder.imageTintList =
                        ColorStateList.valueOf(resourceManager.colorOnPrimary)
                    binding.placeholder.setImageResource(R.drawable.ic_fast_logo)
                    binding.placeholder.setPadding(18)
                } else {
                    binding.placeholderBack.loadWithGlide(
                        drawable = ColorDrawable(resourceManager.colorOnUserAvatarAction),
                        transformations = ImageLoader.userAvatarTransformations
                    )
                    binding.placeholder.imageTintList =
                        ColorStateList.valueOf(resourceManager.colorUserAvatarAction)
                    binding.placeholder.setImageResource(R.drawable.ic_account_circle_cut)
                    binding.placeholder.setPadding(0)
                    binding.avatar.clear()
                }
            } else {
                binding.avatar.loadWithGlide(
                    url = avatar,
                    crossFade = true,
                    onLoadedAction = { binding.avatarPlaceholder.gone() }
                )
            }

            binding.online.toggleVisibility(conversationUser?.online == true)
            binding.pin.toggleVisibility(conversation.isPinned)

            val actionMessage = VkUtils.getActionConversationText(
                context = context,
                message = message,
                youPrefix = resourceManager.youPrefix,
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

            binding.textAttachment.toggleVisibility(attachmentIcon != null)
            binding.textAttachment.setImageDrawable(attachmentIcon)

            val attachmentText = if (attachmentIcon == null) VkUtils.getAttachmentText(
                context = context,
                message = message
            ) else null

            val forwardsMessage = if (message.text == null) VkUtils.getForwardsText(
                context = context,
                message = message
            ) else null

            val messageText = (if (
                actionMessage != null ||
                forwardsMessage != null ||
                attachmentText != null
            ) ""
            else message.text ?: "").run { VkUtils.prepareMessageText(this) }

            val coloredMessage = actionMessage ?: attachmentText ?: forwardsMessage ?: ""

            var prefix = when {
                actionMessage != null -> ""
                message.isOut -> "${resourceManager.youPrefix}: "
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
                ForegroundColorSpan(resourceManager.colorOutline), 0,
                prefix.length + coloredMessage.length,
                0
            )

            binding.message.text = spanMessage

            binding.title.text =
                getItem(position).title ?: conversationUser?.toString() ?: conversationGroup?.name
                        ?: "..."

            binding.date.text = TimeUtils.getLocalizedTime(context, message.date * 1000L)

            binding.container.background = if (conversation.isUnread()) ContextCompat.getDrawable(
                context,
                R.drawable.ic_message_unread
            ) else null

            binding.onlineBorder.setImageDrawable(
                ColorDrawable(
                    ContextCompat.getColor(
                        context,
                        if (conversation.isUnread()) R.color.colorBackgroundVariant
                        else R.color.colorBackground
                    )
                )
            )

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

    fun removeConversation(conversationId: Int): Int? {
        for (i in indices) {
            val conversation = getItem(i)
            if (conversation.id == conversationId) {
                removeAt(i)
                return i
            }
        }

        return null
    }

    fun searchConversationIndex(conversationId: Int): Int? {
        for (i in indices) {
            val conversation = getItem(i)

            if (conversation.id == conversationId) return i
        }

        return null
    }

    companion object {
        private val Comparator = object : DiffUtil.ItemCallback<VkConversation>() {
            override fun areItemsTheSame(
                oldItem: VkConversation,
                newItem: VkConversation
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: VkConversation,
                newItem: VkConversation
            ): Boolean {
                return ObjectsCompat.equals(oldItem, newItem)
            }
        }
    }

}