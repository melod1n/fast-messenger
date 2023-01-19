package com.meloda.fast.screens.conversations.adapter

import android.graphics.drawable.Drawable
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VkUtils
import com.meloda.fast.api.model.ActionState
import com.meloda.fast.api.model.presentation.VkConversationUi
import com.meloda.fast.base.adapter.OnItemClickListener
import com.meloda.fast.base.adapter.OnItemLongClickListener
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.databinding.ItemConversationBinding
import com.meloda.fast.ext.gone
import com.meloda.fast.ext.isFalse
import com.meloda.fast.ext.isTrue
import com.meloda.fast.ext.toggleVisibility
import com.meloda.fast.ext.visible
import com.meloda.fast.model.base.AdapterDiffItem
import com.meloda.fast.model.base.asString
import com.meloda.fast.model.base.setImage
import com.meloda.fast.screens.conversations.ConversationsResourceProvider
import com.meloda.fast.screens.settings.SettingsFragment
import com.meloda.fast.util.TimeUtils

fun conversationDelegate(
    onItemClickListener: OnItemClickListener<VkConversationUi>,
    onItemLongClickListener: OnItemLongClickListener<VkConversationUi>,
) =
    adapterDelegateViewBinding<VkConversationUi, AdapterDiffItem, ItemConversationBinding>(
        viewBinding = { layoutInflater, parent ->
            ItemConversationBinding.inflate(layoutInflater, parent, false)
        }
    ) {
        binding.root.setOnClickListener { onItemClickListener.onItemClick(item) }
        binding.root.setOnLongClickListener { onItemLongClickListener.onLongItemClick(item) }

        val resourceProvider = ConversationsResourceProvider(context)

        bind {
            val isMultilineEnabled =
                AppGlobal.preferences.getBoolean(SettingsFragment.KEY_APPEARANCE_MULTILINE, true)
            val maxLines = if (isMultilineEnabled) 2 else 1

            binding.title.maxLines = maxLines
            binding.message.maxLines = maxLines

            binding.container.background =
                if (item.isRead) resourceProvider.conversationUnreadBackground else null

            binding.title.text = item.title.asString(context)

            binding.date.toggleVisibility(item.date != null)
            binding.date.text = TimeUtils.getLocalizedTime(context, (item.date ?: -1) * 1000L)

            binding.service.toggleVisibility(item.actionState != ActionState.None)
            binding.phantomIcon.toggleVisibility(item.actionState == ActionState.Phantom)
            binding.callIcon.toggleVisibility(item.actionState == ActionState.CallInProgress)

            binding.counter.toggleVisibility(item.unreadCount != null)
            binding.counter.text = item.unreadCount

            binding.textAttachment.toggleVisibility(item.attachmentImage != null)

            binding.pin.toggleVisibility(item.isPinned)

            binding.online.toggleVisibility(item.isOnline)

            binding.avatarPlaceholder.visible()
            binding.avatar.setImage(item.avatar) {
                asCircle = true
                crossFade = true
                onLoadedAction = { binding.avatarPlaceholder.gone() }
            }

            val actionMessage = VkUtils.getActionConversationText(
                context = context,
                message = item.lastMessage,
                youPrefix = resourceProvider.youPrefix,
                messageUser = item.messageUser,
                messageGroup = item.messageGroup,
                action = item.action,
                actionUser = item.actionUser,
                actionGroup = item.actionGroup
            )

            val attachmentIcon: Drawable? = when {
                item.lastMessage?.text == null -> null
                !item.lastMessage?.forwards.isNullOrEmpty() -> {
                    if (item.lastMessage?.forwards?.size == 1) {
                        resourceProvider.iconForwardedMessage
                    } else {
                        resourceProvider.iconForwardedMessages
                    }
                }
                else -> VkUtils.getAttachmentConversationIcon(context, item.lastMessage)
            }

            binding.textAttachment.toggleVisibility(attachmentIcon != null)
            binding.textAttachment.setImageDrawable(attachmentIcon)

            val attachmentText = (if (attachmentIcon == null) VkUtils.getAttachmentText(
                message = item.lastMessage
            ) else null)?.asString(context)

            val forwardsMessage = (if (item.lastMessage?.text == null) VkUtils.getForwardsText(
                message = item.lastMessage
            ) else null)?.asString(context)

            val messageText = (if (
                actionMessage != null ||
                forwardsMessage != null ||
                attachmentText != null
            ) ""
            else item.lastMessage?.text ?: "").run { VkUtils.prepareMessageText(this) }

            val coloredMessage = actionMessage ?: attachmentText ?: forwardsMessage ?: ""

            var prefix = when {
                actionMessage != null -> ""
                item.lastMessage?.isOut.isTrue -> "${resourceProvider.youPrefix}: "
                else ->
                    when {
                        item.lastMessage?.isUser().isTrue && item.messageUser != null && item.messageUser?.firstName?.isNotBlank().isTrue -> {
                            "${item.messageUser?.firstName}: "
                        }
                        item.lastMessage?.isGroup().isTrue && item.messageGroup != null && item.messageGroup?.name?.isNotBlank().isTrue -> {
                            "${item.messageGroup?.name}: "
                        }
                        else -> ""
                    }
            }

            if ((!item.peerType.isChat() && item.lastMessage?.isOut.isFalse) || item.conversationId == UserConfig.userId)
                prefix = ""

            val spanText = "$prefix$coloredMessage$messageText"

            val visualizedMessageText = VkUtils.visualizeMentions(
                messageText = spanText,
                resourceProvider.colorPrimary
            )

            val length = prefix.length + coloredMessage.length
            visualizedMessageText.setSpan(
                ForegroundColorSpan(resourceProvider.colorOutline),
                0,
                length,
                if (length > 0) Spanned.SPAN_EXCLUSIVE_EXCLUSIVE else 0
            )

            binding.message.text = visualizedMessageText
        }
    }
