package com.meloda.fast.screens.chatinfo

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.meloda.fast.api.model.VkChat
import com.meloda.fast.base.adapter.BaseAdapter
import com.meloda.fast.base.adapter.BaseHolder
import com.meloda.fast.databinding.ItemChatMemberBinding
import java.util.*

class ChatInfoMembersAdapter(
    context: Context,
    preAddedValues: List<VkChat.ChatMember>
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

        override fun bind(position: Int) {
            val chatMember = getItem(position)

            val title = chatMember.name ?: "${chatMember.firstName} ${chatMember.lastName}"

            binding.title.text = title
        }
    }

}