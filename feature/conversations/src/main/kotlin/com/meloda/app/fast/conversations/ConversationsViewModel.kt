package com.meloda.app.fast.conversations

import android.content.SharedPreferences
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import com.meloda.app.fast.common.extensions.createTimerFlow
import com.meloda.app.fast.common.extensions.emitOnScope
import com.meloda.app.fast.common.extensions.findIndex
import com.meloda.app.fast.common.extensions.findWithIndex
import com.meloda.app.fast.common.extensions.listenValue
import com.meloda.app.fast.common.extensions.setValue
import com.meloda.app.fast.conversations.domain.ConversationsUseCase
import com.meloda.app.fast.conversations.model.ConversationOption
import com.meloda.app.fast.conversations.model.ConversationsScreenState
import com.meloda.app.fast.conversations.model.ConversationsShowOptions
import com.meloda.app.fast.conversations.model.UiConversation
import com.meloda.app.fast.conversations.util.asPresentation
import com.meloda.app.fast.data.db.AccountsRepository
import com.meloda.app.fast.data.processState
import com.meloda.app.fast.datastore.SettingsKeys
import com.meloda.app.fast.datastore.UserConfig
import com.meloda.app.fast.designsystem.ImmutableList
import com.meloda.app.fast.model.InteractionType
import com.meloda.app.fast.model.LongPollEvent
import com.meloda.app.fast.model.api.domain.VkConversation
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

    fun onConversationItemLongClick(conversation: UiConversation)

    fun onPinDialogDismissed()
    fun onPinDialogPositiveClick(conversation: UiConversation)
    fun onOptionClicked(conversation: UiConversation, option: ConversationOption)
}

