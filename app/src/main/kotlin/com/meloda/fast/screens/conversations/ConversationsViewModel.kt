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
import com.meloda.fast.api.model.base.BaseVkGroup
import com.meloda.fast.api.model.base.BaseVkUser
import com.meloda.fast.api.model.data.BaseVkConversation
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
import com.meloda.fast.ext.toMap
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

    private val dataConversations: MutableStateFlow<List<BaseVkConversation>> =
        MutableStateFlow(emptyList())

    val domainConversations: MutableStateFlow<List<VkConversationDomain>> =
        MutableStateFlow(emptyList())

    val uiConversations: MutableStateFlow<List<VkConversationUi>> = MutableStateFlow(emptyList())

    val pinnedConversationsCount = domainConversations.map { conversations ->
        val pinnedConversations = conversations.filter { it.isPinned() }
        pinnedConversations.size
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

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

        updatesParser.onConversationPinStateChanged {
            viewModelScope.launch { handlePinStateChanged(it) }
        }
    }

    fun loadConversations(offset: Int? = null) {
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
                    val dataConversationsMessages = response.items.map { item ->
                        item.conversation to item.lastMessage
                    }
                    val dataConversationsList = dataConversationsMessages.map { pair -> pair.first }
                    dataConversations.update { dataConversationsList }

                    val messages =
                        dataConversationsMessages
                            .map { pair -> pair.second }
                            .mapNotNull { message -> message?.asVkMessage() }

                    withContext(Dispatchers.IO) {
                        messagesRepository.store(messages)
                    }

                    val newProfiles = response.profiles
                        ?.map(BaseVkUser::mapToDomain)
                        ?.toMap(hashMapOf(), VkUser::id) ?: hashMapOf()
                    profiles.update { newProfiles }

                    val newGroups = response.groups
                        ?.map(BaseVkGroup::mapToDomain)
                        ?.toMap(hashMapOf(), VkGroup::id) ?: hashMapOf()
                    groups.update { newGroups }

                    val domainConversationsList = dataConversationsList.mapToDomain()
                    emitConversations(domainConversationsList)
                }
            }
        )
    }

    private suspend fun emitConversations(conversations: List<VkConversationDomain>) =
        withContext(Dispatchers.Default) {
            domainConversations.emit(conversations)

            val uiConversationsList =
                conversations.map(VkConversationDomain::mapToPresentation)

            uiConversations.emit(uiConversationsList)
        }

    private suspend fun List<BaseVkConversation>.mapToDomain(): List<VkConversationDomain> =
        this.map { baseConversation -> getFilledDomainVkConversation(baseConversation) }

    private suspend fun VkConversationDomain.fill(): VkConversationDomain {
        val conversation = this
        val messages = messagesRepository.getCached(conversation.id)

        val lastMessage = messages.find { it.id == conversation.lastMessageId }
        conversation.lastMessage = lastMessage

        val userGroup =
            VkUtils.getConversationUserGroup(
                conversation,
                profiles.value,
                groups.value
            )
        val actionUserGroup =
            VkUtils.getMessageActionUserGroup(
                lastMessage,
                profiles.value,
                groups.value
            )
        val messageUserGroup =
            VkUtils.getMessageUserGroup(
                lastMessage,
                profiles.value,
                groups.value
            )

        conversation.conversationUser = userGroup.first
        conversation.conversationGroup = userGroup.second
        conversation.action = lastMessage?.getPreparedAction()
        conversation.actionUser = actionUserGroup.first
        conversation.actionGroup = actionUserGroup.second
        conversation.messageUser = messageUserGroup.first
        conversation.messageGroup = messageUserGroup.second

        return conversation
    }

    private suspend fun getFilledDomainVkConversation(
        baseConversation: BaseVkConversation,
        defDomainConversation: VkConversationDomain? = null,
    ): VkConversationDomain {
        val conversation = defDomainConversation ?: baseConversation.mapToDomain()
        return conversation.fill()
    }

    fun loadProfileUser() = viewModelScope.launch {
        makeJob({ usersRepository.getById(UsersGetRequest(fields = VKConstants.USER_FIELDS)) },
            onAnswer = {
                it.response?.let { r ->
                    val users = r.map { u -> u.mapToDomain() }
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
                onAnswer = { handleConversationPinStateUpdate(peerId, true) }
            )
        } else {
            makeJob(
                { conversationsRepository.unpin(ConversationsUnpinRequest(peerId)) },
                onAnswer = { handleConversationPinStateUpdate(peerId, false) }
            )
        }
    }

    // TODO: 07.01.2023, Danil Nikolaev: handle major AND minor id
    private suspend fun handleConversationPinStateUpdate(peerId: Int, pin: Boolean) =
        withContext(Dispatchers.IO) {
            val conversationsList = domainConversations.value.toMutableList()
            val conversationIndex =
                conversationsList.findIndex { it.id == peerId } ?: return@withContext

            val conversation = conversationsList[conversationIndex].copy(
                majorId = if (pin) (pinnedConversationsCount.value + 1) * 16
                else 0
            ).fill()

            conversationsList.removeAt(conversationIndex)

            if (pin) {
                conversationsList.add(0, conversation)
            } else {
                conversationsList.add(pinnedConversationsCount.value - 1, conversation)
            }

            emitConversations(conversationsList)
        }

    private suspend fun handleNewMessage(event: LongPollEvent.VkMessageNewEvent) =
        withContext(Dispatchers.IO) {
            val message = event.message

            messagesRepository.store(message)

            val newProfiles: HashMap<Int, VkUser> =
                (profiles.value + event.profiles) as HashMap<Int, VkUser>
            profiles.update { newProfiles }

            val newGroups: HashMap<Int, VkGroup> =
                (groups.value + event.groups) as HashMap<Int, VkGroup>
            groups.update { newGroups }

            val dataConversationsList = domainConversations.value.toMutableList()
            val dataConversationIndex = dataConversationsList.findIndex { it.id == message.peerId }

            if (dataConversationIndex == null) { // диалога нет в списке
                // pizdets
            } else {
                val dataConversation = dataConversationsList[dataConversationIndex]
                var newConversation = dataConversation.copy(
                    lastMessageId = message.id,
                    lastConversationMessageId = -1
                ).fill().also {
                    it.lastMessage = message
                }
                if (!message.isOut) {
                    newConversation = newConversation.copy(
                        unreadCount = newConversation.unreadCount + 1
                    ).fill().also {
                        it.lastMessage = message
                    }
                }

                if (dataConversation.isPinned()) {
                    dataConversationsList[dataConversationIndex] = newConversation
                    emitConversations(dataConversationsList)
                    return@withContext
                }

                dataConversationsList.removeAt(dataConversationIndex)

                val toPosition = pinnedConversationsCount.value
                dataConversationsList.add(toPosition, newConversation)

                emitConversations(dataConversationsList)
            }
        }

    private suspend fun handleEditedMessage(event: LongPollEvent.VkMessageEditEvent) =
        withContext(Dispatchers.IO) {
            val message = event.message

            messagesRepository.store(message)

            val conversationsList = domainConversations.value.toMutableList()

            val conversationIndex = conversationsList.findIndex { it.id == message.peerId }
            if (conversationIndex == null) { // диалога нет в списке

            } else {
                val conversation = conversationsList[conversationIndex]
                conversationsList[conversationIndex] = conversation.copy(
                    lastMessageId = message.id,
                    lastConversationMessageId = -1
                ).fill().also {
                    it.lastMessage = message
                }

                emitConversations(conversationsList)
            }
        }

    private suspend fun handleReadIncomingMessage(event: LongPollEvent.VkMessageReadIncomingEvent) =
        withContext(Dispatchers.IO) {
            val conversationsList = domainConversations.value.toMutableList()

            val conversationIndex =
                conversationsList.findIndex { it.id == event.peerId } ?: return@withContext

            var conversation = conversationsList[conversationIndex]
            conversation = conversation.copy(
                inRead = event.messageId,
                unreadCount = event.unreadCount
            ).fill()

            conversationsList[conversationIndex] = conversation

            emitConversations(conversationsList)
        }

    private suspend fun handleReadOutgoingMessage(event: LongPollEvent.VkMessageReadOutgoingEvent) =
        withContext(Dispatchers.IO) {
            val conversationsList = domainConversations.value.toMutableList()

            val conversationIndex =
                conversationsList.findIndex { it.id == event.peerId } ?: return@withContext

            var conversation = conversationsList[conversationIndex]
            conversation = conversation.copy(
                outRead = event.messageId,
                unreadCount = event.unreadCount
            ).fill()

            conversationsList[conversationIndex] = conversation

            emitConversations(conversationsList)
        }

    // TODO: 07.01.2023, Danil Nikolaev: handle major AND minor id
    private suspend fun handlePinStateChanged(event: LongPollEvent.VkConversationPinStateChangedEvent) =
        withContext(Dispatchers.IO) {
            val conversationsList = domainConversations.value.toMutableList()

            val conversationIndex =
                conversationsList.findIndex { it.id == event.peerId } ?: return@withContext

            val pin = event.majorId > 0

            var conversation = conversationsList[conversationIndex]
            conversation = conversation.copy(
                majorId = event.majorId
            ).fill()

            conversationsList.removeAt(conversationIndex)

            if (pin) {
                conversationsList.add(0, conversation)
            } else {
                conversationsList.add(pinnedConversationsCount.value - 1, conversation)
            }

            emitConversations(conversationsList)
        }


    fun openRootScreen() {
        router.replaceScreen(Screens.Main())
    }

    fun openMessagesHistoryScreen(
        conversation: VkConversationDomain,
        user: VkUser?,
        group: VkGroup?,
    ) {
        router.navigateTo(Screens.MessagesHistory(conversation, user, group))
    }

    fun readConversation(conversation: VkConversationDomain) {
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
    val conversations: List<VkConversationDomain>,
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
