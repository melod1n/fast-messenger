package com.meloda.fast.screens.conversations

import androidx.lifecycle.viewModelScope
import com.meloda.fast.api.*
import com.meloda.fast.api.model.VkConversation
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.network.conversations.*
import com.meloda.fast.api.network.users.UsersDataSource
import com.meloda.fast.api.network.users.UsersGetRequest
import com.meloda.fast.base.viewmodel.BaseViewModel
import com.meloda.fast.base.viewmodel.VkEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ConversationsViewModel @Inject constructor(
    private val conversations: ConversationsDataSource,
    private val users: UsersDataSource,
    updatesParser: LongPollUpdatesParser
) : BaseViewModel() {

    companion object {
        private const val TAG = "ConversationsViewModel"
    }

    init {
        updatesParser.onNewMessage {
            viewModelScope.launch { handleNewMessage(it) }
        }

        updatesParser.onMessageEdited {
            viewModelScope.launch { handleEditedMessage(it) }
        }
    }

    fun loadConversations(
        offset: Int? = null
    ) = viewModelScope.launch(Dispatchers.Default) {
        makeJob({
            conversations.get(
                ConversationsGetRequest(
                    count = 30,
                    extended = true,
                    offset = offset,
                    fields = VKConstants.ALL_FIELDS
                )
            )
        },
            onAnswer = {
                it.response?.let { response ->
                    val profiles = hashMapOf<Int, VkUser>()
                    response.profiles?.forEach { baseUser ->
                        baseUser.asVkUser().let { user -> profiles[user.id] = user }
                    }

                    val groups = hashMapOf<Int, VkGroup>()
                    response.groups?.forEach { baseGroup ->
                        baseGroup.asVkGroup().let { group -> groups[group.id] = group }
                    }

                    sendEvent(
                        ConversationsLoadedEvent(
                            count = response.count,
                            offset = offset,
                            unreadCount = response.unreadCount ?: 0,
                            conversations = response.items.map { items ->
                                items.conversation.asVkConversation(
                                    items.lastMessage?.asVkMessage()
                                )
                            },
                            profiles = profiles,
                            groups = groups
                        )
                    )
                }
            }
        )
    }

    fun loadProfileUser() = viewModelScope.launch {
        makeJob({ users.getById(UsersGetRequest(fields = VKConstants.USER_FIELDS)) },
            onAnswer = {
                it.response?.let { r ->
                    val users = r.map { u -> u.asVkUser() }
                    this@ConversationsViewModel.users.storeUsers(users)

                    UserConfig.vkUser.value = users[0]
                }
            })
    }

    fun deleteConversation(peerId: Int) = viewModelScope.launch {
        makeJob({
            conversations.delete(
                ConversationsDeleteRequest(peerId)
            )
        }, onAnswer = { sendEvent(ConversationsDeleteEvent(peerId)) })
    }

    fun pinConversation(
        peerId: Int,
        pin: Boolean
    ) = viewModelScope.launch {
        if (pin) {
            makeJob(
                { conversations.pin(ConversationsPinRequest(peerId)) },
                onAnswer = { sendEvent(ConversationsPinEvent(peerId)) }
            )
        } else {
            makeJob(
                { conversations.unpin(ConversationsUnpinRequest(peerId)) },
                onAnswer = { sendEvent(ConversationsUnpinEvent(peerId)) }
            )
        }
    }

    private suspend fun handleNewMessage(event: LongPollEvent.VkMessageNewEvent) {
        sendEvent(
            MessageNewEvent(
                message = event.message,
                profiles = event.profiles,
                groups = event.groups
            )
        )
    }

    private suspend fun handleEditedMessage(event: LongPollEvent.VkMessageEditEvent) {
        sendEvent(MessageEditEvent(event.message))
    }
}

data class ConversationsLoadedEvent(
    val count: Int,
    val offset: Int?,
    val unreadCount: Int?,
    val conversations: List<VkConversation>,
    val profiles: HashMap<Int, VkUser>,
    val groups: HashMap<Int, VkGroup>
) : VkEvent()

data class ConversationsDeleteEvent(val peerId: Int) : VkEvent()

data class ConversationsPinEvent(val peerId: Int) : VkEvent()

data class ConversationsUnpinEvent(val peerId: Int) : VkEvent()

data class MessageNewEvent(
    val message: VkMessage,
    val profiles: HashMap<Int, VkUser>,
    val groups: HashMap<Int, VkGroup>
) : VkEvent()

data class MessageEditEvent(val message: VkMessage) : VkEvent()