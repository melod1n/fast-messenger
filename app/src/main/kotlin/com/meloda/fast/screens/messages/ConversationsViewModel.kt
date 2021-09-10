package com.meloda.fast.screens.messages

import androidx.lifecycle.viewModelScope
import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.model.base.BaseVkConversation
import com.meloda.fast.api.model.base.BaseVkMessage
import com.meloda.fast.api.network.repo.ConversationsRepo
import com.meloda.fast.api.network.request.ConversationsGetRequest
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
    private val repo: ConversationsRepo
) : BaseViewModel() {

    fun loadConversations() = viewModelScope.launch(Dispatchers.Default) {
        makeJob({
            repo.getAllChats(
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
                            messages = response.items.map { items -> items.lastMessage },
                            conversations = response.items.map { items -> items.conversation }
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
}

data class ConversationsLoaded(
    val count: Int,
    val unreadCount: Int,
    val messages: List<BaseVkMessage>,
    val conversations: List<BaseVkConversation>
) : VKEvent()