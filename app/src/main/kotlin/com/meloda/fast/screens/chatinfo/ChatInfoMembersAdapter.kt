package com.meloda.fast.screens.chatinfo

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.model.VkChat
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.base.adapter.BaseAdapter
import com.meloda.fast.base.adapter.BaseHolder
import com.meloda.fast.databinding.ItemChatMemberBinding
import com.meloda.fast.extensions.ImageLoader.loadWithGlide
import com.meloda.fast.extensions.toggleVisibility
import java.text.SimpleDateFormat
import java.util.*

class ChatInfoMembersAdapter(
    context: Context,
    preAddedValues: List<VkChat.ChatMember>,
    private val profiles: List<VkUser>,
    private val groups: List<VkGroup>,
    private val confirmRemoveMemberAction: ((memberId: Int) -> Unit)? = null
) : BaseAdapter<VkChat.ChatMember, ChatInfoMembersAdapter.Holder>(
    context,
    comparator,
    preAddedValues
) {

    companion object {
        val comparator = object : DiffUtil.ItemCallback<VkChat.ChatMember>() {
            override fun areItemsTheSame(
                oldItem: VkChat.ChatMember,
                newItem: VkChat.ChatMember
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: VkChat.ChatMember,
                newItem: VkChat.ChatMember
            ): Boolean {
                return Objects.deepEquals(oldItem, newItem)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(ItemChatMemberBinding.inflate(inflater, parent, false))
    }

    inner class Holder(
        private val binding: ItemChatMemberBinding
    ) : BaseHolder(binding.root) {

        private val colorOnBackground = ContextCompat.getColor(context, R.color.colorOnBackground)
        private val colorPrimary = ContextCompat.getColor(context, R.color.colorPrimary)

        override fun bind(position: Int) {
            val chatMember = getItem(position)

            binding.avatar.loadWithGlide(
                url = chatMember.photo200,
                crossFade = true,
                placeholderColor = Color.GRAY,
                errorColor = Color.RED
            )

            val title = chatMember.name ?: "${chatMember.firstName} ${chatMember.lastName}"
            binding.title.text = title

            binding.online.toggleVisibility(chatMember.isProfile())
            binding.online.text =
                if (chatMember.isOnline == true) "Online"
                else if (chatMember.lastSeen != null) "Last seen at ${
                    SimpleDateFormat(
                        "HH:mm",
                        Locale.getDefault()
                    ).format(chatMember.lastSeen * 1000L)
                }"
                else "Offline"

            binding.star.toggleVisibility(chatMember.isAdmin || chatMember.isOwner)
            binding.star.imageTintList =
                ColorStateList.valueOf(
                    if (chatMember.isOwner) colorPrimary
                    else colorOnBackground
                )

            binding.remove.toggleVisibility(
                chatMember.canKick || chatMember.id == UserConfig.userId
            )
            binding.remove.setOnClickListener { confirmRemoveMemberAction?.invoke(chatMember.id) }
        }
    }

    fun searchMemberIndex(memberId: Int): Int? {
        for (i in indices) {
            val member = getItem(i)
            if (member.id == memberId) return i
        }

        return null
    }
}