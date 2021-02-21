package com.meloda.fast.fragment.ui.repository

import com.meloda.fast.api.VKApi
import com.meloda.fast.api.model.VKConversation
import com.meloda.fast.api.model.VKMessage
import com.meloda.fast.api.model.VKUser
import com.meloda.fast.api.util.VKUtil
import com.meloda.fast.common.TaskManager
import com.meloda.fast.database.MemoryCache
import com.meloda.fast.extensions.ArrayExtensions.asArrayList
import com.meloda.fast.listener.OnResponseListener
import com.meloda.mvp.MvpOnLoadListener
import com.meloda.mvp.MvpRepository

class ConversationsRepositoryDeprecated : MvpRepository<VKConversation>() {

    fun loadConversations(
        offset: Int, count: Int,
        listener: MvpOnLoadListener<ArrayList<VKConversation>>
    ) {
        TaskManager.execute {
            VKApi.messages()
                .getConversations()
                .filter("all")
                .extended(true)
                .fields(VKUser.DEFAULT_FIELDS)
                .offset(offset)
                .count(count)
                .executeArray(VKConversation::class.java,
                    object : OnResponseListener<ArrayList<VKConversation>> {
                        override fun onResponse(response: ArrayList<VKConversation>) {
                            TaskManager.execute {
                                cacheLoadedConversations(response)

                                MemoryCache.putUsers(VKConversation.profiles)
                                MemoryCache.putGroups(VKConversation.groups)

                                sendResponse(listener, response)
                            }
                        }

                        override fun onError(t: Throwable) {
                            sendError(listener, t)
                        }
                    })
        }
    }

    fun getCachedConversations(
        offset: Int, count: Int,
        listener: MvpOnLoadListener<ArrayList<VKConversation>>
    ) {
        if (true) {
            sendResponse(listener, arrayListOf())
            return
        }
        TaskManager.execute {
            val conversations = MemoryCache.getConversations().asArrayList()

            VKUtil.sortConversationsByDate(conversations, true)

            sendResponse(listener, conversations)
        }
    }

    private fun fillConversationsWithProfilesAndGroups(conversations: ArrayList<VKConversation>) {
        for (conversation in conversations) {
            val lastMessage = conversation.lastMessage

            when (conversation.type) {
                VKConversation.TYPE_USER -> {
                    VKUtil.searchUser(conversation.conversationId)?.let {
                        conversation.peerUser = it
                    }
                }

                VKConversation.TYPE_GROUP -> {
                    VKUtil.searchGroup(conversation.conversationId)?.let {
                        conversation.peerGroup = it
                    }
                }
            }

            if (lastMessage.isFromGroup()) {
                VKUtil.searchGroup(lastMessage.fromId)?.let {
                    lastMessage.fromGroup = it
                }
            } else {
                VKUtil.searchUser(lastMessage.fromId)?.let {
                    lastMessage.fromUser = it
                }
            }
        }
    }

    private fun cacheLoadedConversations(conversations: List<VKConversation>) {
        val messages = arrayListOf<VKMessage>()

        for (conversation in conversations) {
            messages.add(conversation.lastMessage)
        }

        MemoryCache.putMessages(messages)
        MemoryCache.putConversations(conversations)
    }
}
