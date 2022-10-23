package com.meloda.fast.screens.conversations

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.ViewGroup
import androidx.core.util.ObjectsCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.recyclerview.widget.DiffUtil
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VkUtils
import com.meloda.fast.api.model.VkConversation
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.base.adapter.BaseAdapter
import com.meloda.fast.base.adapter.BaseHolder
import com.meloda.fast.databinding.ItemConversationBinding
import com.meloda.fast.ext.ImageLoader
import com.meloda.fast.ext.ImageLoader.clear
import com.meloda.fast.ext.ImageLoader.loadWithGlide
import com.meloda.fast.ext.gone
import com.meloda.fast.ext.orDots
import com.meloda.fast.ext.toggleVisibility
import com.meloda.fast.ext.visible
import com.meloda.fast.util.TimeUtils

class ConversationsAdapter constructor(
    context: Context,
    private val resourceManager: ConversationsResourceProvider,
    var isMultilineEnabled: Boolean = true,
    val profiles: HashMap<Int, VkUser> = hashMapOf(),
    val groups: HashMap<Int, VkGroup> = hashMapOf(),
) : BaseAdapter<VkConversation, ConversationsAdapter.ItemHolder>(context, comparator) {

    companion object {
        private val comparator = object : DiffUtil.ItemCallback<VkConversation>() {
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

    var pinnedCount = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(
            ItemConversationBinding.inflate(inflater, parent, false),
            resourceManager
        )
    }

    inner class ItemHolder(
        private val binding: ItemConversationBinding,
        private val resourceManager: ConversationsResourceProvider
    ) : BaseHolder(binding.root) {

        override fun bind(position: Int) {
            val conversation = getItem(position)

            if (conversation.isAccount()) {
                binding.service.gone()
                binding.callIcon.gone()
                binding.phantomIcon.gone()
            } else {
                binding.service.toggleVisibility(conversation.isPhantom || conversation.callInProgress)
                binding.callIcon.toggleVisibility(conversation.callInProgress)
                binding.phantomIcon.toggleVisibility(conversation.isPhantom)
            }

            val maxLines = if (isMultilineEnabled) 2 else 1

            binding.title.maxLines = maxLines
            binding.message.maxLines = maxLines

            val message =
                if (conversation.lastMessage != null) requireNotNull(conversation.lastMessage)
                else {
                    binding.title.text = conversation.title
                    val text = context.getString(
                        if (conversation.isPhantom) R.string.messages_self_destructed
                        else R.string.no_messages
                    )

                    val span = SpannableString(text)
                    span.setSpan(
                        ForegroundColorSpan(resourceManager.colorOutline),
                        0,
                        text.length,
                        0
                    )

                    binding.message.text = span
                    return
                }

            val conversationUserGroup =
                VkUtils.getConversationUserGroup(conversation, profiles, groups)
            val messageUserGroup = VkUtils.getMessageUserGroup(message, profiles, groups)

            val conversationUser = conversationUserGroup.first
            val conversationGroup = conversationUserGroup.second

            val messageUser = messageUserGroup.first
            val messageGroup = messageUserGroup.second

            val title = VkUtils.getConversationTitle(
                context = context,
                conversation = conversation,
                defConversationUser = conversationUser,
                defConversationGroup = conversationGroup
            )

            binding.title.text = title.orDots()

            binding.online.toggleVisibility(
                !conversation.isAccount() && conversationUser?.online == true
            )

            binding.pin.toggleVisibility(conversation.isPinned())

            val avatar = VkUtils.getConversationAvatar(
                conversation = conversation,
                conversationUser = conversationUser,
                conversationGroup = conversationGroup
            )

            binding.avatar.toggleVisibility(avatar != null)

            if (avatar == null) {
                binding.avatar.clear()
                binding.avatarPlaceholder.visible()

                if (conversation.isAccount()) {
                    binding.placeholderBack.loadWithGlide(
                        drawable = ColorDrawable(resourceManager.icLauncherColor),
                        transformations = ImageLoader.userAvatarTransformations
                    )
                    binding.placeholder.imageTintList =
                        ColorStateList.valueOf(resourceManager.colorOnPrimary)
                    binding.placeholder.setImageResource(R.drawable.ic_round_bookmark_border_24)
                    binding.placeholder.setPadding(36)
                } else {
                    binding.placeholderBack.loadWithGlide(
                        drawable = ColorDrawable(resourceManager.colorOnUserAvatarAction),
                        transformations = ImageLoader.userAvatarTransformations
                    )
                    binding.placeholder.imageTintList =
                        ColorStateList.valueOf(resourceManager.colorUserAvatarAction)
                    binding.placeholder.setImageResource(R.drawable.ic_account_circle_cut)
                    binding.placeholder.setPadding(0)
                }
            } else {
                binding.avatar.loadWithGlide(
                    url = avatar,
                    crossFade = true,
                    onLoadedAction = { binding.avatarPlaceholder.gone() }
                )
            }

            val actionMessage = VkUtils.getActionConversationText(
                context = context,
                message = message,
                youPrefix = resourceManager.youPrefix,
                profiles = profiles,
                groups = groups,
                messageUser = messageUser,
                messageGroup = messageGroup
            )

            val attachmentIcon: Drawable? = when {
                message.text == null -> null
                !message.forwards.isNullOrEmpty() -> {
                    if (message.forwards?.size == 1) {
                        resourceManager.iconForwardedMessage
                    } else {
                        resourceManager.iconForwardedMessages
                    }
                }
                else -> VkUtils.getAttachmentConversationIcon(context, message)
            }

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
                else ->
                    when {
                        message.isUser() && messageUser != null && messageUser.firstName.isNotBlank() -> {
                            "${messageUser.firstName}: "
                        }
                        message.isGroup() && messageGroup != null && messageGroup.name.isNotBlank() -> {
                            "${messageGroup.name}: "
                        }
                        else -> ""
                    }
            }

            if ((!conversation.isChat() && !message.isOut) || conversation.id == UserConfig.userId)
                prefix = ""

            val spanText = "$prefix$coloredMessage$messageText"


            val visualizedMessageText = VkUtils.visualizeMentions(
                messageText = spanText,
                resourceManager.colorPrimary
            )
            visualizedMessageText.setSpan(
                ForegroundColorSpan(resourceManager.colorOutline),
                0,
                prefix.length + coloredMessage.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            binding.message.text = visualizedMessageText

            binding.date.text = TimeUtils.getLocalizedTime(context, message.date * 1000L)

            val showUnreadBackgroundCondition =
                (message.isOut && conversation.isOutUnread()) ||
                        (!message.isOut && conversation.isInUnread())

            binding.container.background =
                if (showUnreadBackgroundCondition) resourceManager.conversationUnreadBackground
                else null

            binding.onlineBorder.setImageDrawable(
                ColorDrawable(
                    if (showUnreadBackgroundCondition) resourceManager.colorBackgroundVariant
                    else resourceManager.colorBackground
                )
            )

            binding.counter.toggleVisibility(
                !message.isOut && conversation.isInUnread()
            )
            if (binding.counter.isVisible) {
                if (conversation.unreadCount > 0) {
                    val count =
                        if (conversation.unreadCount > 999) "${conversation.unreadCount / 1000}K"
                        else conversation.unreadCount.toString()
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

    override fun onQueryItem(item: VkConversation, query: String): Boolean {
        val userGroup = VkUtils.getConversationUserGroup(item, profiles, groups)
        val title = VkUtils.getConversationTitle(context, item, userGroup.first, userGroup.second)

        return title.orEmpty().contains(query, ignoreCase = true) ||
                item.lastMessage?.text.orEmpty().contains(query, ignoreCase = true)
    }


}