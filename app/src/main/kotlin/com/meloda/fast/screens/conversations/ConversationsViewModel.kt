package com.meloda.fast.screens.conversations

import androidx.lifecycle.viewModelScope
import com.github.terrakok.cicerone.Router
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.VkUtils
import com.meloda.fast.api.longpoll.LongPollEvent
import com.meloda.fast.api.longpoll.LongPollUpdatesParser
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.model.data.VkConversation
import com.meloda.fast.api.model.domain.VkConversationDomain
import com.meloda.fast.api.model.presentation.VkConversationUi
import com.meloda.fast.api.network.conversations.ConversationsDeleteRequest
import com.meloda.fast.api.network.conversations.ConversationsGetRequest
import com.meloda.fast.api.network.conversations.ConversationsPinRequest
import com.meloda.fast.api.network.conversations.ConversationsUnpinRequest
import com.meloda.fast.api.network.users.UsersGetRequest
import com.meloda.fast.base.viewmodel.BaseViewModel
import com.meloda.fast.base.viewmodel.VkEvent
import com.meloda.fast.common.Screens
import com.meloda.fast.data.conversations.ConversationsRepository
import com.meloda.fast.data.messages.MessagesRepository
import com.meloda.fast.data.users.UsersRepository
import com.meloda.fast.ext.findIndex
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ConversationsViewModel @Inject constructor(
    private val conversationsRepository: ConversationsRepository,
    private val usersRepository: UsersRepository,
    updatesParser: LongPollUpdatesParser,
    private val router: Router,
    private val messagesRepository: MessagesRepository,
) : BaseViewModel() {

    val profiles: MutableStateFlow<HashMap<Int, VkUser>> = MutableStateFlow(hashMapOf())

    val groups: MutableStateFlow<HashMap<Int, VkGroup>> = MutableStateFlow(hashMapOf())

    val dataConversations: MutableStateFlow<List<VkConversation>> = MutableStateFlow(emptyList())

    val uiConversations: MutableStateFlow<List<VkConversationUi>> = MutableStateFlow(emptyList())

    val pinnedConversationsCount = dataConversations.map { conversations ->
        val pinnedConversations = conversations.filter { it.isPinned() }
        pinnedConversations.size
    }.stateIn(viewModelScope, SharingStarted.Lazily, -1)

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
        offset: Int? = null,
    ) = viewModelScope.launch(Dispatchers.IO) {
        makeJob({
            conversationsRepository.get(
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
                    this@ConversationsViewModel.profiles.update { profiles }

                    val groups = hashMapOf<Int, VkGroup>()
                    response.groups?.forEach { baseGroup ->
                        baseGroup.asVkGroup().let { group -> groups[group.id] = group }
                    }
                    this@ConversationsViewModel.groups.update { groups }

                    val conversations = response.items.map { items ->
                        items.conversation.asVkConversation(
                            items.lastMessage?.asVkMessage()
                        )
                    }
                    dataConversations.emit(conversations)

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

                    prepareConversations(conversations)
                }
            }
        )
    }

    private suspend fun prepareConversations(conversations: List<VkConversation>) =
        withContext(Dispatchers.Default) {
            val newConversations = conversations
                .mapToDomain()
                .map(VkConversationDomain::mapToPresentation)

            uiConversations.emit(newConversations)
        }

    @Suppress("UNCHECKED_CAST")
    private fun List<*>.mapToDomain(): List<VkConversationDomain> {
        val list = this as List<VkConversation>

        return list.map { conversation ->
            val userGroup =
                VkUtils.getConversationUserGroup(
                    conversation,
                    profiles.value,
                    groups.value
                )
            val actionUserGroup =
                VkUtils.getMessageActionUserGroup(
                    conversation.lastMessage,
                    profiles.value,
                    groups.value
                )
            val messageUserGroup =
                VkUtils.getMessageUserGroup(
                    conversation.lastMessage,
                    profiles.value,
                    groups.value
                )

            conversation.mapToDomain(
                userGroup.first,
                userGroup.second,
                actionUserGroup.first,
                actionUserGroup.second,
                messageUserGroup.first,
                messageUserGroup.second
            )
        }
    }

    fun loadProfileUser() = viewModelScope.launch {
        makeJob({ usersRepository.getById(UsersGetRequest(fields = VKConstants.USER_FIELDS)) },
            onAnswer = {
                it.response?.let { r ->
                    val users = r.map { u -> u.asVkUser() }
                    this@ConversationsViewModel.usersRepository.storeUsers(users)

                    UserConfig.vkUser.value = users[0]
                }
            })
    }

    fun deleteConversation(peerId: Int) = viewModelScope.launch {
        makeJob({
            conversationsRepository.delete(
                ConversationsDeleteRequest(peerId)
            )
        }, onAnswer = { sendEvent(ConversationsDeleteEvent(peerId)) })
    }

    fun pinConversation(
        peerId: Int,
        pin: Boolean,
    ) = viewModelScope.launch {
        if (pin) {
            makeJob(
                { conversationsRepository.pin(ConversationsPinRequest(peerId)) },
                onAnswer = { sendEvent(ConversationsPinEvent(peerId)) }
            )
        } else {
            makeJob(
                { conversationsRepository.unpin(ConversationsUnpinRequest(peerId)) },
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

        val message = event.message

        val newProfiles: HashMap<Int, VkUser> =
            (profiles.value + event.profiles) as HashMap<Int, VkUser>
        profiles.update { newProfiles }

        val newGroups: HashMap<Int, VkGroup> =
            (groups.value + event.groups) as HashMap<Int, VkGroup>
        groups.update { newGroups }

        val dataConversationsList = dataConversations.value.toMutableList()
        val dataConversationIndex = dataConversationsList.findIndex { it.id == message.peerId }

        if (dataConversationIndex == null) { // диалога нет в списке
            // pizdets
        } else {
            val dataConversation = dataConversationsList[dataConversationIndex]
            val newConversation = dataConversation.copy(
                lastMessage = message,
                lastMessageId = message.id,
                lastConversationMessageId = -1
            )
            if (!message.isOut) {
                newConversation.unreadCount += 1
            }

            if (dataConversation.isPinned()) {
                dataConversationsList[dataConversationIndex] = newConversation
                prepareConversations(dataConversationsList)
                return
            }

            dataConversationsList.removeAt(dataConversationIndex)

            val toPosition = pinnedConversationsCount.value
            dataConversationsList.add(toPosition, newConversation)

            prepareConversations(dataConversationsList)
        }
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
        router.replaceScreen(Screens.Main())
    }

    fun openMessagesHistoryScreen(
        conversation: VkConversation,
        user: VkUser?,
        group: VkGroup?,
    ) {
        router.navigateTo(Screens.MessagesHistory(conversation, user, group))
    }

    fun readConversation(conversation: VkConversation) {
        makeJob(
            {
                messagesRepository.markAsRead(
                    conversation.id,
                    startMessageId = conversation.lastMessageId
                )
            },
            onAnswer = {
                sendEvent(MessagesReadEvent(false, conversation.id, conversation.lastMessageId))
            }
        )
    }
}

data class ConversationsLoadedEvent(
    val count: Int,
    val offset: Int?,
    val unreadCount: Int?,
    val conversations: List<VkConversation>,
    val profiles: HashMap<Int, VkUser>,
    val groups: HashMap<Int, VkGroup>,
    val avatars: List<String>? = null,
) : VkEvent()

data class ConversationsDeleteEvent(val peerId: Int) : VkEvent()

data class ConversationsPinEvent(val peerId: Int) : VkEvent()

data class ConversationsUnpinEvent(val peerId: Int) : VkEvent()

data class MessagesNewEvent(
    val message: VkMessage,
    val profiles: HashMap<Int, VkUser>,
    val groups: HashMap<Int, VkGroup>,
) : VkEvent()

data class MessagesEditEvent(val message: VkMessage) : VkEvent()

data class MessagesReadEvent(
    val isOut: Boolean,
    val peerId: Int,
    val messageId: Int,
) : VkEvent()
