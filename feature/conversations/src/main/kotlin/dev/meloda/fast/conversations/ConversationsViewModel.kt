package dev.meloda.fast.conversations

import android.content.Context
import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import com.conena.nanokt.collections.indexOfFirstOrNull
import dev.meloda.fast.common.extensions.createTimerFlow
import dev.meloda.fast.common.extensions.findWithIndex
import dev.meloda.fast.common.extensions.listenValue
import dev.meloda.fast.common.extensions.setValue
import dev.meloda.fast.conversations.model.ConversationsScreenState
import dev.meloda.fast.conversations.util.asPresentation
import dev.meloda.fast.conversations.util.extractAvatar
import dev.meloda.fast.data.State
import dev.meloda.fast.data.processState
import dev.meloda.fast.datastore.UserSettings
import dev.meloda.fast.domain.ConversationsUseCase
import dev.meloda.fast.domain.LoadConversationsByIdUseCase
import dev.meloda.fast.domain.LongPollUpdatesParser
import dev.meloda.fast.domain.MessagesUseCase
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.model.InteractionType
import dev.meloda.fast.model.LongPollEvent
import dev.meloda.fast.model.api.domain.VkConversation
import dev.meloda.fast.network.VkErrorCode
import dev.meloda.fast.ui.model.api.ConversationOption
import dev.meloda.fast.ui.model.api.ConversationsShowOptions
import dev.meloda.fast.ui.model.api.UiConversation
import dev.meloda.fast.ui.util.ImmutableList
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
    val currentOffset: StateFlow<Int>
    val canPaginate: StateFlow<Boolean>

    fun onPaginationConditionsMet()

    fun onDeleteDialogDismissed()
    fun onDeleteDialogPositiveClick()

    fun onRefresh()

    fun onConversationItemClick()
    fun onConversationItemLongClick(conversation: UiConversation)

    fun onPinDialogDismissed()
    fun onPinDialogPositiveClick()

    fun onOptionClicked(conversation: UiConversation, option: ConversationOption)

    fun onErrorConsumed()

    fun setScrollIndex(index: Int)
    fun setScrollOffset(offset: Int)
}

