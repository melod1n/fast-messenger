package com.meloda.fast.adapter.diffutil

import androidx.recyclerview.widget.DiffUtil
import com.meloda.vksdk.model.VKConversation

class ConversationsCallback(
    private val oldList: List<VKConversation>,
    private val newList: List<VKConversation>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        //TODO: rewrite
        return false
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        //TODO: rewrite
        return false
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        //TODO: rewrite
        return null
    }
}