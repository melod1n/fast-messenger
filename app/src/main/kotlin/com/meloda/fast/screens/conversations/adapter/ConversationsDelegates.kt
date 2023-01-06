package com.meloda.fast.screens.conversations.adapter

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import com.meloda.fast.R
import com.meloda.fast.api.model.data.ActionState
import com.meloda.fast.api.model.presentation.VkConversationUi
import com.meloda.fast.base.adapter.OnItemClickListener
import com.meloda.fast.base.adapter.OnItemLongClickListener
import com.meloda.fast.databinding.ItemConversationBinding
import com.meloda.fast.ext.ImageLoader.loadWithGlide
import com.meloda.fast.ext.gone
import com.meloda.fast.ext.toggleVisibility
import com.meloda.fast.ext.visible
import com.meloda.fast.model.base.AdapterDiffItem
import com.meloda.fast.model.base.Image
import com.meloda.fast.model.base.asString
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

        val colorPrimary = getColor(R.color.colorPrimary)
        val colorOutline = getColor(R.color.colorOutline)
        val colorOnPrimary = getColor(R.color.colorOnPrimary)
        val colorUserAvatarAction = getColor(R.color.colorUserAvatarAction)
        val colorOnUserAvatarAction = getColor(R.color.colorOnUserAvatarAction)
        val colorBackground = getColor(R.color.colorBackground)
        val colorBackgroundVariant = getColor(R.color.colorBackgroundVariant)

        val unreadBackground = getDrawable(R.drawable.ic_message_unread)
        val icLauncherColor = getColor(R.color.a1_500)

        bind {
            binding.container.background = if (item.isRead) unreadBackground else null

            binding.title.text = item.title.asString(context)

            binding.message.toggleVisibility(item.message != null)
            binding.message.text = item.message

            binding.date.toggleVisibility(item.date != null)
            binding.date.text = TimeUtils.getLocalizedTime(context, (item.date ?: -1) * 1000L)

            binding.service.toggleVisibility(item.actionState != ActionState.None)
            binding.phantomIcon.toggleVisibility(item.actionState == ActionState.Phantom)
            binding.callIcon.toggleVisibility(item.actionState == ActionState.CallInProgress)

            binding.counter.toggleVisibility(item.unreadCount != null)
            binding.counter.text = item.unreadCount

            binding.textAttachment.toggleVisibility(item.attachmentImage != null)

            binding.avatarPlaceholder.visible()
            when (val avatar = item.avatar) {
                is Image.Url -> {
                    binding.avatar.loadWithGlide(
                        url = avatar.url,
                        crossFade = true,
                        onLoadedAction = { binding.avatarPlaceholder.gone() }
                    )
                }
                is Image.Simple -> {
                    if (avatar == ColorDrawable(Color.TRANSPARENT)) {
                        binding.avatar.setImageDrawable(ColorDrawable(colorOnPrimary))
                    } else {
                        binding.avatar.setImageDrawable(avatar.drawable)
                    }
                }
                else -> Unit
            }
        }
    }
