package com.meloda.fast.fragment.ui.repository

import com.meloda.concurrent.TaskManager
import com.meloda.mvp.MvpOnResponseListener
import com.meloda.mvp.MvpRepository
import com.meloda.vksdk.OnResponseListener
import com.meloda.vksdk.VKApi
import com.meloda.vksdk.VKConstants
import com.meloda.vksdk.model.VKConversation
import com.meloda.vksdk.model.VKMessage

class ConversationsRepositoryDeprecated : MvpRepository<VKConversation>() {

    fun loadConversations(
        offset: Int, count: Int,
        listener: MvpOnResponseListener<ArrayList<VKConversation>>
    ) {
        TaskManager.execute {
            VKApi.messages()
                .getConversations()
                .filter("all")
                .extended(true)
                .fields(VKConstants.USER_FIELDS)
                .offset(offset)
                .count(count)
                .executeArray(VKConversation::class.java,
                    object : OnResponseListener<ArrayList<VKConversation>> {
                        override fun onResponse(response: ArrayList<VKConversation>) {
                            TaskManager.execute {
                                cacheLoadedConversations(response)

//                                MemoryCache.putUsers(VKConversation.profiles)
//                                MemoryCache.putGroups(VKConversation.groups)

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
        listener: MvpOnResponseListener<ArrayList<VKConversation>>
    ) {
        if (true) {
            sendResponse(listener, arrayListOf())
            return
        }
        TaskManager.execute {
//            val conversations = MemoryCache.getConversations().asArrayList()
//
//            VKUtil.sortConversationsByDate(conversations, true)

//            sendResponse(listener, conversations)
        }
    }

    private fun fillConversationsWithProfilesAndGroups(conversations: ArrayList<VKConversation>) {
        for (conversation in conversations) {
            val lastMessage = conversation.lastMessage

            when (conversation.type) {
                VKConversation.Type.USER -> {
//                    VKUtil.searchUser(conversation.conversationId)?.let {
//                        conversation.peerUser = it
//                    }
                }

                VKConversation.Type.GROUP -> {
//                    VKUtil.searchGroup(conversation.conversationId)?.let {
//                        conversation.peerGroup = it
//                    }
                }
            }

            if (lastMessage.isFromGroup()) {
//                VKUtil.searchGroup(lastMessage.fromId)?.let {
//                    lastMessage.fromGroup = it
//                }
            } else {
//                VKUtil.searchUser(lastMessage.fromId)?.let {
//                    lastMessage.fromUser = it
//                }
            }
        }
    }

    private fun cacheLoadedConversations(conversations: List<VKConversation>) {
        val messages = arrayListOf<VKMessage>()

        for (conversation in conversations) {
            messages.add(conversation.lastMessage)
        }

//        MemoryCache.putMessages(messages)
//        MemoryCache.putConversations(conversations)
    }
}
