package ru.melod1n.project.vkm.adapter.diffutil

import androidx.recyclerview.widget.DiffUtil
import ru.melod1n.project.vkm.api.model.VKConversation

class ConversationsCallback(
    private val oldList: List<VKConversation>,
    private val newList: List<VKConversation>
) : DiffUtil.Callback() {

    companion object {
        const val DATE = "date"
        const val ONLINE = "online"
        const val AVATAR = "avatar"
        const val USER_AVATAR = "user_avatar"
        const val ATTACHMENTS = "attachments"
        const val READ = "read"
        const val NOTIFICATIONS = "notifications"
        const val EDIT_MESSAGE = "edit_message"
        const val MESSAGE = "message"
        const val USER = "user"
        const val GROUP = "group"
        const val CONVERSATION = "conversation"
    }

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]

        if (true) return false
        return old.conversationId == new.conversationId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]

        val oldMessage = old.lastMessage
        val newMessage = new.lastMessage

        if (true) return false else

            return old.title == new.title &&
                    old.lastMessageId == new.lastMessageId &&
                    old.photo50 == new.photo50 &&
                    old.unreadCount == new.unreadCount &&

                    old.isNoSound == new.isNoSound &&
                    old.isDisabledForever == new.isDisabledForever &&
                    old.disabledUntil == new.disabledUntil &&

                    old.inRead == new.inRead &&
                    old.outRead == new.outRead &&

                    old.peerUser == new.peerUser &&
                    old.peerGroup == new.peerGroup &&

                    oldMessage == newMessage

//                oldMessage.messageId == newMessage.messageId &&
//                oldMessage.isOut == newMessage.isOut &&
//                oldMessage.fromId == newMessage.fromId &&
//                oldMessage.date == newMessage.date &&
//                oldMessage.action == newMessage.action &&
//                oldMessage.text == newMessage.text &&
//                oldMessage.attachments == newMessage.attachments &&
//                oldMessage.fwdMessages == newMessage.fwdMessages &&
//                oldMessage.messageId == newMessage.messageId &&
//
//                oldMessage.fromUser == newMessage.fromUser &&
//                oldMessage.fromGroup == newMessage.fromGroup
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val oldConversation = oldList[oldItemPosition]
        val newConversation = newList[newItemPosition]

        val oldMessage = oldConversation.lastMessage
        val newMessage = newConversation.lastMessage

        val oldDate = oldMessage.date
        val newDate = newMessage.date

//        if (oldDate != newDate) return DATE

//        if (oldMessage != newMessage) return MESSAGE

        return super.getChangePayload(oldItemPosition, newItemPosition)
    }

}