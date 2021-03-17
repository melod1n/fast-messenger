package com.meloda.fast.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.meloda.concurrent.EventInfo
import com.meloda.concurrent.TaskManager
import com.meloda.fast.R
import com.meloda.fast.VKLongPollParser
import com.meloda.fast.adapter.diffutil.ConversationsCallback
import com.meloda.fast.base.BaseAdapter
import com.meloda.fast.base.BaseHolder
import com.meloda.vksdk.model.VKConversation
import com.meloda.vksdk.model.VKMessage

class ChatsAdapter(context: Context, values: ArrayList<VKConversation>) :
    BaseAdapter<VKConversation, ChatsAdapter.ViewHolder>(
        context, values
    ),
    TaskManager.OnEventListener,
    VKLongPollParser.OnMessagesListener {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(view(R.layout.item_conversation, parent))
    }

    override fun notifyChanges(oldList: List<VKConversation>, newList: List<VKConversation>) {
        val callback = ConversationsCallback(oldList, newList)
        val diff = DiffUtil.calculateDiff(callback)

        diff.dispatchUpdatesTo(this)
    }

    override fun onNewEvent(info: EventInfo<*>) {

    }

    inner class ViewHolder(v: View) : BaseHolder(v) {

        override fun bind(position: Int, payloads: MutableList<Any>?) {
            val conversation = getItem(position)
            val lastMessage = conversation.lastMessage

            TaskManager.execute {

            }
        }

    }

    override fun onNewMessage(message: VKMessage) {
        TODO("Not yet implemented")
    }

    override fun onEditMessage(message: VKMessage) {
        TODO("Not yet implemented")
    }

    override fun onRestoredMessage(message: VKMessage) {
        TODO("Not yet implemented")
    }

    override fun onDeleteMessage(peerId: Int, messageId: Int) {
        TODO("Not yet implemented")
    }

    override fun onReadMessage(peerId: Int, messageId: Int) {
        TODO("Not yet implemented")
    }
}