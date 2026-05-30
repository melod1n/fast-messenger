package dev.meloda.fast.convos

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
import dev.meloda.fast.convos.model.ConvoDialog
import dev.meloda.fast.convos.model.ConvoNavigation
import dev.meloda.fast.convos.model.ConvosScreenState
import dev.meloda.fast.convos.model.InteractionJob
import dev.meloda.fast.convos.model.NewInteractionException
import dev.meloda.fast.data.VkUtils
import dev.meloda.fast.data.processState
import dev.meloda.fast.datastore.UserSettings
import dev.meloda.fast.domain.ConvoUseCase
import dev.meloda.fast.domain.LoadConvosByIdUseCase
import dev.meloda.fast.domain.LongPollUpdatesParser
import dev.meloda.fast.domain.MessagesUseCase
import dev.meloda.fast.domain.util.asPresentation
import dev.meloda.fast.domain.util.extractAvatar
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.model.ConvosFilter
import dev.meloda.fast.model.InteractionType
import dev.meloda.fast.model.LongPollParsedEvent
import dev.meloda.fast.model.api.domain.VkConvo
import dev.meloda.fast.ui.model.vk.ConvoOption
import dev.meloda.fast.ui.model.vk.UiConvo
import dev.meloda.fast.ui.util.ImmutableList.Companion.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class ConvosViewModel(
    updatesParser: LongPollUpdatesParser,
    private val filter: ConvosFilter,
    private val convoUseCase: ConvoUseCase,
    private val messagesUseCase: MessagesUseCase,
    private val resources: Resources,
    private val userSettings: UserSettings,
    private val imageLoader: ImageLoader,
    private val applicationContext: Context,
    private val loadConvosByIdUseCase: LoadConvosByIdUseCase
) : ViewModel() {

    private val screenState = MutableStateFlow(ConvosScreenState.EMPTY)
    val screenStateFlow get() = screenState.asStateFlow()

    private val navigation = MutableStateFlow<ConvoNavigation?>(null)
    val navigationFlow get() = navigation.asStateFlow()

    private val dialog = MutableStateFlow<ConvoDialog?>(null)
    val dialogFlow get() = dialog.asStateFlow()

    private val convos = MutableStateFlow<List<VkConvo>>(emptyList())
    val convosFlow get() = convos.asStateFlow()

    private val uiConvos = MutableStateFlow<List<UiConvo>>(emptyList())
    val uiConvosFlow get() = uiConvos.asStateFlow()

    private val pinnedConvosCount = convosFlow.map { convos ->
        convos.count(VkConvo::isPinned)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val baseError = MutableStateFlow<BaseError?>(null)
    val baseErrorFlow get() = baseError.asStateFlow()

    private val currentOffset = MutableStateFlow(0)
    val currentOffsetFlow get() = currentOffset.asStateFlow()

    private val canPaginate = MutableStateFlow(false)
    val canPaginateFlow get() = canPaginate.asStateFlow()

    private val expandedConvoId = MutableStateFlow(0L)

    private val useContactNames: Boolean get() = userSettings.useContactNames.value

    private val interactionsTimers = hashMapOf<Long, InteractionJob?>()

    init {
        screenState.updateValue { copy(isArchive = filter == ConvosFilter.ARCHIVE) }

        loadConvos()

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
            syncUiConvos()
        }
    }

    fun onNavigationConsumed() {
        navigation.setValue { null }
    }

    fun onDialogConfirmed(dialog: ConvoDialog, bundle: Bundle) {
        onDialogDismissed(dialog)

        when (dialog) {
            is ConvoDialog.ConvoDelete -> {
                deleteConvo(dialog.convoId)
            }

            is ConvoDialog.ConvoPin -> {
                pinConvo(dialog.convoId, true)
            }

            is ConvoDialog.ConvoUnpin -> {
                pinConvo(dialog.convoId, false)
            }

            is ConvoDialog.ConvoArchive -> {
                archiveConvo(dialog.convoId, true)
            }

            is ConvoDialog.ConvoUnarchive -> {
                archiveConvo(dialog.convoId, false)
            }
        }

        expandedConvoId.setValue { 0 }
        syncUiConvos()
    }

    fun onDialogDismissed(dialog: ConvoDialog) {
        this.dialog.setValue { null }
    }

    fun onDialogItemPicked(dialog: ConvoDialog, bundle: Bundle) {
        when (dialog) {
            is ConvoDialog.ConvoDelete -> Unit
            is ConvoDialog.ConvoPin -> Unit
            is ConvoDialog.ConvoUnpin -> Unit
            is ConvoDialog.ConvoArchive -> Unit
            is ConvoDialog.ConvoUnarchive -> Unit
        }
    }

    fun onErrorButtonClicked() {
        when (baseErrorFlow.value) {
            null -> Unit

            is BaseError.ConnectionError,
            is BaseError.InternalError,
            is BaseError.SimpleError,
            is BaseError.UnknownError -> onRefresh()

            else -> Unit
        }
    }

    fun onPaginationConditionsMet() {
        currentOffset.update { convosFlow.value.size }
        loadConvos()
    }

    fun onRefresh() {
        onErrorConsumed()
        loadConvos(offset = 0)
    }

    fun onConvoItemClick(convo: UiConvo) {
        collapseConvos()
        navigation.setValue { ConvoNavigation.MessagesHistory(peerId = convo.id) }
    }

    fun onConvoItemLongClick(convo: UiConvo) {
        expandedConvoId.setValue {
            if (convo.isExpanded) 0
            else convo.id
        }
        syncUiConvos()
    }

    fun onOptionClicked(
        convo: UiConvo,
        option: ConvoOption
    ) {
        when (option) {
            ConvoOption.Delete -> {
                dialog.setValue { ConvoDialog.ConvoDelete(convo.id) }
            }

            ConvoOption.MarkAsRead -> {
                convo.lastMessageId?.let { lastMessageId ->
                    readConvo(
                        peerId = convo.id,
                        startMessageId = lastMessageId
                    )
                    collapseConvos()
                }
            }

            ConvoOption.Pin -> {
                dialog.setValue { ConvoDialog.ConvoPin(convo.id) }
            }

            ConvoOption.Unpin -> {
                dialog.setValue { ConvoDialog.ConvoUnpin(convo.id) }
            }

            ConvoOption.Archive -> {
                dialog.setValue { ConvoDialog.ConvoArchive(convo.id) }
            }

            ConvoOption.Unarchive -> {
                dialog.setValue { ConvoDialog.ConvoUnarchive(convo.id) }
            }
        }
    }

    fun onErrorConsumed() {
        baseError.setValue { null }
    }

    fun setScrollIndex(index: Int) {
        screenState.setValue { old -> old.copy(scrollIndex = index) }
    }

    fun setScrollOffset(offset: Int) {
        screenState.setValue { old -> old.copy(scrollOffset = offset) }
    }

    fun onCreateChatButtonClicked() {
        navigation.setValue { ConvoNavigation.CreateChat }
    }

    private fun collapseConvos() {
        expandedConvoId.setValue { 0 }
        syncUiConvos()
    }

    private fun loadConvos(
        offset: Int = currentOffsetFlow.value
    ) {
        convoUseCase.getConvos(
            count = LOAD_COUNT,
            offset = offset,
            filter = filter
        ).listenValue(viewModelScope) { state ->
            state.processState(
                error = { error ->
                    val newBaseError = VkUtils.parseError(error)
                    baseError.update { newBaseError }
                },
                success = { response ->
                    val convos = response
                    val fullConvos = if (offset == 0) {
                        convos
                    } else {
                        this.convosFlow.value.plus(convos)
                    }

                    val itemsCountSufficient = response.size == LOAD_COUNT

                    val paginationExhausted = !itemsCountSufficient &&
                            this.convosFlow.value.isNotEmpty()

                    screenState.updateValue {
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

                    convoUseCase.storeConvos(response)

                    this.convos.emit(fullConvos)
                    syncUiConvos()
                    canPaginate.setValue { itemsCountSufficient }
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

    private fun deleteConvo(peerId: Long) {
        convoUseCase.delete(peerId).listenValue(viewModelScope) { state ->
            state.processState(
                error = {},
                success = {
                    val newConvos = convosFlow.value.toMutableList()
                    val convoIndex =
                        newConvos.indexOfFirstOrNull { it.id == peerId }
                            ?: return@processState

                    newConvos.removeAt(convoIndex)
                    convos.update { newConvos.sorted() }
                    syncUiConvos()
                }
            )
            screenState.emit(screenStateFlow.value.copy(isLoading = state.isLoading()))
        }
    }

    private fun pinConvo(peerId: Long, pin: Boolean) {
        convoUseCase.changePinState(peerId, pin)
            .listenValue(viewModelScope) { state ->
                state.processState(
                    error = {},
                    success = {
                        handleChatMajorChanged(
                            LongPollParsedEvent.ChatMajorChanged(
                                peerId = peerId,
                                majorId = if (pin) {
                                    pinnedConvosCount.value.plus(1) * 16
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

    private fun archiveConvo(peerId: Long, archive: Boolean) {
        convoUseCase.changeArchivedState(peerId, archive)
            .listenValue(viewModelScope) { state ->
                state.processState(
                    error = {},
                    success = {
                        convosFlow.value.find { it.id == peerId }?.let { convo ->
                            handleChatArchived(
                                LongPollParsedEvent.ChatArchived(
                                    convo = convo,
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

        val newConvos = convosFlow.value.toMutableList()
        val convoIndex =
            newConvos.indexOfFirstOrNull { it.id == message.peerId }

        if (convoIndex == null) {
            if (event.inArchive != (filter == ConvosFilter.ARCHIVE)) return

            loadConvosByIdUseCase(
                peerIds = listOf(message.peerId),
                extended = true,
                fields = VkConstants.ALL_FIELDS
            ).listenValue(viewModelScope) { state ->
                state.processState(
                    error = {},
                    success = { response ->
                        val convo = (response.firstOrNull() ?: return@listenValue)
                            .copy(lastMessage = message)

                        newConvos.add(pinnedConvosCount.value, convo)
                        convos.update { newConvos.sorted() }
                        syncUiConvos()
                    }
                )
            }
        } else {
            val convo = newConvos[convoIndex]
            var newConvo = convo.copy(
                lastMessage = message,
                lastMessageId = message.id,
                lastCmId = message.cmId,
                unreadCount = if (message.isOut) convo.unreadCount
                else convo.unreadCount + 1
            )

            interactionsTimers[convo.id]?.let { job ->
                if (job.interactionType == InteractionType.Typing
                    && message.fromId in convo.interactionIds
                ) {
                    val newInteractionIds = newConvo.interactionIds.filter { id ->
                        id != message.fromId
                    }

                    newConvo = newConvo.copy(
                        interactionType = if (newInteractionIds.isEmpty()) -1 else {
                            newConvo.interactionType
                        },
                        interactionIds = newInteractionIds
                    )
                }
            }

            if (convo.isPinned()) {
                newConvos[convoIndex] = newConvo
            } else {
                newConvos.removeAt(convoIndex)

                val toPosition = pinnedConvosCount.value
                newConvos.add(toPosition, newConvo)
            }

            convos.update { newConvos.sorted() }
            syncUiConvos()
        }
    }

    private fun handleEditedMessage(event: LongPollParsedEvent.MessageEdited) {
        val message = event.message
        val newConvos = convosFlow.value.toMutableList()

        val convoIndex = newConvos.indexOfFirstOrNull { it.id == message.peerId }
        if (convoIndex == null) { // диалога нет в списке
            //  pizdets
        } else {
            val convo = newConvos[convoIndex]
            newConvos[convoIndex] = convo.copy(
                lastMessage = message,
                lastMessageId = message.id,
                lastCmId = message.cmId
            )
            convos.update { newConvos }
            syncUiConvos()
        }
    }

    private fun handleReadIncomingMessage(event: LongPollParsedEvent.IncomingMessageRead) {
        val newConvos = convosFlow.value.toMutableList()

        val convoIndex =
            newConvos.indexOfFirstOrNull { it.id == event.peerId }

        if (convoIndex == null) { // диалога нет в списке
            // pizdets
        } else {
            newConvos[convoIndex] =
                newConvos[convoIndex].copy(
                    inReadCmId = event.cmId,
                    unreadCount = event.unreadCount
                )

            convos.update { newConvos }
            syncUiConvos()
        }
    }

    private fun handleReadOutgoingMessage(event: LongPollParsedEvent.OutgoingMessageRead) {
        val newConvos = convosFlow.value.toMutableList()

        val convoIndex =
            newConvos.indexOfFirstOrNull { it.id == event.peerId }

        if (convoIndex == null) { // диалога нет в списке
            // pizdets
        } else {
            newConvos[convoIndex] =
                newConvos[convoIndex].copy(
                    outReadCmId = event.cmId,
                    unreadCount = event.unreadCount
                )

            convos.update { newConvos }
            syncUiConvos()
        }
    }

    private fun handleInteraction(event: LongPollParsedEvent.Interaction) {
        val interactionType = event.interactionType
        val peerId = event.peerId
        val userIds = event.userIds

        val newConvos = convosFlow.value.toMutableList()
        val convoAndIndex =
            newConvos.findWithIndex { it.id == peerId }

        if (convoAndIndex != null) {
            newConvos[convoAndIndex.first] =
                convoAndIndex.second.copy(
                    interactionType = interactionType.value,
                    interactionIds = userIds
                )

            convos.update { newConvos }
            syncUiConvos()

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

        val newConvos = convosFlow.value.toMutableList()
        val convoAndIndex =
            newConvos.findWithIndex { it.id == peerId } ?: return

        newConvos[convoAndIndex.first] =
            convoAndIndex.second.copy(
                interactionType = -1,
                interactionIds = emptyList()
            )

        convos.update { newConvos }
        syncUiConvos()

        interactionJob.timerJob.cancel()
        interactionsTimers[peerId] = null
    }

    private fun handleChatMajorChanged(event: LongPollParsedEvent.ChatMajorChanged) {
        val newConvos = convosFlow.value.toMutableList()
        val convoIndex =
            newConvos.indexOfFirstOrNull { it.id == event.peerId }

        if (convoIndex == null) { // диалога нет в списке
            // pizdets
        } else {
            newConvos[convoIndex] =
                newConvos[convoIndex].copy(majorId = event.majorId)

            convos.setValue { newConvos.sorted() }
            syncUiConvos()
        }
    }

    private fun handleChatMinorChanged(event: LongPollParsedEvent.ChatMinorChanged) {
        val newConvos = convosFlow.value.toMutableList()
        val convoIndex =
            newConvos.indexOfFirstOrNull { it.id == event.peerId }

        if (convoIndex == null) { // диалога нет в списке
            // pizdets
        } else {
            newConvos[convoIndex] =
                newConvos[convoIndex].copy(minorId = event.minorId)

            convos.setValue { newConvos.sorted() }
            syncUiConvos()
        }
    }

    private fun handleChatClearing(event: LongPollParsedEvent.ChatCleared) {
        val newConvos = convosFlow.value.toMutableList()

        val convoIndex = newConvos.indexOfFirstOrNull { it.id == event.peerId }

        if (convoIndex == null) { // диалога нет в списке
            // pizdets
        } else {
            newConvos.removeAt(convoIndex)

            convos.setValue { newConvos.sorted() }
            syncUiConvos()
        }
    }

    private fun handleChatArchived(event: LongPollParsedEvent.ChatArchived) {
        val convo = event.convo

        val newConvos = convosFlow.value.toMutableList()

        when (filter) {
            ConvosFilter.BUSINESS_NOTIFY -> Unit

            ConvosFilter.ARCHIVE -> {
                if (event.archived) {
                    newConvos.add(0, convo)
                } else {
                    val index = newConvos.indexOfFirstOrNull { it.id == convo.id }
                    if (index == null) return

                    newConvos.removeAt(index)
                }

                convos.update { newConvos }
                syncUiConvos()
            }

            else -> {
                if (event.archived) {
                    val index = newConvos.indexOfFirstOrNull { it.id == convo.id }
                    if (index == null) return

                    newConvos.removeAt(index)
                } else {
                    newConvos.add(pinnedConvosCount.value, convo)
                }

                convos.update { newConvos.sorted() }
                syncUiConvos()
            }
        }
    }

    private fun readConvo(peerId: Long, startMessageId: Long) {
        messagesUseCase.markAsRead(
            peerId = peerId,
            startMessageId = startMessageId
        ).listenValue(viewModelScope) { state ->
            state.processState(
                error = {},
                success = {
                    val newConvos = convosFlow.value.toMutableList()
                    val convoIndex =
                        newConvos.indexOfFirstOrNull { it.id == peerId }
                            ?: return@listenValue

                    newConvos[convoIndex] =
                        newConvos[convoIndex].copy(inRead = startMessageId)

                    convos.update { newConvos }
                    syncUiConvos()
                }
            )
        }
    }

    private fun List<VkConvo>.sorted(): List<VkConvo> {
        val newConvos = toMutableList()

        val pinnedConvos = newConvos
            .filter(VkConvo::isPinned)
            .sortedWith { c1, c2 ->
                val diff = c2.majorId - c1.majorId

                if (diff == 0) {
                    c2.minorId - c1.minorId
                } else {
                    diff
                }
            }

        newConvos.removeAll(pinnedConvos)
        newConvos.sortWith { c1, c2 ->
            (c2.lastMessage?.date ?: 0) - (c1.lastMessage?.date ?: 0)
        }

        newConvos.addAll(0, pinnedConvos)
        return newConvos
    }

    private fun syncUiConvos(): List<UiConvo> {
        val convos = convosFlow.value

        val newUiConvos = convos.map { convo ->
            val options = mutableListOf<ConvoOption>()
            convo.lastMessage?.run {
                if (!convo.isRead() && !this.isOut) {
                    options += ConvoOption.MarkAsRead
                }
            }

            val convosSize = this.convosFlow.value.size
            val pinnedCount = pinnedConvosCount.value

            val canPinOneMoreDialog =
                convosSize > 4 && pinnedCount < 5 && !convo.isPinned()

            if (convo.isPinned()) {
                options += ConvoOption.Unpin
            } else if (canPinOneMoreDialog) {
                options += ConvoOption.Pin
            }

            when (filter) {
                ConvosFilter.ARCHIVE -> ConvoOption.Unarchive

                ConvosFilter.UNREAD,
                ConvosFilter.ALL -> ConvoOption.Archive

                ConvosFilter.BUSINESS_NOTIFY -> null
            }?.let(options::add)

            options += ConvoOption.Delete

            convo.asPresentation(
                resources = resources,
                useContactName = useContactNames,
                isExpanded = expandedConvoId.value == convo.id,
                options = options.toImmutableList()
            )
        }
        uiConvos.setValue { newUiConvos }

        return newUiConvos
    }

    companion object {
        const val LOAD_COUNT = 30
    }
}
