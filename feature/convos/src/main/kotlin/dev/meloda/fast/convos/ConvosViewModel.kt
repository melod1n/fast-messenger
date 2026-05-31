package dev.meloda.fast.convos

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import com.conena.nanokt.collections.indexOfFirstOrNull
import dev.meloda.fast.common.NetworkStateListener
import dev.meloda.fast.common.VkConstants
import dev.meloda.fast.common.extensions.createTimerFlow
import dev.meloda.fast.common.extensions.findWithIndex
import dev.meloda.fast.common.extensions.listenValue
import dev.meloda.fast.common.extensions.setValue
import dev.meloda.fast.common.extensions.updateValue
import dev.meloda.fast.common.model.NetworkState
import dev.meloda.fast.convos.model.ConvoDialog
import dev.meloda.fast.convos.model.ConvoIntent
import dev.meloda.fast.convos.model.ConvoNavigationIntent
import dev.meloda.fast.convos.model.ConvosScreenState
import dev.meloda.fast.convos.model.InteractionJob
import dev.meloda.fast.convos.model.NewInteractionException
import dev.meloda.fast.data.UserConfig
import dev.meloda.fast.data.VkUtils
import dev.meloda.fast.data.processState
import dev.meloda.fast.datastore.UserSettings
import dev.meloda.fast.domain.ConvoUseCase
import dev.meloda.fast.domain.LoadConvosByIdUseCase
import dev.meloda.fast.domain.LongPollEventsHandler
import dev.meloda.fast.domain.MessagesUseCase
import dev.meloda.fast.domain.util.asPresentation
import dev.meloda.fast.domain.util.extractAvatar
import dev.meloda.fast.logger.FastLogger
import dev.meloda.fast.model.ConvosFilter
import dev.meloda.fast.model.InteractionType
import dev.meloda.fast.model.LongPollParsedEvent
import dev.meloda.fast.model.api.domain.VkConvo
import dev.meloda.fast.ui.model.vk.ConvoOption
import dev.meloda.fast.ui.model.vk.UiConvo
import dev.meloda.fast.ui.util.ImmutableList
import dev.meloda.fast.ui.util.ImmutableList.Companion.toImmutableList
import dev.meloda.fast.ui.util.buildImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn

