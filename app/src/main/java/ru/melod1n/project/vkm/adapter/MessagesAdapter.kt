package ru.melod1n.project.vkm.adapter

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import ru.melod1n.project.vkm.BuildConfig
import ru.melod1n.project.vkm.R
import ru.melod1n.project.vkm.activity.MessagesActivity
import ru.melod1n.project.vkm.api.VKApiKeys
import ru.melod1n.project.vkm.api.model.*
import ru.melod1n.project.vkm.api.util.VKUtil
import ru.melod1n.project.vkm.base.BaseAdapter
import ru.melod1n.project.vkm.base.BaseHolder
import ru.melod1n.project.vkm.common.AppGlobal
import ru.melod1n.project.vkm.common.TaskManager
import ru.melod1n.project.vkm.database.MemoryCache
import ru.melod1n.project.vkm.event.EventInfo
import ru.melod1n.project.vkm.extensions.FloatExtensions.int
import ru.melod1n.project.vkm.listener.OnResponseListener
import ru.melod1n.project.vkm.util.AndroidUtils
import ru.melod1n.project.vkm.util.ImageUtils
import ru.melod1n.project.vkm.widget.BoundedLinearLayout
import ru.melod1n.project.vkm.widget.CircleImageView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs


@Suppress("UNCHECKED_CAST")
class MessagesAdapter(
    context: Context,
    values: ArrayList<VKMessage>,
    var conversation: VKConversation

) : BaseAdapter<VKMessage, MessagesAdapter.Holder>(context, values),
    TaskManager.OnEventListener {

    companion object {
        private const val TYPE_FOOTER = 10101

        private const val TYPE_NORMAL_IN = 7910
        private const val TYPE_NORMAL_OUT = 7911

        private const val TYPE_ATTACHMENT_IN = 7920
        private const val TYPE_ATTACHMENT_OUT = 7921

        private const val TYPE_ACTION = 7930

        private const val TYPE_NORMAL_CHANNEL = 7940

        const val TAG = "MessagesAdapter"
    }

    private var recyclerView = (context as MessagesActivity).recyclerView
    private var layoutManager = recyclerView.layoutManager as LinearLayoutManager

    var isNotCachedValues = false

    init {
        TaskManager.addOnEventListener(this)
    }

    override fun destroy() {
        TaskManager.removeOnEventListener(this)
    }

    override fun onNewEvent(info: EventInfo<*>) {
        when (info.key) {
            VKApiKeys.NEW_MESSAGE -> addMessage(info.data as VKMessage)

            VKApiKeys.READ_MESSAGE -> readMessage(
                (info.data as Array<Int>)[0],
                (info.data as Array<Int>)[1]
            )

            VKApiKeys.RESTORE_MESSAGE -> restoreMessage(info.data as VKMessage)
            VKApiKeys.EDIT_MESSAGE -> editMessage(info.data as VKMessage)
            VKApiKeys.DELETE_MESSAGE -> deleteMessage(
                (info.data as Array<Int>)[0],
                (info.data as Array<Int>)[1]
            )

            VKApiKeys.UPDATE_MESSAGE -> updateMessage(info.data as Int)
            VKApiKeys.UPDATE_USER -> updateUser(info.data as ArrayList<Int>)
            VKApiKeys.UPDATE_GROUP -> updateGroup(info.data as ArrayList<Int>)

            else -> return
        }
    }

    override fun getItemCount(): Int {
        return values.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        if (position == values.size) return TYPE_FOOTER

        val message = getItem(position)

        return when {
            message.action != null -> TYPE_ACTION
            conversation.isGroupChannel -> TYPE_NORMAL_CHANNEL
            message.isOut && message.attachments.isEmpty() && message.fwdMessages.isEmpty() -> TYPE_NORMAL_OUT
            !message.isOut && message.attachments.isEmpty() && message.fwdMessages.isEmpty() -> TYPE_NORMAL_IN
            message.isOut && (message.attachments.isNotEmpty() || message.fwdMessages.isNotEmpty()) -> TYPE_ATTACHMENT_OUT
            !message.isOut && (message.attachments.isNotEmpty() || message.fwdMessages.isNotEmpty()) -> TYPE_ATTACHMENT_IN
            else -> 0
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, type: Int): Holder {
        return when (type) {
            TYPE_FOOTER -> FooterHolder(generateEmptyView())

            TYPE_NORMAL_IN -> ItemNormalIn(view(R.layout.item_message_normal_in, viewGroup))
            TYPE_NORMAL_OUT -> ItemNormalOut(view(R.layout.item_message_normal_out, viewGroup))

            TYPE_ATTACHMENT_IN -> ItemAttachmentIn(
                view(
                    R.layout.item_message_attachment_in,
                    viewGroup
                )
            )
            TYPE_ATTACHMENT_OUT -> ItemAttachmentOut(
                view(
                    R.layout.item_message_attachment_out,
                    viewGroup
                )
            )

            TYPE_ACTION -> ItemAction(view(R.layout.item_message_action, viewGroup))

            TYPE_NORMAL_CHANNEL -> ItemChannel(view(R.layout.item_message_channel, viewGroup))

            else -> PlaceHolder(view(R.layout.item_message, viewGroup))
        }
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "bind position: $position")
        }

        if (holder is FooterHolder) return

        super.onBindViewHolder(holder, position)
        if (!isNotCachedValues) return

        val message = this[position]

        if (message.isUnreaded()) {
            TaskManager.readMessage(
                VKApiKeys.READ_MESSAGE,
                conversation.conversationId,
                message.messageId
            )
        }
    }

    private fun generateEmptyView(): View {
        return View(context).also {
            it.isFocusable = false
            it.isClickable = false
            it.isEnabled = false
            it.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                if (conversation.isGroupChannel) 0 else AndroidUtils.px(74f).int()
            )
        }
    }

    inner class FooterHolder(v: View) : Holder(v) {
        override fun bind(position: Int) {}
    }

    inner class ItemChannel(v: View) : ItemNormalIn(v) {
        private val title: TextView = v.findViewById(R.id.channelTitle)

        override fun bind(position: Int) {
            val message = getItem(position)

            ViewController().prepareDate(message, date)

            val avatarString = conversation.photo100

            val placeHolder = VKUtil.getAvatarPlaceholder(context, conversation.title)

            avatar.setImageDrawable(placeHolder)
            ImageUtils.loadImage(avatarString, avatar, placeHolder)

            title.text = conversation.title

            text.text = message.text

            root.visibility = View.VISIBLE
        }
    }

    inner class ItemAction(v: View) : Holder(v) {
        private val text: TextView = v.findViewById(R.id.messageAction)

        override fun bind(position: Int) {
            val message = getItem(position)

            TaskManager.execute {
                val user = searchUser(message)
                val group = searchGroup(message)

                val name =
                    (if (group == null && !VKGroup.isGroupId(message.fromId)) user?.firstName else group?.name)
                        ?: "null"

                VKUtil.getActionText(context, message, object : OnResponseListener<String> {

                    override fun onResponse(response: String) {
                        val actionText = "$name $response"

                        val spannable = SpannableString(actionText)
                        spannable.setSpan(StyleSpan(Typeface.BOLD), 0, name.length, 0)

                        text.text = spannable
                    }

                    override fun onError(t: Throwable) {
                    }
                })

                post { text.isVisible = true }
            }
        }
    }

    open inner class ItemNormalIn(v: View) : NormalViewHolder(v) {

        override fun bind(position: Int) {
            val message = getItem(position)

            TaskManager.execute {
                val user = searchUser(message)
                val group = searchGroup(message)

                post {
                    ViewController().apply {
                        prepareText(message, bubble, text)
                        prepareDate(message, date)
                        prepareAvatar(message, avatar)
                        loadAvatarImage(message, user, group, avatar)
                    }

                    root.isVisible = true
                }
            }
        }
    }

    inner class ItemAttachmentIn(v: View) : ItemNormalIn(v) {
        val attachments: LinearLayout = v.findViewById(R.id.messageAttachments)

        override fun bind(position: Int) {
            super.bind(position)

            val message = getItem(position)

            AttachmentInflater.showAttachments(message, this)
        }
    }

    open inner class ItemNormalOut(v: View) : NormalViewHolder(v) {

        override fun bind(position: Int) {
            val message = getItem(position)

            TaskManager.execute {
                val user = searchUser(message)
                val group = searchGroup(message)

                post {
                    ViewController().apply {
                        prepareText(message, bubble, text)
                        prepareDate(message, date)
                        prepareAvatar(message, avatar)
                        loadAvatarImage(message, user, group, avatar)
                    }

                    root.isVisible = true
                }
            }
        }
    }

    inner class ItemAttachmentOut(v: View) : ItemNormalOut(v) {

        val attachments: LinearLayout = v.findViewById(R.id.messageAttachments)

        override fun bind(position: Int) {
            super.bind(position)

            val message = getItem(position)

            AttachmentInflater.showAttachments(message, this)
        }

    }

    abstract inner class NormalViewHolder(v: View) : Holder(v) {
        protected val date: TextView = v.findViewById(R.id.messageDate)
        protected val text: TextView = v.findViewById(R.id.messageText)
        protected val root: LinearLayout = v.findViewById(R.id.messageRoot)
        protected val bubble: BoundedLinearLayout = v.findViewById(R.id.messageBubble)
        protected val avatar: CircleImageView = v.findViewById(R.id.messageAvatar)
    }

    object AttachmentInflater {
        fun showAttachments(message: VKMessage, holder: NormalViewHolder) {
            val attachments =
                (if (holder is ItemAttachmentOut) holder.attachments else if (holder is ItemAttachmentIn) holder.attachments else null)
                    ?: return

            if (message.fwdMessages.isNotEmpty() || message.attachments.isNotEmpty()) {
                attachments.visibility = View.VISIBLE
                attachments.removeAllViews()
            } else {
                attachments.visibility = View.GONE
            }

            if (message.attachments.isNotEmpty()) {
                prepareAttachments(message, attachments)
            }

            if (message.fwdMessages.isNotEmpty()) {
                prepareForwardedMessages(message, attachments)
            }
        }

        private fun prepareAttachments(message: VKMessage, attachments: LinearLayout) {
            for (attachment in message.attachments) {
                when (attachment) {
                    is VKPhoto -> photo(message, attachments)
                    is VKVideo -> video(message, attachments)
                    is VKLink -> link(message, attachments)
                    is VKAudio -> audio(message, attachments)
                    is VKDoc -> doc(message, attachments)
                }
            }
        }

        private fun prepareForwardedMessages(message: VKMessage, attachments: LinearLayout) {

        }

        fun link(message: VKMessage, attachments: LinearLayout) {

        }

        fun video(message: VKMessage, attachments: LinearLayout) {

        }

        fun photo(message: VKMessage, attachments: LinearLayout) {
        }

        fun audio(message: VKMessage, attachments: LinearLayout) {

        }

        fun doc(message: VKMessage, attachments: LinearLayout) {

        }
    }

    inner class ViewController {

        fun prepareText(message: VKMessage, bubble: BoundedLinearLayout, text: TextView) {
            val screenWidth = context.resources.displayMetrics.widthPixels
            val boundedWidth = screenWidth - screenWidth / 5
            bubble.maxWidth = boundedWidth

            text.text = VKUtil.matchMentions(message.text)
        }

        fun prepareDate(message: VKMessage, date: TextView) {
            var dateText =
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(message.date * 1000L)

            if (message.editTime > 0) {
                dateText += ", ${
                    context.getString(R.string.edited)
                        .toLowerCase(Locale.getDefault())
                }"
            }

            date.text = dateText
        }

        fun prepareAvatar(message: VKMessage, avatar: ImageView) {
            avatar.isVisible = !message.isOut
        }

        fun loadAvatarImage(message: VKMessage, user: VKUser?, group: VKGroup?, avatar: ImageView) {
            val dialogTitle = VKUtil.getMessageTitle(message, user, group)
            val avatarPlaceholder = VKUtil.getAvatarPlaceholder(context, dialogTitle)

            avatar.setImageDrawable(avatarPlaceholder)

            val avatarString = VKUtil.getUserAvatar(message, user, group)

            ImageUtils.loadImage(avatarString, avatar, avatarPlaceholder)
        }

    }

    open inner class Holder(v: View) : BaseHolder(v) {
        override fun bind(position: Int) {
        }
    }

    inner class PlaceHolder(v: View) : NormalViewHolder(v)

    private fun searchUser(message: VKMessage): VKUser? {
        if (!message.isFromUser()) return null

        return VKUtil.searchUser(message.fromId)
    }

    private fun searchGroup(message: VKMessage): VKGroup? {
        if (!message.isFromGroup()) return null

        return VKUtil.searchGroup(message.fromId)
    }

    private fun updateGroup(groupIds: ArrayList<Int>) {
        for (groupId in groupIds) {
            var index = -1

            for (i in values.indices) {
                val item = getItem(i)

                if (abs(item.fromId) == groupId) {
                    index = i
                    break
                }
            }

            if (index == -1) return

            notifyItemChanged(index)
        }
    }

    private fun updateUser(userIds: ArrayList<Int>) {
        for (userId in userIds) {
            var index = -1

            for (i in values.indices) {
                val item = getItem(i)

                if (item.fromId == userId) {
                    index = i
                    break
                }
            }

            if (index == -1) return
            notifyItemChanged(index)
        }
    }

    private fun updateMessage(messageId: Int) {
        var index = -1

        for (i in values.indices) {
            val item = getItem(i)

            if (item.messageId == messageId) {
                index = i
                break
            }
        }

        if (index == -1) return

        TaskManager.execute {
            AppGlobal.database.messages.getById(messageId)?.let {
                values[index] = it

                post { notifyItemChanged(index) }
            }
        }
    }

    private fun searchMessagePosition(messageId: Int): Int {
        for (i in values.indices) {
            if (getItem(i).messageId == messageId) return i
        }

        return -1
    }

    private fun containsRandomId(randomId: Int): Boolean {
        for (message in values) {
            if (message.randomId == randomId) return true
        }

        return false
    }

    fun addMessage(message: VKMessage, fromApp: Boolean = false, withScroll: Boolean = false) {
        val randomId = message.randomId
        if (randomId > 0 && containsRandomId(message.randomId) || message.peerId != conversation.conversationId) return

        add(message)

        notifyDataSetChanged()

        val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()

        if ((message.isInbox() && lastVisiblePosition >= itemCount - 2) || !fromApp || withScroll) {
            recyclerView.scrollToPosition(itemCount - 1)
        }
    }

    private fun readMessage(peerId: Int, messageId: Int) {
        if (peerId != conversation.conversationId) return

        val index = searchMessagePosition(messageId)
        if (index == -1) return

        val message = this[index]
        message.isRead = true

        notifyDataSetChanged()

        if (message.isInbox()) {
            conversation.inRead = messageId
        } else {
            conversation.outRead = messageId
        }

        conversation.unreadCount--

        TaskManager.execute {
            MemoryCache.put(message)
            MemoryCache.put(conversation)
        }
    }

    fun editMessage(message: VKMessage) {
        val index = searchMessagePosition(message.messageId)
        if (index == -1) return

        set(index, message)
        notifyDataSetChanged()
    }

    fun deleteMessage(messageId: Int, peerId: Int) {
        if (peerId != conversation.conversationId) return

        val index = searchMessagePosition(messageId)
        if (index == -1) return

        removeAt(index)
        notifyDataSetChanged()
    }

    //TODO: кривое сообщение
    fun restoreMessage(message: VKMessage) {
        if (message.peerId != conversation.conversationId) return

        updateValues(VKUtil.sortMessagesByDate(values.apply { add(message) }, false))
        notifyDataSetChanged()
    }
}