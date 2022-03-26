package com.meloda.fast.screens.conversations

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
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
import com.meloda.fast.common.Screens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationsViewModel @Inject constructor(
    private val conversations: ConversationsDataSource,
    private val users: UsersDataSource,
    updatesParser: LongPollUpdatesParser,
    private val router: Router
) : BaseViewModel() {

    init {
        updatesParser.onNewMessage {
            viewModelScope.launch { handleNewMessage(it) }
        }

        updatesParser.onMessageEdited {
            viewModelScope.launch { handleEditedMessage(it) }
        }

        updatesParser.onMessageIncomingRead {
            viewModelScope.launch { handleReadIncomingMessage(it) }
        }

        updatesParser.onMessageOutgoingRead {
            viewModelScope.launch { handleReadOutgoingMessage(it) }
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

                    val conversations = response.items.map { items ->
                        items.conversation.asVkConversation(
                            items.lastMessage?.asVkMessage()
                        )
                    }

                    val avatars = conversations.mapNotNull { conversation ->
                        VkUtils.getConversationAvatar(
                            conversation,
                            if (conversation.isUser()) profiles[conversation.id] else null,
                            if (conversation.isGroup()) groups[conversation.id] else null
                        )
                    }

                    sendEvent(
                        ConversationsLoadedEvent(
                            count = response.count,
                            offset = offset,
                            unreadCount = response.unreadCount ?: 0,
                            conversations = conversations,
                            profiles = profiles,
                            groups = groups,
                            avatars = avatars
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
            MessagesNewEvent(
                message = event.message,
                profiles = event.profiles,
                groups = event.groups
            )
        )
    }

    private suspend fun handleEditedMessage(event: LongPollEvent.VkMessageEditEvent) {
        sendEvent(MessagesEditEvent(event.message))
    }

    private suspend fun handleReadIncomingMessage(event: LongPollEvent.VkMessageReadIncomingEvent) {
        sendEvent(MessagesReadEvent(false, event.peerId, event.messageId))
    }

    private suspend fun handleReadOutgoingMessage(event: LongPollEvent.VkMessageReadOutgoingEvent) {
        sendEvent(MessagesReadEvent(true, event.peerId, event.messageId))
    }

    fun openRootScreen() {
        router.exit()
        router.newRootScreen(Screens.Main())
    }

    fun openMessagesHistoryScreen(bundle: Bundle) {
        router.navigateTo(Screens.MessagesHistory(bundle))
    }
}

data class ConversationsLoadedEvent(
    val count: Int,
    val offset: Int?,
    val unreadCount: Int?,
    val conversations: List<VkConversation>,
    val profiles: HashMap<Int, VkUser>,
    val groups: HashMap<Int, VkGroup>,
    val avatars: List<String>? = null
) : VkEvent()

data class ConversationsDeleteEvent(val peerId: Int) : VkEvent()

data class ConversationsPinEvent(val peerId: Int) : VkEvent()

data class ConversationsUnpinEvent(val peerId: Int) : VkEvent()

data class MessagesNewEvent(
    val message: VkMessage,
    val profiles: HashMap<Int, VkUser>,
    val groups: HashMap<Int, VkGroup>
) : VkEvent()

data class MessagesEditEvent(val message: VkMessage) : VkEvent()

data class MessagesReadEvent(val isOut: Boolean, val peerId: Int, val messageId: Int) : VkEvent()