class ConversationsViewModelImpl(
//    updatesParser: LongPollUpdatesParser,
    private val imageLoader: ImageLoader,
    private val accountsRepository: AccountsRepository,
    private val conversationsUseCase: ConversationsUseCase,
    private val resources: Resources,
    private val preferences: SharedPreferences
) : ConversationsViewModel, ViewModel() {

    override val screenState = MutableStateFlow(ConversationsScreenState.EMPTY)

    private val conversations = MutableStateFlow<List<VkConversation>>(emptyList())

    private val pinnedConversationsCount = conversations.map { conversations ->
        conversations.filter { conversation -> conversation.isPinned() }.size
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    init {
//        updatesParser.onNewMessage(::handleNewMessage)
//        updatesParser.onMessageEdited(::handleEditedMessage)
//        updatesParser.onMessageIncomingRead(::handleReadIncomingMessage)
//        updatesParser.onMessageOutgoingRead(::handleReadOutgoingMessage)
//        updatesParser.onConversationPinStateChanged(::handlePinStateChanged)
//        updatesParser.onInteractions(::handleInteraction)

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

    override fun onConversationItemLongClick(conversation: UiConversation) {
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

    override fun onPinDialogPositiveClick(conversation: UiConversation) {
        pinConversation(conversation.id, !conversation.isPinned)
        hideOptions(conversation.id)
    }

    override fun onOptionClicked(conversation: UiConversation, option: ConversationOption) {
        when (option) {
            ConversationOption.Delete -> {
                emitShowOptions { old ->
                    old.copy(showDeleteDialog = conversation.id)
                }
            }

            ConversationOption.MarkAsRead -> {
                conversation.lastMessageId?.let { lastMessageId ->
                    readConversation(
                        peerId = conversation.id,
                        startMessageId = lastMessageId
                    )
                    hideOptions(conversation.id)
                }
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

    private fun emitShowOptions(function: (ConversationsShowOptions) -> ConversationsShowOptions) {
        val newShowOptions = function.invoke(screenState.value.showOptions)
        screenState.setValue { old -> old.copy(showOptions = newShowOptions) }
    }

    private fun loadConversations(offset: Int? = null) {
        conversationsUseCase.getConversations(
            count = 60,
            offset = 0
        ).listenValue { state ->
            state.processState(
                error = { error -> },
                success = { response ->
                    conversationsUseCase.storeConversations(response)

                    val pinnedConversationsCount =
                        response.filter(VkConversation::isPinned).size

                    conversations.emitOnScope { response }

                    val conversations = response.map {
                        it.asPresentation(
                            resources,
                            preferences.getBoolean(
                                SettingsKeys.KEY_USE_CONTACT_NAMES,
                                SettingsKeys.DEFAULT_VALUE_USE_CONTACT_NAMES
                            )
                        )
                    }

//                    val avatars = uiConversations.mapNotNull { conversation ->
//                        conversation.avatar?.extractUrl()
//                    }
//
//                    avatars.forEach { avatar ->
//                        val request = ImageRequest.Builder(context)
//                            .data(avatar)
//                            .build()
//
//                        context.imageLoader.enqueue(request)
//                    }

                    screenState.setValue { old ->
                        old.copy(
                            conversations = conversations,
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
            accountsRepository.getAccounts().let { accounts ->
                Log.d("ConversationsViewModel", "initUserConfig: accounts: $accounts")
                if (accounts.isNotEmpty()) {
                    val currentAccount = accounts.find { it.userId == UserConfig.currentUserId }
                    if (currentAccount != null) {
                        UserConfig.userId = currentAccount.userId
                        UserConfig.accessToken = currentAccount.accessToken
                        UserConfig.fastToken = currentAccount.fastToken
                        UserConfig.trustedHash = currentAccount.trustedHash
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
//        conversationsUseCase.delete(peerId).listenValue { state ->
//            state.processState(
//                error = {},
//                success = {
//                    val newConversations = domainConversations.value.toMutableList()
//                    val conversationIndex =
//                        newConversations.findIndex { it.id == peerId }
//                            ?: return@processState
//
//                    newConversations.removeAt(conversationIndex)
//
//                    emitDomainConversations(newConversations)
//                }
//            )
//            screenState.emit(screenState.value.copy(isLoading = state.isLoading()))
//        }
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

            val newConversations = conversations.value.toMutableList()
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

                conversations.emit(newConversations)
                screenState.setValue { old ->
                    old.copy(
                        conversations = newConversations.map { it.asPresentation(resources) }
                    )
                }
            }
        }
    }

    private fun handleEditedMessage(event: LongPollEvent.VkMessageEditEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            val message = event.message

//            messagesRepository.store(message)

            val newConversations = conversations.value.toMutableList()

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

                conversations.emit(newConversations)
                screenState.setValue { old ->
                    old.copy(
                        conversations = newConversations.map { it.asPresentation(resources) }
                    )
                }
            }
        }
    }

    private fun handleReadIncomingMessage(event: LongPollEvent.VkMessageReadIncomingEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            val newConversations = conversations.value.toMutableList()

            val conversationIndex =
                newConversations.findIndex { it.id == event.peerId } ?: return@launch

            newConversations[conversationIndex] =
                newConversations[conversationIndex].copy(
                    inRead = event.messageId,
                    unreadCount = event.unreadCount
                )

            conversations.emit(newConversations)
            screenState.setValue { old ->
                old.copy(
                    conversations = newConversations.map { it.asPresentation(resources) }
                )
            }
        }
    }

    private fun handleReadOutgoingMessage(event: LongPollEvent.VkMessageReadOutgoingEvent) =
        viewModelScope.launch(Dispatchers.IO) {
            val newConversations = conversations.value.toMutableList()

            val conversationIndex =
                newConversations.findIndex { it.id == event.peerId } ?: return@launch

            newConversations[conversationIndex] =
                newConversations[conversationIndex].copy(
                    outRead = event.messageId,
                    unreadCount = event.unreadCount
                )

            conversations.emit(newConversations)
            screenState.setValue { old ->
                old.copy(
                    conversations = newConversations.map { it.asPresentation(resources) }
                )
            }
        }

    private fun handlePinStateChanged(event: LongPollEvent.VkConversationPinStateChangedEvent) =
        viewModelScope.launch(Dispatchers.IO) {
            var pinnedCount = pinnedConversationsCount.value
            val newConversations = conversations.value.toMutableList()

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

                val pinnedSubList = newConversations.filter(VkConversation::isPinned)
                val unpinnedSubList = newConversations
                    .filterNot(VkConversation::isPinned)
                    .toMutableList()

                unpinnedSubList.sortByDescending { item ->
                    item.lastMessage?.date
                }

                newConversations.clear()
                newConversations += pinnedSubList + unpinnedSubList
            }

            conversations.emit(newConversations)
            screenState.setValue { old ->
                old.copy(
                    conversations = newConversations.map { it.asPresentation(resources) }
                )
            }
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
            val newConversations = conversations.value.toMutableList()
            val conversationAndIndex =
                newConversations.findWithIndex { it.id == peerId } ?: return@launch

            newConversations[conversationAndIndex.first] =
                conversationAndIndex.second.copy(
                    interactionType = interactionType.value,
                    interactionIds = userIds
                )

            conversations.emit(newConversations)
            screenState.setValue { old ->
                old.copy(
                    conversations = newConversations.map { it.asPresentation(resources) }
                )
            }

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
            val newConversations = conversations.value.toMutableList()
            val conversationAndIndex =
                newConversations.findWithIndex { it.id == peerId } ?: return@launch

            newConversations[conversationAndIndex.first] =
                conversationAndIndex.second.copy(
                    interactionType = -1,
                    interactionIds = emptyList()
                )

            conversations.emit(newConversations)
            screenState.setValue { old ->
                old.copy(
                    conversations = newConversations.map { it.asPresentation(resources) }
                )
            }

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

