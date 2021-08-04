package com.meloda.fast.fragment.messages

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.meloda.fast.api.model.VKConversation
import com.meloda.fast.base.adapter.BaseAdapter
import com.meloda.fast.base.adapter.BindingHolder
import com.meloda.fast.databinding.ItemConversationBinding

class ConversationsAdapter(context: Context, values: ArrayList<VKConversation>) :
    BaseAdapter<VKConversation, ConversationsAdapter.ItemHolder>(
        context, values, COMPARATOR
    ) {

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<VKConversation>() {
            override fun areItemsTheSame(
                oldItem: VKConversation,
                newItem: VKConversation
            ) = false

            override fun areContentsTheSame(
                oldItem: VKConversation,
                newItem: VKConversation
            ) = false
        }
    }

    inner class ItemHolder(binding: ItemConversationBinding) :
        BindingHolder<ItemConversationBinding>(binding) {

        override fun bind(position: Int) {
            binding.title.text = getItem(position).title ?: "HUI"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ItemHolder(ItemConversationBinding.inflate(inflater, parent, false))

}