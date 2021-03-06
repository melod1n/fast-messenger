package com.meloda.fast.activity.ui.repository

import com.meloda.fast.R
import com.meloda.fast.api.VKApi
import com.meloda.fast.api.VKApiKeys
import com.meloda.fast.api.model.VKConversation
import com.meloda.fast.api.model.VKGroup
import com.meloda.fast.api.model.VKMessage
import com.meloda.fast.api.model.VKUser
import com.meloda.fast.api.util.VKUtil
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.common.TaskManager
import com.meloda.fast.database.MemoryCache
import com.meloda.fast.extensions.ArrayExtensions.asArrayList
import com.meloda.fast.listener.OnResponseListener
import com.meloda.fast.util.ArrayUtils
import com.meloda.mvp.MvpOnLoadListener
import com.meloda.mvp.MvpRepository
import java.util.*

class MessagesRepositoryDeprecated : MvpRepository<VKMessage>() {

    fun loadMessages(
        peerId: Int,
        offset: Int,
        count: Int,
        listener: MvpOnLoadListener<ArrayList<VKMessage>>
    ) {
        TaskManager.execute {
            VKApi.messages()
                .getHistory()
                .peerId(peerId)
                .reversed(false)
                .extended(true)
                .fields(VKUser.DEFAULT_FIELDS + "," + VKGroup.DEFAULT_FIELDS)
                .offset(offset)
                .count(count)
                .executeArray(
                    VKMessage::class.java,
                    object : OnResponseListener<ArrayList<VKMessage>> {
                        override fun onResponse(response: ArrayList<VKMessage>) {
                            TaskManager.execute {
                                cacheLoadedMessages(response)

                                MemoryCache.putUsers(VKMessage.profiles)
                                MemoryCache.putGroups(VKMessage.groups)
                                MemoryCache.putConversations(VKMessage.conversations)

                                VKUtil.sortMessagesByDate(response, false)

                                sendResponse(listener, response)
                            }
                        }

                        override fun onError(t: Throwable) {
                            sendError(listener, t)
                        }
                    })
        }
    }

    fun getCachedMessages(
        peerId: Int, offset: Int, count: Int,
        listener: MvpOnLoadListener<ArrayList<VKMessage>>
    ) {
        TaskManager.execute {
            val messages = MemoryCache.getMessagesByPeerId(peerId).asArrayList()

            if (messages.isEmpty()) {
                sendError(listener, NullPointerException("Messages is empty"))
                return@execute
            }

            VKUtil.sortMessagesByDate(messages, false)

            val preparedMessages = ArrayUtils.cut(messages, offset, count)

            sendResponseArray(listener, preparedMessages)
        }
    }

    fun getCachedConversation(peerId: Int, listener: MvpOnLoadListener<VKConversation>) {
        TaskManager.execute {
            val conversation = MemoryCache.getConversationById(peerId)

            if (conversation == null) {
                sendError(
                    listener,
                    NullPointerException("Conversation is not cached at the moment")
                )
            } else {
                sendResponse(listener, conversation)
            }
        }
    }

    fun loadConversation(peerId: Int, listener: MvpOnLoadListener<VKConversation>) {
        TaskManager.loadConversation(
            VKApiKeys.UPDATE_CONVERSATION,
            peerId,
            object : OnResponseListener<VKConversation> {
                override fun onResponse(response: VKConversation) {
                    sendResponse(listener, response)
                }

                override fun onError(t: Throwable) {
                    sendError(listener, t)
                }
            })
    }

    fun getChatInfo(conversation: VKConversation, listener: MvpOnLoadListener<String>) {
        when (conversation.type) {
            VKConversation.TYPE_CHAT -> {
                sendResponse(
                    listener,
                    AppGlobal.resources.getString(
                        if (conversation.isGroupChannel)
                            R.string.group_channel_members
                        else R.string.chat_members,
                        conversation.membersCount
                    )
                )
            }
            VKConversation.TYPE_USER -> {
                val user = VKUtil.searchUser(conversation.conversationId,
                    object : OnResponseListener<VKUser> {
                        override fun onResponse(response: VKUser) {
                            sendResponse(listener, VKUtil.getUserOnline(response))
                        }

                        override fun onError(t: Throwable) {
                            sendError(listener, t)
                        }
                    })

                user?.let {
                    sendResponse(listener, VKUtil.getUserOnline(it))
                }
            }
            else -> {
                sendResponse(listener, "")
            }
        }
    }

    fun sendMessage(
        peerId: Int,
        message: String,
        randomId: Int,
        listener: MvpOnLoadListener<Int>
    ) {
        TaskManager.execute {
            VKApi.messages()
                .send()
                .peerId(peerId)
                .message(message)
                .randomId(randomId)
                .executeArray(Int::class.java, object : OnResponseListener<ArrayList<Int>> {
                    override fun onResponse(response: ArrayList<Int>) {
                        val messageId = response[0]
                        sendResponse(listener, messageId)
                    }

                    override fun onError(t: Throwable) {
                        sendError(listener, t)
                    }
                })
        }
    }

    private fun cacheLoadedMessages(messages: ArrayList<VKMessage>) {
        MemoryCache.putMessages(messages)
    }
}