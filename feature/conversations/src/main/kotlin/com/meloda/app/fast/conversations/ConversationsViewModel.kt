package com.meloda.app.fast.conversations

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.conena.nanokt.collections.indexOfFirstOrNull
import com.meloda.app.fast.common.extensions.createTimerFlow
import com.meloda.app.fast.common.extensions.findWithIndex
import com.meloda.app.fast.common.extensions.listenValue
import com.meloda.app.fast.common.extensions.setValue
import com.meloda.app.fast.conversations.model.ConversationOption
import com.meloda.app.fast.conversations.model.ConversationsScreenState
import com.meloda.app.fast.conversations.model.ConversationsShowOptions
import com.meloda.app.fast.conversations.model.UiConversation
import com.meloda.app.fast.conversations.util.asPresentation
import com.meloda.app.fast.conversations.util.extractAvatar
import com.meloda.app.fast.data.LongPollUpdatesParser
import com.meloda.app.fast.data.State
import com.meloda.app.fast.data.api.conversations.ConversationsUseCase
import com.meloda.app.fast.data.api.messages.MessagesUseCase
import com.meloda.app.fast.data.processState
import com.meloda.app.fast.datastore.UserSettings
import com.meloda.app.fast.designsystem.ImmutableList
import com.meloda.app.fast.model.BaseError
import com.meloda.app.fast.model.InteractionType
import com.meloda.app.fast.model.LongPollEvent
import com.meloda.app.fast.model.api.domain.VkConversation
import com.meloda.app.fast.network.VkErrorCodes
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlin.coroutines.cancellation.CancellationException

interface ConversationsViewModel {

    val screenState: StateFlow<ConversationsScreenState>
    val baseError: StateFlow<BaseError?>
    val imagesToPreload: StateFlow<List<String>>
    val currentOffset: StateFlow<Int>
    val canPaginate: StateFlow<Boolean>

    fun onMetPaginationCondition()

    fun onDeleteDialogDismissed()

    fun onDeleteDialogPositiveClick(conversationId: Int)

    fun onRefresh()

    fun onConversationItemClick(conversationId: Int)
    fun onConversationItemLongClick(conversation: UiConversation)

    fun onPinDialogDismissed()
    fun onPinDialogPositiveClick(conversation: UiConversation)
    fun onOptionClicked(conversation: UiConversation, option: ConversationOption)

    fun onErrorConsumed()
}

