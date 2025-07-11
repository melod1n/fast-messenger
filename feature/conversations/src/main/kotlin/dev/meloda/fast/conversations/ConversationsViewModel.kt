package dev.meloda.fast.conversations

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import com.conena.nanokt.collections.indexOfFirstOrNull
import dev.meloda.fast.common.VkConstants
import dev.meloda.fast.common.extensions.createTimerFlow
import dev.meloda.fast.common.extensions.findWithIndex
import dev.meloda.fast.common.extensions.listenValue
import dev.meloda.fast.common.extensions.setValue
import dev.meloda.fast.common.extensions.updateValue
import dev.meloda.fast.conversations.model.ConversationDialog
import dev.meloda.fast.conversations.model.ConversationNavigation
import dev.meloda.fast.conversations.model.ConversationsScreenState
import dev.meloda.fast.conversations.model.InteractionJob
import dev.meloda.fast.conversations.model.NewInteractionException
import dev.meloda.fast.conversations.util.asPresentation
import dev.meloda.fast.conversations.util.extractAvatar
import dev.meloda.fast.data.VkUtils
import dev.meloda.fast.data.processState
import dev.meloda.fast.datastore.UserSettings
import dev.meloda.fast.domain.ConversationsUseCase
import dev.meloda.fast.domain.LoadConversationsByIdUseCase
import dev.meloda.fast.domain.LongPollUpdatesParser
import dev.meloda.fast.domain.MessagesUseCase
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.model.ConversationsFilter
import dev.meloda.fast.model.InteractionType
import dev.meloda.fast.model.LongPollParsedEvent
import dev.meloda.fast.model.api.domain.VkConversation
import dev.meloda.fast.ui.model.api.ConversationOption
import dev.meloda.fast.ui.model.api.UiConversation
import dev.meloda.fast.ui.util.ImmutableList.Companion.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class ConversationsViewModel(
    updatesParser: LongPollUpdatesParser,
    private val filter: ConversationsFilter,
    private val conversationsUseCase: ConversationsUseCase,
    private val messagesUseCase: MessagesUseCase,
    private val resources: Resources,
    private val userSettings: UserSettings,
    private val imageLoader: ImageLoader,
    private val applicationContext: Context,
    private val loadConversationsByIdUseCase: LoadConversationsByIdUseCase
) : ViewModel() {
    private val _screenState = MutableStateFlow(ConversationsScreenState.EMPTY)
    val screenState = _screenState.asStateFlow()

    private val _navigation = MutableStateFlow<ConversationNavigation?>(null)
    val navigation = _navigation.asStateFlow()

    private val _dialog = MutableStateFlow<ConversationDialog?>(null)
    val dialog = _dialog.asStateFlow()

    private val _conversations = MutableStateFlow<List<VkConversation>>(emptyList())
    val conversations = _conversations.asStateFlow()

    private val _uiConversations = MutableStateFlow<List<UiConversation>>(emptyList())
    val uiConversations = _uiConversations.asStateFlow()

    private val pinnedConversationsCount = conversations.map { conversations ->
        conversations.count(VkConversation::isPinned)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val _baseError = MutableStateFlow<BaseError?>(null)
    val baseError = _baseError.asStateFlow()

    private val _currentOffset = MutableStateFlow(0)
    val currentOffset = _currentOffset.asStateFlow()

    private val _canPaginate = MutableStateFlow(false)
    val canPaginate = _canPaginate.asStateFlow()

    private val expandedConversationId = MutableStateFlow(0L)

    private val useContactNames: Boolean get() = userSettings.useContactNames.value

    private val interactionsTimers = hashMapOf<Long, InteractionJob?>()

    init {
        _screenState.updateValue { copy(isArchive = filter == ConversationsFilter.ARCHIVE) }

        loadConversations()

        updatesParser.onNewMessage(::handleNewMessage)
        updatesParser.onMessageEdited(::handleEditedMessage)
        updatesParser.onMessageIncomingRead(::handleReadIncomingMessage)
        updatesParser.onMessageOutgoingRead(::handleReadOutgoingMessage)
        updatesParser.onInteractions(::handleInteraction)
        updatesParser.onChatMajorChanged(::handleChatMajorChanged)
        updatesParser.onChatMinorChanged(::handleChatMinorChanged)
        updatesParser.onChatCleared(::handleChatClearing)
        updatesParser.onChatArchived(::handleChatArchived)

        userSettings.useContactNames.listenValue(viewModelScope) {
            syncUiConversation()
        }
    }

    fun onNavigationConsumed() {
        _navigation.setValue { null }
    }

    fun onDialogConfirmed(dialog: ConversationDialog, bundle: Bundle) {
        onDialogDismissed(dialog)

        when (dialog) {
            is ConversationDialog.ConversationDelete -> {
                deleteConversation(dialog.conversationId)
            }

            is ConversationDialog.ConversationPin -> {
                pinConversation(dialog.conversationId, true)
            }

            is ConversationDialog.ConversationUnpin -> {
                pinConversation(dialog.conversationId, false)
            }

            is ConversationDialog.ConversationArchive -> {
                archiveConversation(dialog.conversationId, true)
            }

            is ConversationDialog.ConversationUnarchive -> {
                archiveConversation(dialog.conversationId, false)
            }
        }

        expandedConversationId.setValue { 0 }
        syncUiConversation()
    }

    fun onDialogDismissed(dialog: ConversationDialog) {
        _dialog.setValue { null }
    }

    fun onDialogItemPicked(dialog: ConversationDialog, bundle: Bundle) {
        when (dialog) {
            is ConversationDialog.ConversationDelete -> Unit
            is ConversationDialog.ConversationPin -> Unit
            is ConversationDialog.ConversationUnpin -> Unit
            is ConversationDialog.ConversationArchive -> Unit
            is ConversationDialog.ConversationUnarchive -> Unit
        }
    }

    fun onErrorButtonClicked() {
        when (baseError.value) {
            null -> Unit

            is BaseError.ConnectionError,
            is BaseError.InternalError,
            is BaseError.SimpleError,
            is BaseError.UnknownError -> onRefresh()

            else -> Unit
        }
    }

    fun onPaginationConditionsMet() {
        _currentOffset.update { conversations.value.size }
        loadConversations()
    }

    fun onRefresh() {
        onErrorConsumed()
        loadConversations(offset = 0)
    }

    fun onConversationItemClick(conversation: UiConversation) {
        collapseConversations()
        _navigation.setValue { ConversationNavigation.MessagesHistory(peerId = conversation.id) }
    }

    fun onConversationItemLongClick(conversation: UiConversation) {
        expandedConversationId.setValue {
            if (conversation.isExpanded) 0
            else conversation.id
        }
        syncUiConversation()
    }

    fun onOptionClicked(
        conversation: UiConversation,
        option: ConversationOption
    ) {
        when (option) {
            ConversationOption.Delete -> {
                _dialog.setValue { ConversationDialog.ConversationDelete(conversation.id) }
            }

            ConversationOption.MarkAsRead -> {
                conversation.lastMessageId?.let { lastMessageId ->
                    readConversation(
                        peerId = conversation.id,
                        startMessageId = lastMessageId
                    )
                    collapseConversations()
                }
            }

            ConversationOption.Pin -> {
                _dialog.setValue { ConversationDialog.ConversationPin(conversation.id) }
            }

            ConversationOption.Unpin -> {
                _dialog.setValue { ConversationDialog.ConversationUnpin(conversation.id) }
            }

            ConversationOption.Archive -> {
                _dialog.setValue { ConversationDialog.ConversationArchive(conversation.id) }
            }

            ConversationOption.Unarchive -> {
                _dialog.setValue { ConversationDialog.ConversationUnarchive(conversation.id) }
            }
        }
    }

    fun onErrorConsumed() {
        _baseError.setValue { null }
    }

    fun setScrollIndex(index: Int) {
        _screenState.setValue { old -> old.copy(scrollIndex = index) }
    }

    fun setScrollOffset(offset: Int) {
        _screenState.setValue { old -> old.copy(scrollOffset = offset) }
    }

    fun onCreateChatButtonClicked() {
        _navigation.setValue { ConversationNavigation.CreateChat }
    }

    private fun collapseConversations() {
        expandedConversationId.setValue { 0 }
        syncUiConversation()
    }

    private fun loadConversations(
        offset: Int = currentOffset.value
    ) {
        conversationsUseCase.getConversations(
            count = LOAD_COUNT,
            offset = offset,
            filter = filter
        ).listenValue(viewModelScope) { state ->
            state.processState(
                error = { error ->
                    val newBaseError = VkUtils.parseError(error)
                    _baseError.update { newBaseError }
                },
                success = { response ->
                    val conversations = response
                    val fullConversations = if (offset == 0) {
                        conversations
                    } else {
                        this.conversations.value.plus(conversations)
                    }

                    val itemsCountSufficient = response.size == LOAD_COUNT

                    val paginationExhausted = !itemsCountSufficient &&
                            this.conversations.value.isNotEmpty()

                    _screenState.updateValue {
                        copy(isPaginationExhausted = paginationExhausted)
                    }

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

                    _conversations.emit(fullConversations)
                    syncUiConversation()
                    _canPaginate.setValue { itemsCountSufficient }
                }
            )

            _screenState.setValue { old ->
                old.copy(
                    isLoading = offset == 0 && state.isLoading(),
                    isPaginating = offset > 0 && state.isLoading()
                )
            }
        }
    }

    private fun deleteConversation(peerId: Long) {
        conversationsUseCase.delete(peerId).listenValue(viewModelScope) { state ->
            state.processState(
                error = {},
                success = {
                    val newConversations = conversations.value.toMutableList()
                    val conversationIndex =
                        newConversations.indexOfFirstOrNull { it.id == peerId }
                            ?: return@processState

                    newConversations.removeAt(conversationIndex)
                    _conversations.update { newConversations.sorted() }
                    syncUiConversation()
                }
            )
            _screenState.emit(screenState.value.copy(isLoading = state.isLoading()))
        }
    }

    private fun pinConversation(peerId: Long, pin: Boolean) {
        conversationsUseCase.changePinState(peerId, pin)
            .listenValue(viewModelScope) { state ->
                state.processState(
                    error = {},
                    success = {
                        handleChatMajorChanged(
                            LongPollParsedEvent.ChatMajorChanged(
                                peerId = peerId,
                                majorId = if (pin) {
                                    pinnedConversationsCount.value.plus(1) * 16
                                } else {
                                    0
                                }
                            )
                        )
                    }
                )

                _screenState.setValue { old -> old.copy(isLoading = state.isLoading()) }
            }
    }

    private fun archiveConversation(peerId: Long, archive: Boolean) {
        conversationsUseCase.changeArchivedState(peerId, archive)
            .listenValue(viewModelScope) { state ->
                state.processState(
                    error = {},
                    success = {
                        conversations.value.find { it.id == peerId }?.let { conversation ->
                            handleChatArchived(
                                LongPollParsedEvent.ChatArchived(
                                    conversation = conversation,
                                    archived = archive
                                )
                            )
                        }
                    }
                )
            }
    }

    // TODO: 03-Apr-25, Danil Nikolaev: handle business messages
    private fun handleNewMessage(event: LongPollParsedEvent.NewMessage) {
        val message = event.message

        val newConversations = conversations.value.toMutableList()
        val conversationIndex =
            newConversations.indexOfFirstOrNull { it.id == message.peerId }

        if (conversationIndex == null) {
            if (event.inArchive != (filter == ConversationsFilter.ARCHIVE)) return

            loadConversationsByIdUseCase(
                peerIds = listOf(message.peerId),
                extended = true,
                fields = VkConstants.ALL_FIELDS
            ).listenValue(viewModelScope) { state ->
                state.processState(
                    error = {},
                    success = { response ->
                        val conversation = (response.firstOrNull() ?: return@listenValue)
                            .copy(lastMessage = message)

                        newConversations.add(pinnedConversationsCount.value, conversation)
                        _conversations.update { newConversations.sorted() }
                        syncUiConversation()
                    }
                )
            }
        } else {
            val conversation = newConversations[conversationIndex]
            var newConversation = conversation.copy(
                lastMessage = message,
                lastMessageId = message.id,
                lastCmId = message.cmId,
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

            _conversations.update { newConversations.sorted() }
            syncUiConversation()
        }
    }

    private fun handleEditedMessage(event: LongPollParsedEvent.MessageEdited) {
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
                lastCmId = message.cmId
            )
            _conversations.update { newConversations }
            syncUiConversation()
        }
    }

    private fun handleReadIncomingMessage(event: LongPollParsedEvent.IncomingMessageRead) {
        val newConversations = conversations.value.toMutableList()

        val conversationIndex =
            newConversations.indexOfFirstOrNull { it.id == event.peerId }

        if (conversationIndex == null) { // диалога нет в списке
            // pizdets
        } else {
            newConversations[conversationIndex] =
                newConversations[conversationIndex].copy(
                    inReadCmId = event.cmId,
                    unreadCount = event.unreadCount
                )

            _conversations.update { newConversations }
            syncUiConversation()
        }
    }

    private fun handleReadOutgoingMessage(event: LongPollParsedEvent.OutgoingMessageRead) {
        val newConversations = conversations.value.toMutableList()

        val conversationIndex =
            newConversations.indexOfFirstOrNull { it.id == event.peerId }

        if (conversationIndex == null) { // диалога нет в списке
            // pizdets
        } else {
            newConversations[conversationIndex] =
                newConversations[conversationIndex].copy(
                    outReadCmId = event.cmId,
                    unreadCount = event.unreadCount
                )

            _conversations.update { newConversations }
            syncUiConversation()
        }
    }

    private fun handleInteraction(event: LongPollParsedEvent.Interaction) {
        val interactionType = event.interactionType
        val peerId = event.peerId
        val userIds = event.userIds

        val newConversations = conversations.value.toMutableList()
        val conversationAndIndex =
            newConversations.findWithIndex { it.id == peerId }

        if (conversationAndIndex != null) {
            newConversations[conversationAndIndex.first] =
                conversationAndIndex.second.copy(
                    interactionType = interactionType.value,
                    interactionIds = userIds
                )

            _conversations.update { newConversations }
            syncUiConversation()

            interactionsTimers[peerId]?.let { interactionJob ->
                if (interactionJob.interactionType == interactionType) {
                    interactionJob.timerJob.cancel(NewInteractionException())
                }
            }

            var timeoutAction: (() -> Unit)? = null

            val timerJob = createTimerFlow(
                time = 6,
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

    private fun stopInteraction(peerId: Long, interactionJob: InteractionJob) {
        interactionsTimers[peerId] ?: return

        val newConversations = conversations.value.toMutableList()
        val conversationAndIndex =
            newConversations.findWithIndex { it.id == peerId } ?: return

        newConversations[conversationAndIndex.first] =
            conversationAndIndex.second.copy(
                interactionType = -1,
                interactionIds = emptyList()
            )

        _conversations.update { newConversations }
        syncUiConversation()

        interactionJob.timerJob.cancel()
        interactionsTimers[peerId] = null
    }

    private fun handleChatMajorChanged(event: LongPollParsedEvent.ChatMajorChanged) {
        val newConversations = conversations.value.toMutableList()
        val conversationIndex =
            newConversations.indexOfFirstOrNull { it.id == event.peerId }

        if (conversationIndex == null) { // диалога нет в списке
            // pizdets
        } else {
            newConversations[conversationIndex] =
                newConversations[conversationIndex].copy(majorId = event.majorId)

            _conversations.setValue { newConversations.sorted() }
            syncUiConversation()
        }
    }

    private fun handleChatMinorChanged(event: LongPollParsedEvent.ChatMinorChanged) {
        val newConversations = conversations.value.toMutableList()
        val conversationIndex =
            newConversations.indexOfFirstOrNull { it.id == event.peerId }

        if (conversationIndex == null) { // диалога нет в списке
            // pizdets
        } else {
            newConversations[conversationIndex] =
                newConversations[conversationIndex].copy(minorId = event.minorId)

            _conversations.setValue { newConversations.sorted() }
            syncUiConversation()
        }
    }

    private fun handleChatClearing(event: LongPollParsedEvent.ChatCleared) {
        val newConversations = conversations.value.toMutableList()

        val conversationIndex = newConversations.indexOfFirstOrNull { it.id == event.peerId }

        if (conversationIndex == null) { // диалога нет в списке
            // pizdets
        } else {
            newConversations.removeAt(conversationIndex)

            _conversations.setValue { newConversations.sorted() }
            syncUiConversation()
        }
    }

    private fun handleChatArchived(event: LongPollParsedEvent.ChatArchived) {
        val conversation = event.conversation

        val newConversations = conversations.value.toMutableList()

        when (filter) {
            ConversationsFilter.BUSINESS_NOTIFY -> Unit

            ConversationsFilter.ARCHIVE -> {
                if (event.archived) {
                    newConversations.add(0, conversation)
                } else {
                    val index = newConversations.indexOfFirstOrNull { it.id == conversation.id }
                    if (index == null) return

                    newConversations.removeAt(index)
                }

                _conversations.update { newConversations }
                syncUiConversation()
            }

            else -> {
                if (event.archived) {
                    val index = newConversations.indexOfFirstOrNull { it.id == conversation.id }
                    if (index == null) return

                    newConversations.removeAt(index)
                } else {
                    newConversations.add(pinnedConversationsCount.value, conversation)
                }

                _conversations.update { newConversations.sorted() }
                syncUiConversation()
            }
        }
    }

    private fun readConversation(peerId: Long, startMessageId: Long) {
        messagesUseCase.markAsRead(
            peerId = peerId,
            startMessageId = startMessageId
        ).listenValue(viewModelScope) { state ->
            state.processState(
                error = {},
                success = {
                    val newConversations = conversations.value.toMutableList()
                    val conversationIndex =
                        newConversations.indexOfFirstOrNull { it.id == peerId }
                            ?: return@listenValue

                    newConversations[conversationIndex] =
                        newConversations[conversationIndex].copy(inRead = startMessageId)

                    _conversations.update { newConversations }
                    syncUiConversation()
                }
            )
        }
    }

    private fun List<VkConversation>.sorted(): List<VkConversation> {
        val newConversations = toMutableList()

        val pinnedConversations = newConversations
            .filter(VkConversation::isPinned)
            .sortedWith { c1, c2 ->
                val diff = c2.majorId - c1.majorId

                if (diff == 0) {
                    c2.minorId - c1.minorId
                } else {
                    diff
                }
            }

        newConversations.removeAll(pinnedConversations)
        newConversations.sortWith { c1, c2 ->
            (c2.lastMessage?.date ?: 0) - (c1.lastMessage?.date ?: 0)
        }

        newConversations.addAll(0, pinnedConversations)
        return newConversations
    }

    private fun syncUiConversation(): List<UiConversation> {
        val conversations = conversations.value

        val newUiConversations = conversations.map { conversation ->
            val options = mutableListOf<ConversationOption>()
            conversation.lastMessage?.run {
                if (!conversation.isRead() && !this.isOut) {
                    options += ConversationOption.MarkAsRead
                }
            }

            val conversationsSize = this.conversations.value.size
            val pinnedCount = pinnedConversationsCount.value

            val canPinOneMoreDialog =
                conversationsSize > 4 && pinnedCount < 5 && !conversation.isPinned()

            if (conversation.isPinned()) {
                options += ConversationOption.Unpin
            } else if (canPinOneMoreDialog) {
                options += ConversationOption.Pin
            }

            when (filter) {
                ConversationsFilter.ARCHIVE -> ConversationOption.Unarchive

                ConversationsFilter.UNREAD,
                ConversationsFilter.ALL -> ConversationOption.Archive

                ConversationsFilter.BUSINESS_NOTIFY -> null
            }?.let(options::add)

            options += ConversationOption.Delete

            conversation.asPresentation(
                resources = resources,
                useContactName = useContactNames,
                isExpanded = expandedConversationId.value == conversation.id,
                options = options.toImmutableList()
            )
        }
        _uiConversations.setValue { newUiConversations }

        return newUiConversations
    }

    companion object {
        const val LOAD_COUNT = 30
    }
}