class ConversationsViewModelImpl(
    updatesParser: LongPollUpdatesParser,
    private val conversationsUseCase: ConversationsUseCase,
    private val messagesUseCase: MessagesUseCase,
    private val resources: Resources,
    private val userSettings: UserSettings,
    private val imageLoader: ImageLoader,
    private val applicationContext: Context,
    private val loadConversationsByIdUseCase: LoadConversationsByIdUseCase
) : ConversationsViewModel, ViewModel() {

    override val screenState = MutableStateFlow(ConversationsScreenState.EMPTY)
    override val baseError = MutableStateFlow<BaseError?>(null)
    override val currentOffset = MutableStateFlow(0)
    override val canPaginate = MutableStateFlow(false)

    private val useContactNames: Boolean get() = userSettings.useContactNames.value

    override fun onPaginationConditionsMet() {
        currentOffset.update { screenState.value.conversations.size }
        loadConversations()
    }

    private val conversations = MutableStateFlow<List<VkConversation>>(emptyList())

    private val pinnedConversationsCount = conversations.map { conversations ->
        conversations.count(VkConversation::isPinned)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    init {
        userSettings.useContactNames.listenValue(viewModelScope, ::updateConversationsNames)

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

    override fun onDeleteDialogPositiveClick() {
        val conversationId = screenState.value.showOptions.showDeleteDialog ?: return
        deleteConversation(conversationId)
        hideOptions(conversationId)
        onDeleteDialogDismissed()
    }

    override fun onRefresh() {
        onErrorConsumed()
        loadConversations(offset = 0)
    }

    override fun onConversationItemClick() {
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

    override fun onPinDialogPositiveClick() {
        val conversation = screenState.value.showOptions.showPinDialog ?: return
        pinConversation(conversation.id, !conversation.isPinned)
        hideOptions(conversation.id)
        onPinDialogDismissed()
    }

    override fun onOptionClicked(
        conversation: UiConversation,
        option: ConversationOption
    ) {
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

    override fun setScrollIndex(index: Int) {
        screenState.setValue { old -> old.copy(scrollIndex = index) }
    }

    override fun setScrollOffset(offset: Int) {
        screenState.setValue { old -> old.copy(scrollOffset = offset) }
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
        conversationsUseCase.getConversations(count = LOAD_COUNT, offset = offset)
            .listenValue(viewModelScope) { state ->
                state.processState(
                    error = ::handleError,
                    success = { response ->
                        val itemsCountSufficient = response.size == LOAD_COUNT
                        canPaginate.setValue { itemsCountSufficient }

                        val paginationExhausted = !itemsCountSufficient &&
                                screenState.value.conversations.isNotEmpty()

                        val imagesToPreload =
                            response.mapNotNull { it.extractAvatar().extractUrl() }

                        imagesToPreload.forEach { url ->
                            imageLoader.enqueue(
                                ImageRequest.Builder(applicationContext)
                                    .data(url)
                                    .build()
                            )
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

    private fun handleError(error: State.Error) {
        when (error) {
            is State.Error.ApiError -> {
                when (error.errorCode) {
                    VkErrorCode.USER_AUTHORIZATION_FAILED -> {
                        baseError.setValue { BaseError.SessionExpired }
                    }

                    else -> {
                        baseError.setValue {
                            BaseError.SimpleError(message = error.errorMessage)
                        }
                    }
                }
            }

            State.Error.ConnectionError -> {
                baseError.setValue {
                    BaseError.SimpleError(message = "Connection error")
                }
            }

            State.Error.InternalError -> {
                baseError.setValue {
                    BaseError.SimpleError(message = "Internal error")
                }
            }

            State.Error.UnknownError -> {
                baseError.setValue {
                    BaseError.SimpleError(message = "Unknown error")
                }
            }

            else -> Unit
        }
    }

    private fun deleteConversation(peerId: Int) {
        conversationsUseCase.delete(peerId).listenValue(viewModelScope) { state ->
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
                            conversations = newConversations.map {
                                it.asPresentation(
                                    resources = resources,
                                    useContactName = useContactNames
                                )
                            }
                        )
                    }
                }
            )
            screenState.emit(screenState.value.copy(isLoading = state.isLoading()))
        }
    }

    private fun pinConversation(peerId: Int, pin: Boolean) {
        conversationsUseCase.changePinState(peerId, pin)
            .listenValue(viewModelScope) { state ->
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

        if (conversationIndex == null) {
            loadConversationsByIdUseCase(peerIds = listOf(message.peerId))
                .listenValue(viewModelScope) { state ->
                    state.processState(
                        error = { error ->

                        },
                        success = { response ->
                            val conversation = (response.firstOrNull() ?: return@listenValue)
                                .copy(lastMessage = message)

                            // TODO: 22-Dec-24, Danil Nikolaev: handle interactions and pinned state

                            newConversations.add(pinnedConversationsCount.value, conversation)
                            conversations.update { newConversations }

                            screenState.setValue { old ->
                                old.copy(
                                    conversations = newConversations.map {
                                        it.asPresentation(
                                            resources = resources,
                                            useContactName = useContactNames
                                        )
                                    }
                                )
                            }
                        }
                    )
                }
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
                    conversations = newConversations.map {
                        it.asPresentation(
                            resources = resources,
                            useContactName = useContactNames
                        )
                    }
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
                    conversations = newConversations.map {
                        it.asPresentation(
                            resources = resources,
                            useContactName = useContactNames
                        )
                    }
                )
            }
        }
    }

    private fun handleReadIncomingMessage(event: LongPollEvent.VkMessageReadIncomingEvent) {
        val newConversations = conversations.value.toMutableList()

        val conversationIndex =
            newConversations.indexOfFirstOrNull { it.id == event.peerId }

        if (conversationIndex == null) { // диалога нет в списке
            // pizdets
        } else {
            newConversations[conversationIndex] =
                newConversations[conversationIndex].copy(
                    inRead = event.messageId,
                    unreadCount = event.unreadCount
                )

            conversations.update { newConversations }

            screenState.setValue { old ->
                old.copy(
                    conversations = newConversations.map {
                        it.asPresentation(
                            resources = resources,
                            useContactName = useContactNames
                        )
                    }
                )
            }
        }
    }

    private fun handleReadOutgoingMessage(event: LongPollEvent.VkMessageReadOutgoingEvent) {
        val newConversations = conversations.value.toMutableList()

        val conversationIndex =
            newConversations.indexOfFirstOrNull { it.id == event.peerId }

        if (conversationIndex == null) { // диалога нет в списке
            // pizdets
        } else {
            newConversations[conversationIndex] =
                newConversations[conversationIndex].copy(
                    outRead = event.messageId,
                    unreadCount = event.unreadCount
                )

            conversations.update { newConversations }
            screenState.setValue { old ->
                old.copy(
                    conversations = newConversations.map {
                        it.asPresentation(
                            resources = resources,
                            useContactName = useContactNames
                        )
                    }
                )
            }
        }
    }

    private fun handlePinStateChanged(event: LongPollEvent.VkConversationPinStateChangedEvent) {
        var pinnedCount = pinnedConversationsCount.value
        val newConversations = conversations.value.toMutableList()

        val conversationIndex =
            newConversations.indexOfFirstOrNull { it.id == event.peerId }

        if (conversationIndex == null) { // диалога нет в списке
            // pizdets
        } else {
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
                old.copy(conversations = newConversations.map {
                    it.asPresentation(
                        resources = resources,
                        useContactName = useContactNames
                    )
                })
            }
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
            newConversations.findWithIndex { it.id == peerId }

        if (conversationAndIndex == null) { // диалога нет в списке
            // pizdets
        } else {
            newConversations[conversationAndIndex.first] =
                conversationAndIndex.second.copy(
                    interactionType = interactionType.value,
                    interactionIds = userIds
                )

            conversations.update { newConversations }

            screenState.setValue { old ->
                old.copy(
                    conversations = newConversations.map {
                        it.asPresentation(
                            resources = resources,
                            useContactName = useContactNames
                        )
                    }
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
                conversations = newConversations.map {
                    it.asPresentation(
                        resources = resources,
                        useContactName = useContactNames
                    )
                }
            )
        }

        interactionJob.timerJob.cancel()
        interactionsTimers[peerId] = null
    }

    private fun readConversation(peerId: Int, startMessageId: Int) {
        messagesUseCase.markAsRead(
            peerId = peerId,
            startMessageId = startMessageId
        ).listenValue(viewModelScope) { state ->
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
                            conversations = newConversations.map {
                                it.asPresentation(
                                    resources = resources,
                                    useContactName = useContactNames
                                )
                            }
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

    companion object {
        const val LOAD_COUNT = 30
    }
}

