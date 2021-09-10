package com.meloda.fast.screens.messages

import androidx.lifecycle.viewModelScope
import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.datasource.ConversationsDataSource
import com.meloda.fast.api.datasource.UsersDataSource
import com.meloda.fast.api.model.VkConversation
import com.meloda.fast.api.network.request.ConversationsGetRequest
import com.meloda.fast.api.network.request.UsersGetRequest
import com.meloda.fast.base.viewmodel.BaseViewModel
import com.meloda.fast.base.viewmodel.StartProgressEvent
import com.meloda.fast.base.viewmodel.StopProgressEvent
import com.meloda.fast.base.viewmodel.VKEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationsViewModel @Inject constructor(
    private val dataSource: ConversationsDataSource,
    private val usersDataSource: UsersDataSource
) : BaseViewModel() {

    fun loadConversations() = viewModelScope.launch(Dispatchers.Default) {
        makeJob({
            dataSource.getAllChats(
                ConversationsGetRequest(
                    count = 30,
                    fields = "${VKConstants.USER_FIELDS},${VKConstants.GROUP_FIELDS}"
                )
            )
        },
            onAnswer = {
                it.response?.let { response ->
                    sendEvent(
                        ConversationsLoaded(
                            count = response.count,
                            unreadCount = response.unreadCount ?: 0,
                            conversations = response.items.map { items ->
                                items.conversation.asVkConversation(
                                    items.lastMessage.asVkMessage()
                                )
                            }
                        )
                    )
                }
            },
            onError = {
                val er = it
                val i = 0
            },
            onStart = {
                sendEvent(StartProgressEvent)
            },
            onEnd = {
                sendEvent(StopProgressEvent)
            })
    }

    fun loadSomeUsers(usersIds: List<Int>) = viewModelScope.launch {
        makeJob({
            usersDataSource.getById(
                UsersGetRequest(
                    usersIds = usersIds,
                    fields = "sex"
                )
            )
        },
            onAnswer = {
                val argh = it
                val i = 0
                it.response?.let { r ->
                    val users = r.map { user -> user.asVkUser() }

                    usersDataSource.storeUsers(users)
                }
            },
            onError = {
                val e = it
                val i = 0
            })

    }
}

data class ConversationsLoaded(
    val count: Int,
    val unreadCount: Int,
    val conversations: List<VkConversation>
) : VKEvent()