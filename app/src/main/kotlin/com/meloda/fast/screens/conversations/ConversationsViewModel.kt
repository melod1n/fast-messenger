package com.meloda.fast.screens.conversations

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.imageLoader
import coil.request.ImageRequest
import com.google.common.collect.ImmutableList
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.VkGroupsMap
import com.meloda.fast.api.VkGroupsMap.Companion.toGroupsMap
import com.meloda.fast.api.VkUsersMap
import com.meloda.fast.api.VkUsersMap.Companion.toUsersMap
import com.meloda.fast.api.model.InteractionType
import com.meloda.fast.api.model.domain.VkConversationDomain
import com.meloda.fast.api.model.presentation.VkConversationUi
import com.meloda.fast.base.processState
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.data.account.domain.usecase.AccountUseCase
import com.meloda.fast.data.users.domain.UsersUseCase
import com.meloda.fast.ext.createTimerFlow
import com.meloda.fast.ext.emitOnScope
import com.meloda.fast.ext.findIndex
import com.meloda.fast.ext.findWithIndex
import com.meloda.fast.ext.listenValue
import com.meloda.fast.ext.setValue
import com.meloda.fast.screens.conversations.domain.usecase.ConversationsUseCase
import com.meloda.fast.screens.conversations.model.ConversationOption
import com.meloda.fast.screens.conversations.model.ConversationsScreenState
import com.meloda.fast.screens.conversations.model.ConversationsShowOptions
import com.meloda.fast.screens.messages.domain.usecase.MessagesUseCase
import com.meloda.fast.service.longpolling.LongPollEvent
import com.meloda.fast.service.longpolling.LongPollUpdatesParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

interface ConversationsViewModel {

    val screenState: StateFlow<ConversationsScreenState>

    fun onDeleteDialogDismissed()

    fun onDeleteDialogPositiveClick(conversationId: Int)

    fun onRefresh()

    fun onConversationItemLongClick(conversation: VkConversationUi)

    fun onPinDialogDismissed()
    fun onPinDialogPositiveClick(conversation: VkConversationUi)
    fun onOptionClicked(conversation: VkConversationUi, option: ConversationOption)
}

