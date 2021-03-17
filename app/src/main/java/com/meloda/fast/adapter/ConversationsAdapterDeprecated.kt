package com.meloda.fast.adapter

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meloda.concurrent.EventInfo
import com.meloda.concurrent.TaskManager
import com.meloda.extensions.ContextExtensions.color
import com.meloda.fast.R
import com.meloda.fast.UserConfig
import com.meloda.fast.adapter.diffutil.ConversationsCallbackDeprecated
import com.meloda.fast.base.BaseAdapter
import com.meloda.fast.base.BaseHolder
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.database.CacheStorage
import com.meloda.fast.util.VKUtils
import com.meloda.fast.widget.CircleImageView
import com.meloda.vksdk.OnResponseListener
import com.meloda.vksdk.VKApiKeys
import com.meloda.vksdk.model.VKConversation
import com.meloda.vksdk.model.VKGroup
import com.meloda.vksdk.model.VKMessage
import com.meloda.vksdk.model.VKUser
import com.meloda.vksdk.util.VKUtil


@Suppress("UNCHECKED_CAST")
class ConversationsAdapterDeprecated(
    val recyclerView: RecyclerView,
    values: ArrayList<VKConversation>
) : BaseAdapter<VKConversation, ConversationsAdapterDeprecated.ConversationHolder>(
    recyclerView.context,
    values
), TaskManager.OnEventListener {

    companion object {
        private const val TAG = "ConversationsAdapter"
    }

    var isLoading: Boolean = false
    private var currentPosition: Int = -1

    init {
        TaskManager.addOnEventListener(this)
    }

    override fun destroy() {
        TaskManager.removeOnEventListener(this)
    }

    override fun onNewEvent(info: EventInfo<*>) {
        when (info.key) {
            VKApiKeys.NEW_MESSAGE.name -> addMessage(info.data as VKMessage)
            VKApiKeys.EDIT_MESSAGE.name -> editMessage(info.data as VKMessage)
            VKApiKeys.RESTORE_MESSAGE.name -> restoreMessage(info.data as VKMessage)
            VKApiKeys.READ_MESSAGE.name -> readMessage(
                (info.data as Array<Int>)[0],
                (info.data as Array<Int>)[1]
            )
            VKApiKeys.DELETE_MESSAGE.name -> deleteMessage(
                (info.data as Array<Int>)[0],
                (info.data as Array<Int>)[1]
            )

            VKApiKeys.UPDATE_CONVERSATION.name -> updateConversation(info.data as Int)
            VKApiKeys.UPDATE_MESSAGE.name -> updateMessage(info.data as Int)
            VKApiKeys.UPDATE_USER.name -> updateUsers(info.data as ArrayList<Int>)
            VKApiKeys.UPDATE_GROUP.name -> updateGroups(info.data as ArrayList<Int>)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationHolder {
        return ConversationHolder(view(R.layout.item_conversation, parent))
    }

    override fun onBindViewHolder(
        holder: ConversationHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        currentPosition = position
        initListeners(holder.itemView, position)
        holder.bind(position, payloads)
    }

    fun isLastItem() = currentPosition >= itemCount - 1

    inner class ConversationHolder(v: View) : BaseHolder(v) {

        private var attachments: ImageView = v.findViewById(R.id.conversationTextAttachment)
        private var text: TextView = v.findViewById(R.id.conversationText)
        private var title: TextView = v.findViewById(R.id.conversationTitle)
        private var avatar: ImageView = v.findViewById(R.id.conversationAvatar)
        private var online: ImageView = v.findViewById(R.id.conversationUserOnline)
        private var out: CircleImageView = v.findViewById(R.id.conversationOut)
        private var counter: TextView = v.findViewById(R.id.conversationCounter)
        private var date: TextView = v.findViewById(R.id.conversationDate)
        private var type: ImageView = v.findViewById(R.id.conversationType)
        private var userAvatar: ImageView = v.findViewById(R.id.conversationUserAvatar)
        private var root: FrameLayout = v.findViewById(R.id.conversationRoot)

        private val colorHighlight = context.color(R.color.accent)

        override fun bind(position: Int) {
            bind(position, mutableListOf())
        }

        override fun bind(position: Int, payloads: MutableList<Any>?) {
            Log.d(TAG, "bind position: $position")

            val conversation = getItem(position)
            val lastMessage = conversation.lastMessage

            TaskManager.execute {
                val peerUser: VKUser? =
                    if (conversation.isUser()) CacheStorage.usersStorage.getUser(conversation.id) else null

                val peerGroup: VKGroup? =
                    if (conversation.isGroup()) CacheStorage.groupsStorage.getGroup(conversation.id) else null

                val fromUser: VKUser? =
                    if (lastMessage.isFromUser()) CacheStorage.usersStorage.getUser(lastMessage.fromId) else null

                val fromGroup: VKGroup? =
                    if (lastMessage.isFromGroup()) CacheStorage.groupsStorage.getGroup(lastMessage.fromId) else null

                conversation.peerUser = peerUser
                conversation.peerGroup = peerGroup

                lastMessage.fromUser = fromUser
                lastMessage.fromGroup = fromGroup

                post {
                    val dialogTitle = setTitle(conversation, peerUser, peerGroup)

                    if (payloads != null && payloads.isNotEmpty()) {
                        for (payload in payloads) {
                            when (payload) {
                                ConversationsCallbackDeprecated.CONVERSATION -> {
                                    setUserOnline(conversation, peerUser)
                                    prepareUserAvatar(
                                        conversation,
                                        lastMessage,
                                        fromUser,
                                        fromGroup
                                    )
                                    prepareAvatar(dialogTitle, conversation, peerUser, peerGroup)
                                    setDialogType(conversation)
                                    setIsRead(lastMessage, conversation)
                                    setCounterBackground(conversation)
                                }
                                ConversationsCallbackDeprecated.MESSAGE -> {
                                    prepareUserAvatar(
                                        conversation,
                                        lastMessage,
                                        fromUser,
                                        fromGroup
                                    )
                                    prepareAttachments(lastMessage)
                                    setIsRead(lastMessage, conversation)
                                    setDate(lastMessage)
                                }
                                ConversationsCallbackDeprecated.GROUP -> {
                                    prepareAvatar(dialogTitle, conversation, peerUser, peerGroup)
                                }
                                ConversationsCallbackDeprecated.USER -> {
                                    setUserOnline(conversation, peerUser)
                                    prepareAvatar(dialogTitle, conversation, peerUser, peerGroup)
                                }
                                ConversationsCallbackDeprecated.EDIT_MESSAGE -> {
                                    prepareUserAvatar(
                                        conversation,
                                        lastMessage,
                                        fromUser,
                                        fromGroup
                                    )
                                    prepareAttachments(lastMessage)
                                    setIsRead(lastMessage, conversation)
                                    setDate(lastMessage)
                                }
                                ConversationsCallbackDeprecated.DATE -> {
                                    setDate(lastMessage)
                                }
                                ConversationsCallbackDeprecated.ONLINE -> {
                                    setUserOnline(conversation, peerUser)
                                }
                                ConversationsCallbackDeprecated.ATTACHMENTS -> {
                                    prepareAttachments(lastMessage)
                                }
                                ConversationsCallbackDeprecated.AVATAR -> {
                                    prepareAvatar(dialogTitle, conversation, peerUser, peerGroup)
                                }
                                ConversationsCallbackDeprecated.USER_AVATAR -> {
                                    prepareUserAvatar(
                                        conversation,
                                        lastMessage,
                                        fromUser,
                                        fromGroup
                                    )
                                }
                                ConversationsCallbackDeprecated.READ -> {
                                    setIsRead(lastMessage, conversation)
                                }
                                ConversationsCallbackDeprecated.NOTIFICATIONS -> {
                                    setCounterBackground(conversation)
                                }
                            }
                        }

                        return@post
                    }

                    setUserOnline(conversation, peerUser)

                    prepareUserAvatar(conversation, lastMessage, fromUser, fromGroup)

                    prepareAvatar(dialogTitle, conversation, peerUser, peerGroup)

                    setDialogType(conversation)

                    prepareAttachments(lastMessage)

                    setIsRead(lastMessage, conversation)

                    setDate(lastMessage)

                    setCounterBackground(conversation)

                    root.isVisible = true
                }
            }
        }

        private fun setTitle(
            conversation: VKConversation,
            peerUser: VKUser?,
            peerGroup: VKGroup?
        ): String {
            val dialogTitle = VKUtil.getTitle(conversation, peerUser, peerGroup)
            title.text = dialogTitle

            return dialogTitle
        }

        private fun setUserOnline(conversation: VKConversation, peerUser: VKUser?) {
            val onlineIcon = VKUtils.getUserOnlineIcon(context, conversation, peerUser)

            online.setImageDrawable(onlineIcon)
            online.isVisible = onlineIcon != null
        }

        private fun prepareUserAvatar(
            conversation: VKConversation,
            lastMessage: VKMessage,
            fromUser: VKUser?,
            fromGroup: VKGroup?
        ) {
            if ((conversation.isChat() || lastMessage.isOut) && !conversation.isGroupChannel) {
                userAvatar.isVisible = true

                val avatar = VKUtil.getUserAvatar(lastMessage, fromUser, fromGroup)

                if (avatar.isEmpty()) {
                    userAvatar.setImageDrawable(ColorDrawable(Color.TRANSPARENT))
                } else {
                    userAvatar.setImageURI(Uri.parse(avatar))
                }
            } else {
                userAvatar.isVisible = false
                userAvatar.setImageDrawable(null)
            }
        }

        private fun prepareAvatar(
            dialogTitle: String,
            conversation: VKConversation,
            peerUser: VKUser?,
            peerGroup: VKGroup?
        ) {
            val dialogAvatarPlaceholder = VKUtils.getAvatarPlaceholder(context, dialogTitle)

            avatar.setImageDrawable(dialogAvatarPlaceholder)

            val avatarLink = VKUtil.getAvatar(conversation, peerUser, peerGroup)

            if (avatarLink.isNotEmpty()) {
                avatar.setImageURI(Uri.parse(avatarLink))
            }
        }

        private fun setDialogType(conversation: VKConversation) {
//            val dDialogType = VKUtil.getDialogType(context, conversation)
//
//            type.setImageDrawable(dDialogType)
//            type.isVisible = dDialogType != null
//            type.isVisible = false
        }

        private fun prepareAttachments(lastMessage: VKMessage) {
//            text.apply {
//                compoundDrawablePadding = 0
//                setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null)
//            }
            attachments.isVisible = false

            if (lastMessage.action == null) {
                when {
                    lastMessage.attachments.isNotEmpty() -> {
                        val attachmentString =
                            VKUtils.getAttachmentText(context, lastMessage.attachments)

                        val attachmentText =
                            if (lastMessage.text.isEmpty()) attachmentString else lastMessage.text

                        val startIndex =
                            if (lastMessage.text.isEmpty()) 0 else lastMessage.text.length

                        val span = SpannableString(attachmentText).apply {
                            setSpan(
                                ForegroundColorSpan(colorHighlight),
                                startIndex,
                                attachmentText.length,
                                0
                            )
                        }

                        val attachmentDrawable =
                            VKUtils.getAttachmentDrawable(context, lastMessage.attachments)
                        text.text = span

                        attachments.isVisible = true
                        attachments.setImageDrawable(attachmentDrawable)

//                        text.apply {
//                            text = span
//                            setCompoundDrawablesRelativeWithIntrinsicBounds(
//                                attachmentDrawable,
//                                null,
//                                null,
//                                null
//                            )
//                            compoundDrawablePadding = 8
//                        }
                    }
                    lastMessage.fwdMessages.isNotEmpty() -> {
                        val fwdText =
                            VKUtils.getFwdText(context, lastMessage.getForwardedMessages())
                        val span = SpannableString(fwdText).apply {
                            setSpan(ForegroundColorSpan(colorHighlight), 0, fwdText.length, 0)
                        }

                        text.text = span
                    }
                    else -> {
                        text.text = if (text.maxLines == 1) lastMessage.text.replace(
                            "\n",
                            " "
                        ) else lastMessage.text
                    }
                }
            } else {
                VKUtils.getActionText(context, lastMessage,
                    object : OnResponseListener<String> {
                        override fun onResponse(response: String) {
                            val span = SpannableString(response).apply {
                                setSpan(
                                    ForegroundColorSpan(colorHighlight),
                                    0,
                                    response.length,
                                    0
                                )
                            }

                            text.text = span
                        }

                        override fun onError(t: Throwable) {
                            TODO("Not yet implemented")
                        }

                    })

            }

            if (lastMessage.attachments.isEmpty() && lastMessage.fwdMessages.isEmpty() && lastMessage.action == null && TextUtils.isEmpty(
                    lastMessage.text
                )
            ) {
                val unknown = "..."
                val span = SpannableString(unknown).apply {
                    setSpan(ForegroundColorSpan(colorHighlight), 0, unknown.length, 0)
                }

                text.text = span
            }
        }

        private fun setIsRead(lastMessage: VKMessage, conversation: VKConversation) {
            val isRead =
                ((lastMessage.isOut && conversation.outReadMessageId == conversation.lastMessageId ||
                        !lastMessage.isOut && conversation.inReadMessageId == conversation.lastMessageId) && conversation.lastMessageId == lastMessage.id) && conversation.unreadCount == 0

            if (isRead) {
                counter.visibility = View.GONE
                out.visibility = View.GONE
            } else {
                if (lastMessage.isOut) {
                    out.visibility = View.VISIBLE
                    counter.visibility = View.GONE
                    counter.text = ""
                } else {
                    out.visibility = View.GONE
                    counter.visibility = View.VISIBLE
                    counter.text = conversation.unreadCount.toString()
                }
            }
        }

        private fun setDate(lastMessage: VKMessage) {
            val dateText = VKUtils.getTime(context, lastMessage)
            date.text = dateText
        }

        private fun setCounterBackground(conversation: VKConversation) {
            counter.background.setTint(if (conversation.isNotificationsDisabled()) Color.GRAY else colorHighlight)
        }
    }

    @Deprecated("Message is bad")
    private fun addMessage(message: VKMessage) {
        val index = searchConversationIndex(message.peerId)

        val oldList = ArrayList(values)

        if (index >= 0) {
            val currentConversation = this[index]

            val conversation = prepareConversation(currentConversation, message)

            removeAt(index)
            add(0, conversation)
            notifyChanges(oldList)
        } else {
//            TaskManager.loadConversation(
//                VKApiKeys.UPDATE_CONVERSATION,
//                message.peerId,
//                null
//            )

            TaskManager.execute {
//                val cachedConversation = MemoryCache.getConversationById(message.peerId)
//                if (cachedConversation != null) {
//                    add(0, prepareConversation(cachedConversation, message))
//                    post { notifyChanges(oldList) }
//                    return@execute
//                }

                val tempConversations = VKConversation().apply {
                    id = message.peerId

                    localId =
                        if (VKUtil.isChatId(id)) id - 2000000000 else id
                    type =
                        if (id < 0) VKConversation.Type.GROUP else if (id > 2000000000) VKConversation.Type.CHAT else VKConversation.Type.USER

                    lastMessage = message
                    lastMessageId = message.id
                }

                add(0, tempConversations)

                post { notifyChanges(oldList) }
            }
        }

        val firstVisiblePosition =
            (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()

        if (firstVisiblePosition <= 1) recyclerView.scrollToPosition(0)
    }

    private fun editMessage(message: VKMessage) {
        val index = searchConversationIndex(message.peerId)
        if (index == -1) return

        val conversation = getItem(index)

        if (conversation.lastMessageId != message.id) return

        conversation.lastMessage = message

        notifyItemChanged(index, ConversationsCallbackDeprecated.EDIT_MESSAGE)
    }

    private fun readMessage(peerId: Int, messageId: Int) {
        val index = searchConversationIndex(peerId)
        if (index == -1) return

        val conversation = getItem(index)
        val message = conversation.lastMessage

        if (message.isInbox()) {
            conversation.inReadMessageId = messageId
        } else {
            conversation.outReadMessageId = messageId
        }

        conversation.unreadCount = if (conversation.lastMessageId == messageId) {
            0
        } else {
            conversation.lastMessageId - messageId
        }

        notifyItemChanged(index, ConversationsCallbackDeprecated.READ)
    }

    @Deprecated("Need to rewrite")
    private fun deleteMessage(peerId: Int, messageId: Int) {
        return
        val index = searchConversationIndex(peerId)
        if (index == -1) return

        val oldList = ArrayList(values)

        val oldDialog = values[index]

        val dialog = oldDialog.clone()

//        TaskManager.execute {
//            val cachedMessages = MemoryCache.getMessagesByPeerId(dialog.conversationId)
//            val messages = VKUtil.sortMessagesByDate(ArrayList(cachedMessages), true)
//
//            if (messages.isEmpty()) {
//                MemoryCache.deleteConversation(dialog.conversationId)
//
//                AppGlobal.post {
//                    removeAt(index)
//                    notifyChanges(oldList)
//                }
//            } else {
//                val lastMessage = messages[0]
//
//                dialog.lastMessageId = lastMessage.messageId
//                dialog.lastMessage = lastMessage
//
//                set(index, dialog)
//
//                VKUtil.sortConversationsByDate(values, true)
//
//                AppGlobal.post {
//                    notifyChanges(oldList)
//                }
//            }
//        }
    }

    @Deprecated("Message is bad")
    private fun restoreMessage(message: VKMessage) {
        val index = searchConversationIndex(message.peerId)
        if (index == -1) return

        val oldList = ArrayList<VKConversation>().apply { addAll(values) }
        val oldDialog = values[index]

        val dialog = oldDialog.clone()

//        TaskManager.execute {
//            val messages =
//                MemoryCache.getMessagesByPeerId(dialog.conversationId).apply { addMessage(message) }
//
//            VKUtil.sortMessagesByDate(ArrayList(messages), true)
//
//            val lastMessage = messages[0]
//
//            dialog.lastMessageId = lastMessage.messageId
//            dialog.lastMessage = lastMessage
//
//            set(index, dialog)
//
//            VKUtil.sortConversationsByDate(values, true)
//
//            AppGlobal.handler.post {
//                notifyChanges(oldList)
//
//                fragmentConversations.presenter.checkListIsEmpty(values)
//            }
//        }
    }

    private fun prepareConversation(
        conversation: VKConversation,
        newMessage: VKMessage
    ): VKConversation {
        conversation.lastMessage = newMessage
        conversation.lastMessageId = newMessage.id

        if (newMessage.isOut) {
            conversation.unreadCount = 0
            newMessage.isRead = false
        } else {
            conversation.unreadCount++
        }

        if (newMessage.peerId == newMessage.fromId && newMessage.fromId == UserConfig.userId) { //для лс
            conversation.outReadMessageId = newMessage.id
        }

        return conversation
    }

    private fun searchConversationIndex(peerId: Int): Int {
        for (i in values.indices) {
            if (getItem(i).id == peerId) return i
        }
        return -1
    }

    private fun searchMessageIndex(messageId: Int): Int {
        for (i in values.indices) {
            if (getItem(i).lastMessageId == messageId) return i
        }
        return -1
    }

    private fun updateConversation(peerId: Int) {
        val index = searchConversationIndex(peerId)
        if (index == -1) return

//        TaskManager.execute {
//            val conversation = MemoryCache.getConversationById(peerId) ?: return@execute
//
//            set(index, conversation)
//
//            AppGlobal.post {
//                notifyItemChanged(
//                    index,
//                    ConversationsCallbackDeprecated.CONVERSATION
//                )
//            }
//        }
    }

    private fun updateGroups(groupIds: ArrayList<Int>) {
        for (groupId in groupIds) {
            val index = searchConversationIndex(groupId)
            if (index == -1) return

            notifyItemChanged(index)
        }
    }

    private fun updateUsers(userIds: ArrayList<Int>) {
        for (userId in userIds) {
            val index = searchConversationIndex(userId)
            if (index == -1) return

            notifyItemChanged(index)
        }
    }

    private fun updateMessage(messageId: Int) {
        val index = searchMessageIndex(messageId)
        if (index == -1) return


        TaskManager.execute {
            val conversation = getItem(index).clone()

//            conversation.apply {
//                lastMessageId = messageId
//                lastMessage = MemoryCache.getMessageById(messageId) ?: return@execute
//            }

            AppGlobal.handler.post {
                notifyItemChanged(
                    index,
                    ConversationsCallbackDeprecated.MESSAGE
                )
            }
        }
    }

}