class ConversationsViewModelImpl(
    updatesParser: LongPollUpdatesParser,
    private val conversationsUseCase: ConversationsUseCase,
    private val messagesUseCase: MessagesUseCase,
    private val resources: Resources,
    private val userSettings: UserSettings
) : ConversationsViewModel, ViewModel() {

    override val screenState = MutableStateFlow(ConversationsScreenState.EMPTY)
    override val baseError = MutableStateFlow<BaseError?>(null)
    override val imagesToPreload = MutableStateFlow<List<String>>(emptyList())
    override val currentOffset = MutableStateFlow(0)
    override val canPaginate = MutableStateFlow(false)

    override fun onMetPaginationCondition() {
        currentOffset.update { screenState.value.conversations.size }
        loadConversations()
    }

    private val conversations = MutableStateFlow<List<VkConversation>>(emptyList())

    private val pinnedConversationsCount = conversations.map { conversations ->
        conversations.count(VkConversation::isPinned)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    init {
        userSettings.useContactNames.listenValue(::updateConversationsNames)

        updatesParser.onNewMessage(::handleNewMessage)
        updatesParser.onMessageEdited(::handleEditedMessage)
        updatesParser.onMessageIncomingRead(::handleReadIncomingMessage)
        updatesParser.onMessageOutgoingRead(::handleReadOutgoingMessage)
        updatesParser.onConversationPinStateChanged(::handlePinStateChanged)
        updatesParser.onInteractions(::handleInteraction)

        loadConversations()
    }

    override fun onDeleteDialogDismissed() {
        emitShowOptions { old -> old.copy(showDeleteDialog = null) }
    }

    override fun onDeleteDialogPositiveClick(conversationId: Int) {
        deleteConversation(conversationId)
        hideOptions(conversationId)
    }

    override fun onRefresh() {
        loadConversations(offset = 0)
    }

    override fun onConversationItemClick(conversationId: Int) {
        screenState.setValue { old ->
            old.copy(
                conversations = old.conversations.map { item ->
                    item.copy(isExpanded = false)
                }
            )
        }
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

    override fun onErrorConsumed() {
        baseError.setValue { null }
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

    private fun loadConversations(
        offset: Int = currentOffset.value
    ) {
        conversationsUseCase.getConversations(count = 30, offset = offset).listenValue { state ->
            state.processState(
                error = { error ->
                    when (error) {
                        is State.Error.ApiError -> {
                            val (code, message) = error

                            when (code) {
                                VkErrorCodes.UserAuthorizationFailed -> {
                                    baseError.setValue { BaseError.SessionExpired }
                                }

                                else -> {
                                    Unit
                                }
                            }
                        }

                        State.Error.ConnectionError -> TODO()
                        State.Error.InternalError -> TODO()
                        is State.Error.OAuthError -> TODO()
                        State.Error.Unknown -> TODO()
                    }
                },
                success = { response ->
                    val itemsCountSufficient = response.size == 30
                    canPaginate.setValue { itemsCountSufficient }

                    val paginationExhausted = !itemsCountSufficient &&
                            screenState.value.conversations.isNotEmpty()

                    imagesToPreload.setValue {
                        response.mapNotNull { it.extractAvatar().extractUrl() }
                    }
                    conversationsUseCase.storeConversations(response)

                    val loadedConversations = response.map {
                        it.asPresentation(
                            resources,
                            userSettings.useContactNames.value
                        )
                    }

                    val newState = screenState.value.copy(
                        isPaginationExhausted = paginationExhausted
                    )
                    if (offset == 0) {
                        conversations.emit(response)
                        screenState.setValue {
                            newState.copy(conversations = loadedConversations)
                        }
                    } else {
                        conversations.emit(conversations.value.plus(response))
                        screenState.setValue {
                            newState.copy(
                                conversations = newState.conversations.plus(loadedConversations)
                            )
                        }
                    }
                }
            )

            screenState.setValue { old ->
                old.copy(
                    isLoading = offset == 0 && state.isLoading(),
                    isPaginating = offset > 0 && state.isLoading()
                )
            }
        }
    }

    private fun deleteConversation(peerId: Int) {
        conversationsUseCase.delete(peerId).listenValue { state ->
            state.processState(
                error = { error ->

                },
                success = {
                    val newConversations = conversations.value.toMutableList()
                    val conversationIndex =
                        newConversations.indexOfFirstOrNull { it.id == peerId }
                            ?: return@processState

                    newConversations.removeAt(conversationIndex)
                    conversations.update { newConversations }
                    screenState.setValue { old ->
                        old.copy(
                            conversations = newConversations.map { it.asPresentation(resources) }
                        )
                    }
                }
            )
            screenState.emit(screenState.value.copy(isLoading = state.isLoading()))
        }
    }

    private fun pinConversation(peerId: Int, pin: Boolean) {
        conversationsUseCase.changePinState(peerId, pin)
            .listenValue { state ->
                state.processState(
                    error = { error ->

                    },
                    success = {
                        handlePinStateChanged(
                            LongPollEvent.VkConversationPinStateChangedEvent(
                                peerId = peerId,
                                majorId = if (pin) {
                                    (pinnedConversationsCount.value + 1) * 16
                                } else {
                                    0
                                }
                            )
                        )
                    }
                )

                screenState.setValue { old -> old.copy(isLoading = state.isLoading()) }
            }
    }

    private fun handleNewMessage(event: LongPollEvent.VkMessageNewEvent) {
        val message = event.message
        val newConversations = conversations.value.toMutableList()
        val conversationIndex =
            newConversations.indexOfFirstOrNull { it.id == message.peerId }

        if (conversationIndex == null) { // диалога нет в списке
            // pizdets
            // TODO: 04/07/2024, Danil Nikolaev: load conversation and store info
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

            conversations.update { newConversations }

            screenState.setValue { old ->
                old.copy(
                    conversations = newConversations.map { it.asPresentation(resources) }
                )
            }
        }
    }

    private fun handleEditedMessage(event: LongPollEvent.VkMessageEditEvent) {
        val message = event.message
        val newConversations = conversations.value.toMutableList()

        val conversationIndex = newConversations.indexOfFirstOrNull { it.id == message.peerId }
        if (conversationIndex == null) { // диалога нет в списке
            //  pizdets
        } else {
            val conversation = newConversations[conversationIndex]
            newConversations[conversationIndex] = conversation.copy(
                lastMessage = message,
                lastMessageId = message.id,
                lastConversationMessageId = -1
            )
            conversations.update { newConversations }

            screenState.setValue { old ->
                old.copy(
                    conversations = newConversations.map { it.asPresentation(resources) }
                )
            }
        }
    }

    private fun handleReadIncomingMessage(event: LongPollEvent.VkMessageReadIncomingEvent) {
        val newConversations = conversations.value.toMutableList()

        val conversationIndex =
            newConversations.indexOfFirstOrNull { it.id == event.peerId } ?: return

        newConversations[conversationIndex] =
            newConversations[conversationIndex].copy(
                inRead = event.messageId,
                unreadCount = event.unreadCount
            )

        conversations.update { newConversations }

        screenState.setValue { old ->
            old.copy(
                conversations = newConversations.map { it.asPresentation(resources) }
            )
        }
    }

    private fun handleReadOutgoingMessage(event: LongPollEvent.VkMessageReadOutgoingEvent) {
        val newConversations = conversations.value.toMutableList()

        val conversationIndex =
            newConversations.indexOfFirstOrNull { it.id == event.peerId } ?: return

        newConversations[conversationIndex] =
            newConversations[conversationIndex].copy(
                outRead = event.messageId,
                unreadCount = event.unreadCount
            )

        conversations.update { newConversations }
        screenState.setValue { old ->
            old.copy(
                conversations = newConversations.map { it.asPresentation(resources) }
            )
        }
    }

    private fun handlePinStateChanged(event: LongPollEvent.VkConversationPinStateChangedEvent) {
        var pinnedCount = pinnedConversationsCount.value
        val newConversations = conversations.value.toMutableList()

        val conversationIndex =
            newConversations.indexOfFirstOrNull { it.id == event.peerId } ?: return

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
                .sortedByDescending { it.lastMessage?.date }

            newConversations.clear()
            newConversations += pinnedSubList + unpinnedSubList
        }

        conversations.update { newConversations }

        screenState.setValue { old ->
            old.copy(conversations = newConversations.map { it.asPresentation(resources) })
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

        val newConversations = conversations.value.toMutableList()
        val conversationAndIndex =
            newConversations.findWithIndex { it.id == peerId } ?: return

        newConversations[conversationAndIndex.first] =
            conversationAndIndex.second.copy(
                interactionType = interactionType.value,
                interactionIds = userIds
            )

        conversations.update { newConversations }

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

    private fun stopInteraction(peerId: Int, interactionJob: InteractionJob) {
        interactionsTimers[peerId] ?: return

        val newConversations = conversations.value.toMutableList()
        val conversationAndIndex =
            newConversations.findWithIndex { it.id == peerId } ?: return

        newConversations[conversationAndIndex.first] =
            conversationAndIndex.second.copy(
                interactionType = -1,
                interactionIds = emptyList()
            )

        conversations.update { newConversations }
        screenState.setValue { old ->
            old.copy(
                conversations = newConversations.map { it.asPresentation(resources) }
            )
        }

        interactionJob.timerJob.cancel()
        interactionsTimers[peerId] = null
    }

    private fun readConversation(peerId: Int, startMessageId: Int) {
        messagesUseCase.markAsRead(
            peerId = peerId,
            startMessageId = startMessageId
        ).listenValue { state ->
            state.processState(
                error = { error ->

                },
                success = {
                    val newConversations = conversations.value.toMutableList()
                    val conversationIndex =
                        newConversations.indexOfFirstOrNull { it.id == peerId }
                            ?: return@listenValue

                    newConversations[conversationIndex] =
                        newConversations[conversationIndex].copy(inRead = startMessageId)

                    conversations.update { newConversations }
                    screenState.setValue { old ->
                        old.copy(
                            conversations = newConversations.map { it.asPresentation(resources) }
                        )
                    }
                }
            )
        }
    }

    private fun updateConversationsNames(useContactNames: Boolean) {
        val conversations = conversations.value
        if (conversations.isEmpty()) return

        val uiConversations = conversations.map { conversation ->
            conversation.asPresentation(resources, useContactNames)
        }

        screenState.setValue { old ->
            old.copy(conversations = uiConversations)
        }
    }
}

