package com.meloda.fast.screens.messages

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.meloda.fast.api.model.VkConversation
import com.meloda.fast.base.adapter.BaseAdapter
import com.meloda.fast.base.adapter.BindingHolder
import com.meloda.fast.databinding.ItemConversationBinding

class ConversationsAdapter(context: Context, values: MutableList<VkConversation>) :
    BaseAdapter<VkConversation, ConversationsAdapter.ItemHolder>(
        context, values, COMPARATOR
    ) {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ItemHolder(ItemConversationBinding.inflate(inflater, parent, false))

    inner class ItemHolder(binding: ItemConversationBinding) :
        BindingHolder<ItemConversationBinding>(binding) {

        override fun bind(position: Int) {
            binding.title.text = getItem(position).title ?: "HUI"
        }
    }

}