class ConversationsViewModelImpl(
    updatesParser: LongPollUpdatesParser,
    private val imageLoader: ImageLoader,
    private val accountUseCase: AccountUseCase,
    private val conversationsUseCase: ConversationsUseCase,
    private val usersUseCase: UsersUseCase,
    private val messagesUseCase: MessagesUseCase
) : ConversationsViewModel, ViewModel() {

    private val context: Context get() = AppGlobal.Instance

    override val screenState = MutableStateFlow(ConversationsScreenState.EMPTY)

    private val domainConversations = MutableStateFlow<List<VkConversationDomain>>(emptyList())

    private val pinnedConversationsCount = domainConversations.map { conversations ->
        conversations.filter { conversation -> conversation.isPinned() }.size
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    init {
        updatesParser.onNewMessage(::handleNewMessage)
        updatesParser.onMessageEdited(::handleEditedMessage)
        updatesParser.onMessageIncomingRead(::handleReadIncomingMessage)
        updatesParser.onMessageOutgoingRead(::handleReadOutgoingMessage)
        updatesParser.onConversationPinStateChanged(::handlePinStateChanged)
        updatesParser.onInteractions(::handleInteraction)

        loadProfileUser()
    }

    override fun onDeleteDialogDismissed() {
        emitShowOptions { old -> old.copy(showDeleteDialog = null) }
    }

    override fun onDeleteDialogPositiveClick(conversationId: Int) {
        deleteConversation(conversationId)
        hideOptions(conversationId)
    }

    override fun onRefresh() {
        loadConversations()
    }

    override fun onConversationItemLongClick(conversation: VkConversationUi) {
        val options = mutableListOf<ConversationOption>()
        if (!conversation.isExpanded) {
            conversation.lastMessage?.run {
                if (conversation.isUnread && !this.isOut) {
                    options += ConversationOption.MarkAsRead
                }
            }

            val conversationsSize = screenState.value.conversations.size
            val pinnedCount = pinnedConversationsCount.value

            val canPinOneMoreDialog =
                conversationsSize > 4 && pinnedCount < 5 && !conversation.isPinned

            if (conversation.isPinned) {
                options += ConversationOption.Unpin
            } else if (canPinOneMoreDialog) {
                options += ConversationOption.Pin
            }

            options += ConversationOption.Delete
        }

        screenState.setValue { old ->
            old.copy(
                conversations = old.conversations.map { item ->
                    item.copy(
                        isExpanded =
                        if (item.id == conversation.id) {
                            !item.isExpanded
                        } else {
                            false
                        },
                        options = ImmutableList.copyOf(options)
                    )
                }
            )
        }
    }

    override fun onPinDialogDismissed() {
        emitShowOptions { old -> old.copy(showPinDialog = null) }
    }

    override fun onPinDialogPositiveClick(conversation: VkConversationUi) {
        pinConversation(conversation.id, !conversation.isPinned)
        hideOptions(conversation.id)
    }

    override fun onOptionClicked(conversation: VkConversationUi, option: ConversationOption) {
        when (option) {
            ConversationOption.Delete -> {
                emitShowOptions { old ->
                    old.copy(showDeleteDialog = conversation.id)
                }
            }

            ConversationOption.MarkAsRead -> {
                readConversation(
                    peerId = conversation.id,
                    startMessageId = conversation.lastMessageId
                )
                hideOptions(conversation.id)
            }

            ConversationOption.Pin,
            ConversationOption.Unpin -> {
                emitShowOptions { old -> old.copy(showPinDialog = conversation) }
            }
        }
    }

    private fun hideOptions(conversationId: Int) {
        screenState.setValue { old ->
            old.copy(
                conversations = old.conversations.map { item ->
                    if (item.id == conversationId) {
                        item.copy(isExpanded = false)
                    } else item
                }
            )
        }
    }

    private fun emitDomainConversations(conversations: List<VkConversationDomain>) {
        val pinnedConversationsCount = conversations.filter(VkConversationDomain::isPinned).size

        domainConversations.emitOnScope { conversations }

        val usersMap = conversations.mapNotNull(VkConversationDomain::conversationUser).toUsersMap()
        val groupsMap =
            conversations.mapNotNull(VkConversationDomain::conversationGroup).toGroupsMap()

        val uiConversations = conversations.map { conversation ->
            conversation.mapToPresentation(
                usersMap = usersMap,
                groupsMap = groupsMap
            )
        }
        val avatars = uiConversations.mapNotNull { conversation ->
            conversation.avatar?.extractUrl()
        }

        avatars.forEach { avatar ->
            val request = ImageRequest.Builder(context)
                .data(avatar)
                .build()

            imageLoader.enqueue(request)
        }

        screenState.setValue { old ->
            old.copy(
                conversations = uiConversations,
                pinnedConversationsCount = pinnedConversationsCount,
            )
        }
    }

    private fun emitShowOptions(function: (ConversationsShowOptions) -> ConversationsShowOptions) {
        val newShowOptions = function.invoke(screenState.value.showOptions)
        screenState.setValue { old -> old.copy(showOptions = newShowOptions) }
    }

    private fun loadConversations(offset: Int? = null) {
        conversationsUseCase.getConversations(
            count = 30,
            offset = offset,
            fields = VKConstants.ALL_FIELDS,
            extended = true
        ).listenValue { state ->
            state.processState(
                error = { error -> },
                success = { response ->
                    val conversations = response.conversations

                    conversationsUseCase.storeConversations(conversations)
                    usersUseCase.storeUsers(response.profiles)
                    conversationsUseCase.storeGroups(response.groups)
                    messagesUseCase.storeMessages(response.messages)

                    val pinnedConversationsCount =
                        conversations.filter(VkConversationDomain::isPinned).size

                    domainConversations.emitOnScope { conversations }

                    val uiConversations = conversations.map {
                        it.mapToPresentation(
                            usersMap = VkUsersMap.forUsers(response.profiles),
                            groupsMap = VkGroupsMap.forGroups(response.groups)
                        )
                    }
                    val avatars = uiConversations.mapNotNull { conversation ->
                        conversation.avatar?.extractUrl()
                    }

                    avatars.forEach { avatar ->
                        val request = ImageRequest.Builder(context)
                            .data(avatar)
                            .build()

                        context.imageLoader.enqueue(request)
                    }

//                    conversationsRepository.store(conversations)

//                    messagesRepository.store(messages)

                    screenState.setValue { old ->
                        old.copy(
                            conversations = uiConversations,
                            pinnedConversationsCount = pinnedConversationsCount,
                        )
                    }
                }
            )
            screenState.emit(screenState.value.copy(isLoading = state.isLoading()))
        }
    }

    private fun loadProfileUser() {
        viewModelScope.launch(Dispatchers.IO) {
            accountUseCase.getAllAccounts().listenValue { accounts ->
                Log.d("ConversationsViewModel", "initUserConfig: accounts: $accounts")
                if (accounts.isNotEmpty()) {
                    val currentAccount = accounts.find { it.userId == UserConfig.currentUserId }
                    if (currentAccount != null) {
                        UserConfig.parse(currentAccount)
                    }
                }
            }

//            sendRequest(
//                request = {
//                    usersRepository.getById(UsersGetRequest(fields = VKConstants.USER_FIELDS))
//                },
//                onResponse = { response ->
//                    val accountUser = response.response?.firstOrNull() ?: return@sendRequest
//                    val mappedUser = accountUser.mapToDomain()
//
//                    usersRepository.storeUsers(listOf(mappedUser))
//                }
//            )

            loadConversations()
        }
    }

    private fun deleteConversation(peerId: Int) {
        conversationsUseCase.delete(peerId).listenValue { state ->
            state.processState(
                error = {},
                success = {
                    val newConversations = domainConversations.value.toMutableList()
                    val conversationIndex =
                        newConversations.findIndex { it.id == peerId }
                            ?: return@processState

                    newConversations.removeAt(conversationIndex)

                    emitDomainConversations(newConversations)
                }
            )
            screenState.emit(screenState.value.copy(isLoading = state.isLoading()))
        }
    }

    private fun pinConversation(peerId: Int, pin: Boolean) {
//        viewModelScope.launch(Dispatchers.IO) {
//            if (pin) {
//                sendRequest(
//                    request = {
//                        conversationsRepository.pin(ConversationsPinRequest(peerId))
//                    },
//                    onResponse = {
//                        handlePinStateChanged(
//                            LongPollEvent.VkConversationPinStateChangedEvent(
//                                peerId = peerId,
//                                majorId = (pinnedConversationsCount.value + 1) * 16
//                            )
//                        )
//                    }
//                )
//            } else {
//                sendRequest(
//                    request = {
//                        conversationsRepository.unpin(ConversationsUnpinRequest(peerId))
//                    },
//                    onResponse = {
//                        handlePinStateChanged(
//                            LongPollEvent.VkConversationPinStateChangedEvent(
//                                peerId = peerId,
//                                majorId = 0
//                            )
//                        )
//                    }
//                )
//            }
//        }
    }

    private fun handleNewMessage(event: LongPollEvent.VkMessageNewEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            val message = event.message

//            messagesRepository.store(message)

            val newConversations = domainConversations.value.toMutableList()
            val conversationIndex =
                newConversations.findIndex { it.id == message.peerId }

            if (conversationIndex == null) { // диалога нет в списке
                // pizdets
            } else {
                val conversation = newConversations[conversationIndex]
                var newConversation = conversation.copy(
                    lastMessage = message,
                    lastMessageId = message.id,
                    lastConversationMessageId = -1,
                    unreadCount = if (message.isOut) conversation.unreadCount
                    else conversation.unreadCount + 1
                )

                interactionsTimers[conversation.id]?.let { job ->
                    if (job.interactionType == InteractionType.Typing
                        && message.fromId in conversation.interactionIds
                    ) {
                        val newInteractionIds = newConversation.interactionIds.filter { id ->
                            id != message.fromId
                        }

                        newConversation = newConversation.copy(
                            interactionType = if (newInteractionIds.isEmpty()) -1 else {
                                newConversation.interactionType
                            },
                            interactionIds = newInteractionIds
                        )
                    }
                }

                if (conversation.isPinned()) {
                    newConversations[conversationIndex] = newConversation
                } else {
                    newConversations.removeAt(conversationIndex)

                    val toPosition = pinnedConversationsCount.value
                    newConversations.add(toPosition, newConversation)
                }

                emitDomainConversations(newConversations)
            }
        }
    }

    private fun handleEditedMessage(event: LongPollEvent.VkMessageEditEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            val message = event.message

//            messagesRepository.store(message)

            val newConversations = domainConversations.value.toMutableList()

            val conversationIndex = newConversations.findIndex { it.id == message.peerId }
            if (conversationIndex == null) { // диалога нет в списке
                //  pizdets
            } else {
                val conversation = newConversations[conversationIndex]
                newConversations[conversationIndex] = conversation.copy(
                    lastMessage = message,
                    lastMessageId = message.id,
                    lastConversationMessageId = -1
                )

                emitDomainConversations(newConversations)
            }
        }
    }

    private fun handleReadIncomingMessage(event: LongPollEvent.VkMessageReadIncomingEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            val newConversations = domainConversations.value.toMutableList()

            val conversationIndex =
                newConversations.findIndex { it.id == event.peerId } ?: return@launch

            newConversations[conversationIndex] =
                newConversations[conversationIndex].copy(
                    inRead = event.messageId,
                    unreadCount = event.unreadCount
                )

            emitDomainConversations(newConversations)
        }
    }

    private fun handleReadOutgoingMessage(event: LongPollEvent.VkMessageReadOutgoingEvent) =
        viewModelScope.launch(Dispatchers.IO) {
            val newConversations = domainConversations.value.toMutableList()

            val conversationIndex =
                newConversations.findIndex { it.id == event.peerId } ?: return@launch

            newConversations[conversationIndex] =
                newConversations[conversationIndex].copy(
                    outRead = event.messageId,
                    unreadCount = event.unreadCount
                )

            emitDomainConversations(newConversations)
        }

    private fun handlePinStateChanged(event: LongPollEvent.VkConversationPinStateChangedEvent) =
        viewModelScope.launch(Dispatchers.IO) {
            var pinnedCount = pinnedConversationsCount.value
            val newConversations = domainConversations.value.toMutableList()

            val conversationIndex =
                newConversations.findIndex { it.id == event.peerId } ?: return@launch

            val pin = event.majorId > 0

            val conversation = newConversations[conversationIndex].copy(majorId = event.majorId)

            newConversations.removeAt(conversationIndex)

            if (pin) {
                newConversations.add(0, conversation)
            } else {
                pinnedCount -= 1

                newConversations.add(conversation)

                val pinnedSubList = newConversations.filter(VkConversationDomain::isPinned)
                val unpinnedSubList = newConversations
                    .filterNot(VkConversationDomain::isPinned)
                    .toMutableList()

                unpinnedSubList.sortByDescending { item ->
                    item.lastMessage?.date
                }

                newConversations.clear()
                newConversations += pinnedSubList + unpinnedSubList
            }

            emitDomainConversations(newConversations)
        }

    private val interactionsTimers = hashMapOf<Int, InteractionJob?>()

    private data class InteractionJob(
        val interactionType: InteractionType,
        val timerJob: Job
    )

    private object NewInteractionException : CancellationException()

    private fun handleInteraction(event: LongPollEvent.Interaction) {
        val interactionType = event.interactionType
        val peerId = event.peerId
        val userIds = event.userIds

        viewModelScope.launch(Dispatchers.IO) {
            val newConversations = domainConversations.value.toMutableList()
            val conversationAndIndex =
                newConversations.findWithIndex { it.id == peerId } ?: return@launch

            newConversations[conversationAndIndex.first] =
                conversationAndIndex.second.copy(
                    interactionType = interactionType.value,
                    interactionIds = userIds
                )

            emitDomainConversations(newConversations)

            interactionsTimers[peerId]?.let { interactionJob ->
                if (interactionJob.interactionType == interactionType) {
                    interactionJob.timerJob.cancel(NewInteractionException)
                }
            }

            var timeoutAction: (() -> Unit)? = null

            val timerJob = createTimerFlow(
                time = 5,
                onTimeoutAction = { timeoutAction?.invoke() }
            ).launchIn(viewModelScope)

            val newInteractionJob = InteractionJob(
                interactionType = interactionType,
                timerJob = timerJob
            )

            interactionsTimers[peerId] = newInteractionJob

            timeoutAction = {
                stopInteraction(peerId, newInteractionJob)
            }
        }
    }

    private fun stopInteraction(peerId: Int, interactionJob: InteractionJob) {
        interactionsTimers[peerId] ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val newConversations = domainConversations.value.toMutableList()
            val conversationAndIndex =
                newConversations.findWithIndex { it.id == peerId } ?: return@launch

            newConversations[conversationAndIndex.first] =
                conversationAndIndex.second.copy(
                    interactionType = -1,
                    interactionIds = emptyList()
                )

            emitDomainConversations(newConversations)

            interactionJob.timerJob.cancel()
            interactionsTimers[peerId] = null
        }
    }

    private fun readConversation(peerId: Int, startMessageId: Int) {
//        viewModelScope.launch(Dispatchers.IO) {
//            sendRequest(
//                request = {
//                    messagesRepository.markAsRead(
//                        peerId = peerId,
//                        startMessageId = startMessageId
//                    )
//                },
//                onResponse = {
//                    val newConversations = domainConversations.value.toMutableList()
//                    val conversationIndex =
//                        newConversations.findIndex { it.id == peerId } ?: return@sendRequest
//
//                    newConversations[conversationIndex] =
//                        newConversations[conversationIndex].copyWithEssentials { old ->
//                            old.copy(inRead = startMessageId)
//                        }
//
//                    emitDomainConversations(newConversations)
//                }
//            )
//        }
    }
}