@Immutable
class ConvosViewModel(
    eventsHandler: LongPollEventsHandler,
    val filter: ConvosFilter,
    private val convoUseCase: ConvoUseCase,
    private val messagesUseCase: MessagesUseCase,
    private val resources: Resources,
    private val userSettings: UserSettings,
    private val imageLoader: ImageLoader,
    private val applicationContext: Context,
    private val loadConvosByIdUseCase: LoadConvosByIdUseCase,
    private val networkStateListener: NetworkStateListener,
    private val logger: FastLogger
) : ViewModel() {

    private val screenState = MutableStateFlow(ConvosScreenState.EMPTY)
    val screenStateFlow get() = screenState.asStateFlow()

    private val navigationIntent = MutableStateFlow<ConvoNavigationIntent?>(null)
    val navigationIntentFlow get() = navigationIntent.asStateFlow()

    private val convos: MutableList<VkConvo> = mutableListOf()

    private val pinnedConvosCount get() = convos.count(VkConvo::isPinned)

    private var currentOffset = 0

    private val interactionsTimers = hashMapOf<Long, InteractionJob?>()

    init {
        loadConvos()

        eventsHandler.onMessageNew(::handleNewMessage)
        eventsHandler.onMessageEdit(::handleEditedMessage)
        eventsHandler.onMessageIncomingRead(::handleReadIncomingMessage)
        eventsHandler.onMessageOutgoingRead(::handleReadOutgoingMessage)
        eventsHandler.onInteraction(::handleInteraction)
        eventsHandler.onChatMajorChange(::handleChatMajorChanged)
        eventsHandler.onChatMinorChange(::handleChatMinorChanged)
        eventsHandler.onChatClear(::handleChatClearing)
        eventsHandler.onChatArchive(::handleChatArchived)

        userSettings.useContactNames.listenValue(viewModelScope) {
            syncUiConvos()
        }

        networkStateListener.networkStateFlow.listenValue { state ->
            logger.debug(this@ConvosViewModel::class, "network state changed: $state")

            if (state == NetworkState.CONNECTED) {
                if (screenState.value.error != null) {
                    onRefresh()
                }
            }
        }
    }

    fun handleIntent(intent: ConvoIntent) {
        when (intent) {
            ConvoIntent.ArchiveClick -> {
                navigationIntent.setValue { ConvoNavigationIntent.Archive }
            }

            ConvoIntent.Back -> {
                navigationIntent.setValue { ConvoNavigationIntent.Back }
            }

            ConvoIntent.ConsumeScrollToTop -> Unit
            ConvoIntent.CreateChatClick -> {
                navigationIntent.setValue { ConvoNavigationIntent.CreateChat }
            }

            ConvoIntent.ErrorActionButtonClick -> {
                onRefresh()
            }

            is ConvoIntent.ItemClick -> {
                onConvoItemClick(intent.convoId)
            }

            is ConvoIntent.ItemLongClick -> {
                onConvoItemLongClick(intent.convoId)
            }

            is ConvoIntent.OptionItemClick -> {
                onOptionClicked(intent.option)
            }

            ConvoIntent.PaginationConditionsMet -> {
                onPaginationConditionsMet()
            }

            ConvoIntent.Refresh -> {
                onRefresh()
            }

            is ConvoIntent.SetScrollIndex -> {
                setScrollIndex(intent.index)
            }

            is ConvoIntent.SetScrollOffset -> {
                setScrollOffset(intent.offset)
            }

            is ConvoIntent.Dialog -> {
                when (intent) {
                    is ConvoIntent.Dialog.Cancel -> Unit
                    is ConvoIntent.Dialog.Confirm -> onDialogConfirmed(intent.bundle)
                    ConvoIntent.Dialog.Dismiss -> onDialogDismissed()
                }
            }
        }
    }

    fun onNavigationConsumed() {
        navigationIntent.setValue { null }
    }

    private fun onDialogConfirmed(bundle: Bundle?) {
        val dialog = screenState.value.dialog ?: return
        onDialogDismissed()

        val convo = with(screenState.value) {
            convos.find { it.id == expandedConvoId }
        } ?: return

        when (dialog) {
            is ConvoDialog.Delete -> {
                deleteConvo(convo.id)
            }

            is ConvoDialog.Pin -> {
                pinConvo(convo.id, true)
            }

            is ConvoDialog.Unpin -> {
                pinConvo(convo.id, false)
            }

            is ConvoDialog.Archive -> {
                archiveConvo(convo.id, true)
            }

            is ConvoDialog.Unarchive -> {
                archiveConvo(convo.id, false)
            }
        }

        collapseConvos(false)
        syncUiConvos()
    }

    private fun onDialogDismissed() {
        screenState.updateValue { copy(dialog = null) }
    }

    private fun onPaginationConditionsMet() {
        currentOffset = convos.size
        loadConvos()
    }

    private fun clearError() {
        screenState.updateValue { copy(error = null) }
    }

    private fun onRefresh() {
        clearError()
        loadConvos(offset = 0)
    }

    private fun onConvoItemClick(convoId: Long) {
        collapseConvos()
        navigationIntent.setValue { ConvoNavigationIntent.MessagesHistory(convoId) }
    }

    private fun onConvoItemLongClick(convoId: Long) {
        val isExpanded = screenState.value.convos.find { it.id == convoId }?.isExpanded == true

        screenState.updateValue { copy(expandedConvoId = if (isExpanded) 0L else convoId) }
        syncUiConvos()
    }

    private fun onOptionClicked(option: ConvoOption) {
        val convo =
            screenState.value.convos.find { it.id == screenState.value.expandedConvoId } ?: return

        when (option) {
            ConvoOption.Delete -> setDialog(ConvoDialog.Delete)

            ConvoOption.MarkAsRead -> {
                val lastMessageId =
                    screenState.value.convos.find { it.id == screenState.value.expandedConvoId }?.lastMessageId

                if (lastMessageId != null) {
                    readConvo(
                        peerId = convo.id,
                        startMessageId = lastMessageId
                    )
                    collapseConvos()
                }
            }

            ConvoOption.Pin -> setDialog(ConvoDialog.Pin)
            ConvoOption.Unpin -> setDialog(ConvoDialog.Unpin)
            ConvoOption.Archive -> setDialog(ConvoDialog.Archive)
            ConvoOption.Unarchive -> setDialog(ConvoDialog.Unarchive)
        }
    }

    private fun setScrollIndex(index: Int) {
        screenState.setValue { old -> old.copy(scrollIndex = index) }
    }

    private fun setScrollOffset(offset: Int) {
        screenState.setValue { old -> old.copy(scrollOffset = offset) }
    }

    private fun setDialog(dialog: ConvoDialog?) {
        screenState.updateValue { copy(dialog = dialog) }
    }

    private fun replaceConvos(newConvos: List<VkConvo>) {
        convos.clear()
        convos.addAll(newConvos)
    }

    private fun collapseConvos(sync: Boolean = true) {
        screenState.updateValue { copy(expandedConvoId = null) }

        if (sync) {
            syncUiConvos()
        }
    }

    private fun loadConvos(offset: Int = currentOffset) {
        convoUseCase.getConvos(
            count = LOAD_COUNT,
            offset = offset,
            filter = filter
        ).listenValue(viewModelScope) { state ->
            state.processState(
                error = { error ->
                    screenState.updateValue { copy(error = VkUtils.parseError(error)) }
                },
                success = { response ->
                    val newConvos = if (offset == 0) {
                        response
                    } else {
                        convos.plus(response)
                    }

                    val itemsCountSufficient = response.size == LOAD_COUNT

                    val paginationExhausted = !itemsCountSufficient && convos.isNotEmpty()

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

                    replaceConvos(newConvos)

                    screenState.updateValue { copy(canPaginate = itemsCountSufficient) }
                    syncUiConvos()
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
                    val newConvos = convos.toMutableList()
                    val convoIndex =
                        newConvos.indexOfFirstOrNull { it.id == peerId }
                            ?: return@processState

                    newConvos.removeAt(convoIndex)
                    replaceConvos(newConvos.sorted())
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
                                    pinnedConvosCount.plus(1) * 16
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
                        convos.find { it.id == peerId }?.let { convo ->
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
    private fun handleNewMessage(event: LongPollParsedEvent.MessageNew) {
        val message = event.message

        val newConvos = convos.toMutableList()
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

                        newConvos.add(pinnedConvosCount, convo)
                        replaceConvos(newConvos.sorted())
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
                newConvos.add(pinnedConvosCount, newConvo)
            }

            replaceConvos(newConvos.sorted())
            syncUiConvos()
        }
    }

    private fun handleEditedMessage(event: LongPollParsedEvent.MessageEdited) {
        val message = event.message
        val newConvos = convos.toMutableList()

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

            replaceConvos(newConvos)
            syncUiConvos()
        }
    }

    private fun handleReadIncomingMessage(event: LongPollParsedEvent.IncomingMessageRead) {
        val newConvos = convos.toMutableList()

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

            replaceConvos(newConvos)
            syncUiConvos()
        }
    }

    private fun handleReadOutgoingMessage(event: LongPollParsedEvent.OutgoingMessageRead) {
        val newConvos = convos.toMutableList()

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

            replaceConvos(newConvos)
            syncUiConvos()
        }
    }

    private fun handleInteraction(event: LongPollParsedEvent.Interaction) {
        val interactionType = event.interactionType
        val peerId = event.peerId
        val userIds = event.userIds

        val newConvos = convos.toMutableList()
        val convoAndIndex =
            newConvos.findWithIndex { it.id == peerId }

        if (convoAndIndex != null) {
            newConvos[convoAndIndex.first] =
                convoAndIndex.second.copy(
                    interactionType = interactionType.value,
                    interactionIds = userIds
                )

            replaceConvos(newConvos)
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

        val newConvos = convos.toMutableList()
        val convoAndIndex =
            newConvos.findWithIndex { it.id == peerId } ?: return

        newConvos[convoAndIndex.first] =
            convoAndIndex.second.copy(
                interactionType = -1,
                interactionIds = emptyList()
            )

        replaceConvos(newConvos)
        syncUiConvos()

        interactionJob.timerJob.cancel()
        interactionsTimers[peerId] = null
    }

    private fun handleChatMajorChanged(event: LongPollParsedEvent.ChatMajorChanged) {
        val newConvos = convos.toMutableList()
        val convoIndex =
            newConvos.indexOfFirstOrNull { it.id == event.peerId }

        if (convoIndex == null) { // диалога нет в списке
            // pizdets
        } else {
            newConvos[convoIndex] =
                newConvos[convoIndex].copy(majorId = event.majorId)

            replaceConvos(newConvos.sorted())
            syncUiConvos()
        }
    }

    private fun handleChatMinorChanged(event: LongPollParsedEvent.ChatMinorChanged) {
        val newConvos = convos.toMutableList()
        val convoIndex =
            newConvos.indexOfFirstOrNull { it.id == event.peerId }

        if (convoIndex == null) { // диалога нет в списке
            // pizdets
        } else {
            newConvos[convoIndex] =
                newConvos[convoIndex].copy(minorId = event.minorId)

            replaceConvos(newConvos.sorted())
            syncUiConvos()
        }
    }

    private fun handleChatClearing(event: LongPollParsedEvent.ChatCleared) {
        val newConvos = convos.toMutableList()

        val convoIndex = newConvos.indexOfFirstOrNull { it.id == event.peerId }

        if (convoIndex == null) { // диалога нет в списке
            // pizdets
        } else {
            // TODO: 30.05.2026, Danil Nikolaev: reimplement
            newConvos.removeAt(convoIndex)

            replaceConvos(newConvos.sorted())
            syncUiConvos()
        }
    }

    private fun handleChatArchived(event: LongPollParsedEvent.ChatArchived) {
        val convo = event.convo

        val newConvos = convos.toMutableList()

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

                replaceConvos(newConvos)
                syncUiConvos()
            }

            else -> {
                if (event.archived) {
                    val index = newConvos.indexOfFirstOrNull { it.id == convo.id }
                    if (index == null) return

                    newConvos.removeAt(index)
                } else {
                    newConvos.add(pinnedConvosCount, convo)
                }

                replaceConvos(newConvos.sorted())
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
                    val newConvos = convos.toMutableList()
                    val convoIndex =
                        newConvos.indexOfFirstOrNull { it.id == peerId }
                            ?: return@listenValue

                    newConvos[convoIndex] =
                        newConvos[convoIndex].copy(inRead = startMessageId)

                    replaceConvos(newConvos)
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
        val newUiConvos = convos.map { convo ->
            val options: ImmutableList<ConvoOption> = buildImmutableList {
                if (!convo.isRead() && convo.lastMessage != null && convo.lastMessage?.isOut == false) {
                    add(ConvoOption.MarkAsRead)
                }

                if (convo.isPinned()) {
                    add(ConvoOption.Unpin)
                }

                if (convos.size > 4 && pinnedConvosCount < 5 && !convo.isPinned()) {
                    add(ConvoOption.Pin)
                }

                when (filter) {
                    ConvosFilter.BUSINESS_NOTIFY -> Unit
                    ConvosFilter.ARCHIVE -> add(ConvoOption.Unarchive)

                    ConvosFilter.ALL,
                    ConvosFilter.UNREAD -> {
                        if (convo.id != UserConfig.userId) {
                            add(ConvoOption.Archive)
                        }
                    }
                }

                add(ConvoOption.Delete)
            }

            convo.asPresentation(
                resources = resources,
                useContactName = userSettings.useContactNames.value,
                isExpanded = screenState.value.expandedConvoId == convo.id,
                options = options
            )
        }

        screenState.updateValue { copy(convos = newUiConvos.toImmutableList()) }

        return newUiConvos
    }

    companion object {
        const val LOAD_COUNT = 30
    }
}
