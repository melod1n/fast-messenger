package dev.meloda.fast.messageshistory

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.imageLoader
import coil.request.ImageRequest
import com.conena.nanokt.collections.indexOfFirstOrNull
import dev.meloda.fast.common.VkConstants
import dev.meloda.fast.common.extensions.getParcelableCompat
import dev.meloda.fast.common.extensions.listenValue
import dev.meloda.fast.common.extensions.orDots
import dev.meloda.fast.common.extensions.removeIfCompat
import dev.meloda.fast.common.extensions.setValue
import dev.meloda.fast.common.provider.ResourceProvider
import dev.meloda.fast.data.State
import dev.meloda.fast.data.UserConfig
import dev.meloda.fast.data.VkMemoryCache
import dev.meloda.fast.data.processState
import dev.meloda.fast.datastore.AppSettings
import dev.meloda.fast.datastore.UserSettings
import dev.meloda.fast.domain.ConversationsUseCase
import dev.meloda.fast.domain.GetMessageReadPeersUseCase
import dev.meloda.fast.domain.LoadConversationsByIdUseCase
import dev.meloda.fast.domain.LongPollUpdatesParser
import dev.meloda.fast.domain.MessagesUseCase
import dev.meloda.fast.messageshistory.model.ActionMode
import dev.meloda.fast.messageshistory.model.MessageDialog
import dev.meloda.fast.messageshistory.model.MessageNavigation
import dev.meloda.fast.messageshistory.model.MessageOption
import dev.meloda.fast.messageshistory.model.MessagesHistoryScreenState
import dev.meloda.fast.messageshistory.model.UiItem
import dev.meloda.fast.messageshistory.navigation.MessagesHistory
import dev.meloda.fast.messageshistory.util.asPresentation
import dev.meloda.fast.messageshistory.util.extractAvatar
import dev.meloda.fast.messageshistory.util.extractTitle
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.model.LongPollParsedEvent
import dev.meloda.fast.model.api.domain.FormatDataType
import dev.meloda.fast.model.api.domain.VkAttachment
import dev.meloda.fast.model.api.domain.VkMessage
import dev.meloda.fast.model.api.domain.VkPhotoDomain
import dev.meloda.fast.network.VkErrorCode
import dev.meloda.fast.ui.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.abs
import kotlin.random.Random

class MessagesHistoryViewModelImpl(
    private val applicationContext: Context,
    private val messagesUseCase: MessagesUseCase,
    private val conversationsUseCase: ConversationsUseCase,
    private val resourceProvider: ResourceProvider,
    private val userSettings: UserSettings,
    private val loadConversationsByIdUseCase: LoadConversationsByIdUseCase,
    private val getMessageReadPeersUseCase: GetMessageReadPeersUseCase,
    updatesParser: LongPollUpdatesParser,
    savedStateHandle: SavedStateHandle
) : MessagesHistoryViewModel, ViewModel() {

    override val screenState = MutableStateFlow(MessagesHistoryScreenState.EMPTY)
    override val navigation = MutableStateFlow<MessageNavigation?>(null)
    override val dialog = MutableStateFlow<MessageDialog?>(null)
    override val selectedMessages = MutableStateFlow<List<VkMessage>>(emptyList())

    override val inputFieldFocusRequester = MutableStateFlow(false)

    override val isNeedToScrollToIndex = MutableStateFlow<Int?>(null)

    override val baseError = MutableStateFlow<BaseError?>(null)
    override val imagesToPreload = MutableStateFlow<List<String>>(emptyList())

    override val currentOffset = MutableStateFlow(0)

    override val canPaginate = MutableStateFlow(false)

    override val messages = MutableStateFlow<List<VkMessage>>(emptyList())
    override val uiMessages = MutableStateFlow<List<UiItem>>(emptyList())

    private var lastMessageText: String? = null

    private val sendingMessages: MutableList<VkMessage> = mutableListOf()
    private val failedMessages: MutableList<VkMessage> = mutableListOf()

    private var replyToCmId: Long? = null

    init {
        val arguments = MessagesHistory.from(savedStateHandle).arguments

        screenState.setValue { old -> old.copy(conversationId = arguments.conversationId) }

        loadConversation()
        loadMessagesHistory()

        updatesParser.onNewMessage(::handleNewMessage)
        updatesParser.onMessageEdited(::handleEditedMessage)
        updatesParser.onMessageIncomingRead(::handleReadIncomingEvent)
        updatesParser.onMessageOutgoingRead(::handleReadOutgoingEvent)
        updatesParser.onMessageDeleted(::handleMessageDeleted)
        updatesParser.onMessageRestored(::handleMessageRestored)
        updatesParser.onMessageMarkedAsImportant(::handleMessageMarkedAsImportant)
        updatesParser.onMessageMarkedAsSpam(::handleMessageMarkedAsSpam)
        updatesParser.onMessageMarkedAsNotSpam(::handleMessageMarkedAsNotSpam)
    }

    override fun onNavigationConsumed() {
        navigation.setValue { null }
    }

    override fun onTopBarClicked() {
        val cmId = messages.value.firstOrNull()?.cmId ?: return

        navigation.setValue {
            MessageNavigation.ChatMaterials(
                peerId = screenState.value.conversationId,
                cmId = cmId
            )
        }
    }

    override fun onDialogConfirmed(dialog: MessageDialog, bundle: Bundle) {
        onDialogDismissed(dialog)

        when (dialog) {
            is MessageDialog.MessageOptions -> Unit

            is MessageDialog.MessageDelete -> {
                val deleteForEveryone = bundle.getBoolean("everyone")

                if (dialog.message.id <= 0) {
                    val newMessages = messages.value.toMutableList()
                    newMessages.remove(dialog.message)
                    messages.setValue { newMessages }
                    syncUiMessages()
                    return
                }

                deleteMessage(
                    messageIds = listOf(dialog.message.id),
                    deleteForAll = deleteForEveryone
                )
            }

            is MessageDialog.MessagesDelete -> {
                val deleteForEveryone = bundle.getBoolean("everyone")

                val failedMessages = dialog.messages.filter { it.id <= 0 }
                val messageIdsToDelete =
                    dialog.messages
                        .filter { it.id > 0 }
                        .map(VkMessage::id)

                deleteMessage(
                    messageIds = messageIdsToDelete,
                    deleteForAll = deleteForEveryone,
                    onSuccess = {
                        val newMessages = messages.value.toMutableList()
                        newMessages.removeAll(failedMessages)
                        messages.setValue { newMessages }
                        selectedMessages.setValue { emptyList() }
                        syncUiMessages()
                    }
                )
            }

            is MessageDialog.MessagePin -> {
                pinMessage(dialog.messageId)
            }

            is MessageDialog.MessageUnpin -> {
                unpinMessage(dialog.messageId)
            }

            is MessageDialog.MessageMarkImportance -> {
                markAsImportant(
                    messageIds = listOf(dialog.message.id),
                    important = dialog.isImportant
                )
            }

            is MessageDialog.MessageSpam -> {
                if (dialog.isSpam) {
                    deleteMessage(
                        messageIds = listOf(dialog.message.id),
                        spam = true
                    )
                } else {
                    // TODO: 29-Mar-25, Danil Nikolaev: report as not spam
                }
            }
        }
    }

    override fun onDialogDismissed(dialog: MessageDialog) {
        this.dialog.setValue { null }
    }

    override fun onDialogItemPicked(dialog: MessageDialog, bundle: Bundle) {
        when (dialog) {
            is MessageDialog.MessageOptions -> {
                val messageId = bundle.getLong("messageId")
                val cmId = bundle.getLong("cmId")

                when (val option = bundle.getParcelableCompat("option", MessageOption::class)) {
                    null -> Unit

                    MessageOption.Retry -> {
                        // TODO: 28-Mar-25, Danil Nikolaev: retry sending
                    }

                    MessageOption.Reply -> replyToMessage(cmId)

                    MessageOption.ForwardHere -> {

                    }

                    MessageOption.Forward -> {

                    }

                    MessageOption.Pin -> {
                        this.dialog.setValue {
                            MessageDialog.MessagePin(dialog.message.id)
                        }
                    }

                    MessageOption.Unpin -> {
                        this.dialog.setValue {
                            MessageDialog.MessageUnpin(dialog.message.id)
                        }
                    }

                    MessageOption.Read -> {
                        readMessage(dialog.message)
                    }

                    MessageOption.Copy -> {
                        copyMessage(dialog.message)
                    }

                    MessageOption.MarkAsImportant,
                    MessageOption.UnmarkAsImportant -> {
                        this.dialog.setValue {
                            MessageDialog.MessageMarkImportance(
                                message = dialog.message,
                                isImportant = option is MessageOption.MarkAsImportant
                            )
                        }
                    }

                    MessageOption.MarkAsSpam,
                    MessageOption.UnmarkAsSpam -> {
                        this.dialog.setValue {
                            MessageDialog.MessageSpam(
                                message = dialog.message,
                                isSpam = option is MessageOption.MarkAsSpam
                            )
                        }
                    }

                    MessageOption.Edit -> {}

                    MessageOption.Delete -> {
                        this.dialog.setValue {
                            MessageDialog.MessageDelete(dialog.message)
                        }
                    }
                }
            }

            is MessageDialog.MessageDelete -> Unit
            is MessageDialog.MessageUnpin -> Unit
            is MessageDialog.MessageMarkImportance -> Unit
            is MessageDialog.MessageSpam -> Unit
            is MessageDialog.MessagePin -> Unit
            is MessageDialog.MessagesDelete -> Unit
        }
    }

    override fun onScrolledToIndex() {
        isNeedToScrollToIndex.setValue { null }
    }

    override fun onCloseButtonClicked() {
        selectedMessages.setValue { emptyList() }
        syncUiMessages()
    }

    override fun onRefresh() {
        loadMessagesHistory(offset = 0)
    }

    override fun onAttachmentButtonClicked() {

    }

    override fun onMessageInputChanged(newText: TextFieldValue) {
        screenState.setValue { old ->
            old.copy(
                message = newText,
                actionMode = if (newText.text.isBlank()) ActionMode.RECORD_AUDIO
                else ActionMode.SEND
            )
        }
        updateStyles()
    }

    override fun onEmojiButtonLongClicked() {
        AppSettings.Features.fastText.takeIf { it.isNotBlank() }?.let { text ->
            val newText = "${screenState.value.message.text}$text"
            onMessageInputChanged(
                TextFieldValue(text = newText, selection = TextRange(newText.length))
            )
        }
    }

    override fun onActionButtonClicked() {
        when (screenState.value.actionMode) {
            ActionMode.DELETE -> {

            }

            ActionMode.EDIT -> {

            }

            ActionMode.RECORD_AUDIO -> {
                screenState.setValue { it.copy(actionMode = ActionMode.RECORD_VIDEO) }
            }

            ActionMode.RECORD_VIDEO -> {
                screenState.setValue { it.copy(actionMode = ActionMode.RECORD_AUDIO) }
            }

            ActionMode.SEND -> sendMessage()
        }
    }

    override fun onPaginationConditionsMet() {
        currentOffset.update { messages.value.size }
        loadMessagesHistory()
    }

    override fun onMessageClicked(messageId: Long) {
        val currentMessage = messages.value.firstOrNull { it.id == messageId } ?: return

        if (selectedMessages.value.isNotEmpty()) {
            val isSelected = selectedMessages.value.contains(currentMessage)

            selectedMessages.setValue { old ->
                old.toMutableList().also {
                    if (isSelected) {
                        it.remove(currentMessage)
                    } else {
                        it.add(currentMessage)
                    }
                }
            }
            syncUiMessages()
        } else {
            dialog.setValue {
                MessageDialog.MessageOptions(currentMessage)
            }
        }
    }

    override fun onMessageLongClicked(messageId: Long) {
        val currentMessage = messages.value.firstOrNull { it.id == messageId } ?: return

        val isSelected = selectedMessages.value.contains(currentMessage)
        if (isSelected) return

        selectedMessages.setValue { old ->
            old.toMutableList().also {
                it.add(currentMessage)
            }
        }
        syncUiMessages()
    }

    override fun onPinnedMessageClicked(messageId: Long) {
        val uiMessages = uiMessages.value
        val messageIndex = uiMessages.indexOfFirstOrNull {
            it is UiItem.Message && it.id == messageId
        }

        if (messageIndex == null) { // сообщения нет в списке
            // pizdets
        } else {
            isNeedToScrollToIndex.setValue { messageIndex }
        }
    }

    override fun onUnpinMessageClicked() {
        val pinnedMessageId = screenState.value.pinnedMessage?.id ?: return
        dialog.setValue {
            MessageDialog.MessageUnpin(pinnedMessageId)
        }
    }

    override fun onDeleteSelectedMessagesClicked() {
        dialog.setValue {
            MessageDialog.MessagesDelete(selectedMessages.value)
        }
    }

    private fun replyToMessage(cmId: Long) {
        val messageToReply = messages.value.find { it.cmId == cmId } ?: return

        inputFieldFocusRequester.setValue { true }
        replyToCmId = cmId
        screenState.setValue { old ->
            old.copy(
                replyTitle = messageToReply.extractTitle(),
                replyText = messageToReply.text
            )
        }
    }

    private var formatData = VkMessage.FormatData("1", emptyList())

    private fun updateStyles() {
        val annotations =
            mutableListOf<AnnotatedString.Range<out AnnotatedString.Annotation>>()

        formatData.items.forEachIndexed { index, item ->
            val spanStyle = when (item.type) {
                FormatDataType.BOLD -> {
                    SpanStyle(fontWeight = FontWeight.SemiBold)
                }

                FormatDataType.ITALIC -> {
                    SpanStyle(fontStyle = FontStyle.Italic)
                }

                FormatDataType.UNDERLINE -> {
                    SpanStyle(textDecoration = TextDecoration.Underline)
                }

                FormatDataType.URL -> null
            }

            spanStyle?.let {
                annotations += AnnotatedString.Range(
                    item = spanStyle,
                    start = item.offset,
                    end = item.offset + item.length
                )
            }
        }

        val newText = AnnotatedString(
            text = screenState.value.message.text,
            annotations = annotations
        )

        screenState.setValue { old ->
            old.copy(message = old.message.copy(annotatedString = newText))
        }
    }

    override fun onBoldClicked() {
        val selectionRange = screenState.value.message.selection
        val newItems = formatData.items.toMutableList()
        val wasRemoved = newItems.removeIfCompat {
            it.type == FormatDataType.BOLD &&
                    it.offset == selectionRange.start &&
                    it.offset + it.length == selectionRange.end
        }

        if (!wasRemoved) {
            newItems += VkMessage.FormatData.Item(
                offset = selectionRange.start,
                length = selectionRange.end - selectionRange.start,
                type = FormatDataType.BOLD,
                url = null
            )
        }

        formatData = formatData.copy(items = newItems)
        updateStyles()
    }

    override fun onItalicClicked() {
        val selectionRange = screenState.value.message.selection
        val newItems = formatData.items.toMutableList()
        val wasRemoved = newItems.removeIfCompat {
            it.type == FormatDataType.ITALIC &&
                    it.offset == selectionRange.start &&
                    it.offset + it.length == selectionRange.end
        }

        if (!wasRemoved) {
            newItems += VkMessage.FormatData.Item(
                offset = selectionRange.start,
                length = selectionRange.end - selectionRange.start,
                type = FormatDataType.ITALIC,
                url = null
            )
        }

        formatData = formatData.copy(items = newItems)
        updateStyles()
    }

    override fun onUnderlineClicked() {
        val selectionRange = screenState.value.message.selection
        val newItems = formatData.items.toMutableList()
        val wasRemoved = newItems.removeIfCompat {
            it.type == FormatDataType.UNDERLINE &&
                    it.offset == selectionRange.start &&
                    it.offset + it.length == selectionRange.end
        }

        if (!wasRemoved) {
            newItems += VkMessage.FormatData.Item(
                offset = selectionRange.start,
                length = selectionRange.end - selectionRange.start,
                type = FormatDataType.UNDERLINE,
                url = null
            )
        }

        formatData = formatData.copy(items = newItems)
        updateStyles()
    }

    override fun onLinkClicked() {

    }

    override fun onRegularClicked() {
        formatData = formatData.copy(items = emptyList())
        updateStyles()
    }

    override fun onReplyCloseClicked() {
        replyToCmId = null

        screenState.setValue { old ->
            old.copy(
                replyTitle = null,
                replyText = null
            )
        }
    }

    override fun onRequestReplyToMessage(cmId: Long) {
        replyToMessage(cmId)
    }

    override suspend fun loadMessageReadPeers(peerId: Long, cmId: Long): Int = suspendCoroutine {
        viewModelScope.launch {
            getMessageReadPeersUseCase
                .invoke(peerId = peerId, cmId = cmId)
                .listenValue(viewModelScope) { state ->
                    state.processState(
                        error = { error ->
                            it.resume(-1)
                        },
                        success = { count ->
                            it.resume(count)
                        }
                    )
                }
        }
    }

    private fun handleNewMessage(event: LongPollParsedEvent.NewMessage) {
        val message = event.message

        Log.d("MessagesHistoryViewModel", "handleNewMessage: $message")

        if (message.peerId != screenState.value.conversationId) return
        if (messages.value.indexOfFirstOrNull { it.id == message.id } != null) return

        val randomIds = messages.value.map(VkMessage::randomId)
        if (message.randomId != 0L && message.randomId in randomIds) return

        val newMessages = messages.value.toMutableList()
        newMessages.add(0, message)

        messages.setValue { newMessages }

        syncUiMessages()
    }

    private fun handleEditedMessage(event: LongPollParsedEvent.MessageEdited) {
        val message = event.message
        if (message.peerId != screenState.value.conversationId) return

        val newMessages = messages.value.toMutableList()
        val index = newMessages.indexOfFirstOrNull { it.id == message.id }
        if (index == null) { // сообщения нет в списке
            // pizdets
        } else {
            newMessages[index] = message
            messages.setValue { newMessages }
            syncUiMessages()
        }
    }

    private fun handleReadIncomingEvent(event: LongPollParsedEvent.IncomingMessageRead) {
        if (event.peerId != screenState.value.conversationId) return

        val messages = messages.value
        val index = messages.indexOfFirstOrNull { it.cmId == event.cmId }

        if (index == null) { // диалога нет в списке
            // pizdets
        } else {
            val newConversation = screenState.value.conversation.copy(
                inReadCmId = event.cmId
            )

            screenState.setValue { old ->
                old.copy(conversation = newConversation)
            }

            syncUiMessages()
        }
    }

    private fun handleReadOutgoingEvent(event: LongPollParsedEvent.OutgoingMessageRead) {
        if (event.peerId != screenState.value.conversationId) return

        val messages = messages.value
        val index = messages.indexOfFirstOrNull { it.cmId == event.cmId }

        if (index == null) { // сообщения нет в списке
            // pizdets
        } else {
            val newConversation = screenState.value.conversation.copy(
                outReadCmId = event.cmId
            )

            screenState.setValue { old ->
                old.copy(conversation = newConversation)
            }

            syncUiMessages()
        }
    }

    private fun handleMessageDeleted(event: LongPollParsedEvent.MessageDeleted) {
        if (event.peerId != screenState.value.conversationId) return

        val newMessages = messages.value.toMutableList()
        val index = newMessages.indexOfFirstOrNull { it.cmId == event.cmId }

        if (index == null) { // сообщения нет в списке
            // pizdets
        } else {
            newMessages.removeAt(index)
            messages.setValue { newMessages }
            syncUiMessages()
        }
    }

    private fun handleMessageRestored(event: LongPollParsedEvent.MessageRestored) {
        if (event.message.peerId != screenState.value.conversationId) return

        val newMessages = messages.value.toMutableList()
        val minDate = newMessages.minOf(VkMessage::date)

        if (event.message.date < minDate) { // сообщения не должно быть в списке
            // pizdets
            return
        }

        newMessages.add(event.message)
        messages.setValue { newMessages.sorted() }
        syncUiMessages()
    }

    private fun handleMessageMarkedAsImportant(event: LongPollParsedEvent.MessageMarkedAsImportant) {
        if (event.peerId != screenState.value.conversationId) return

        val newMessages = messages.value.toMutableList()
        val index = newMessages.indexOfFirstOrNull { it.cmId == event.cmId }

        if (index == null) { // сообщения нет в списке
            // pizdets
        } else {
            val newMessage = newMessages[index].copy(isImportant = event.marked)
            newMessages[index] = newMessage
            messages.setValue { newMessages }
            syncUiMessages()
        }
    }

    private fun handleMessageMarkedAsSpam(event: LongPollParsedEvent.MessageMarkedAsSpam) {
        if (event.peerId != screenState.value.conversationId) return

        val newMessages = messages.value.toMutableList()
        val index = newMessages.indexOfFirstOrNull { it.cmId == event.cmId }

        if (index == null) { // сообщения нет в списке
            // pizdets
        } else {
            newMessages.removeAt(index)
            messages.setValue { newMessages }
            syncUiMessages()
        }
    }

    private fun handleMessageMarkedAsNotSpam(event: LongPollParsedEvent.MessageMarkedAsNotSpam) {
        if (event.message.peerId != screenState.value.conversationId) return

        val newMessages = messages.value.toMutableList()
        val maxDate = newMessages.maxOf(VkMessage::date)
        val minDate = newMessages.minOf(VkMessage::date)

        if (event.message.date !in minDate..maxDate) return

        newMessages.add(event.message)
        messages.setValue { newMessages.sorted() }
        syncUiMessages()
    }

    private fun loadConversation() {
        Log.d("MessagesHistoryViewModelImpl", "loadConversation()")

        loadConversationsByIdUseCase(
            peerIds = listOf(screenState.value.conversationId),
            extended = true,
            fields = VkConstants.ALL_FIELDS
        ).listenValue(viewModelScope) { state ->
            state.processState(
                error = ::handleError,
                success = { response ->
                    val conversation = response.firstOrNull() ?: return@listenValue
                    val title = conversation.extractTitle(
                        useContactName = AppSettings.General.useContactNames,
                        resources = resourceProvider.resources
                    )
                    val avatar = conversation.extractAvatar()

                    screenState.setValue { old ->
                        old.copy(
                            conversation = conversation,
                            title = title,
                            avatar = avatar
                        )
                    }

                    conversation.pinnedMessage?.let(::handlePinnedMessage)
                }
            )
        }
    }

    private fun handlePinnedMessage(pinnedMessage: VkMessage?) {
        if (pinnedMessage == null) {
            screenState.setValue { old ->
                old.copy(
                    pinnedMessage = null,
                    conversation = old.conversation.copy(
                        pinnedMessage = null,
                        pinnedMessageId = null
                    ),
                    pinnedSummary = null,
                    pinnedTitle = null
                )
            }
            return
        }

        val pinnedUser = VkMemoryCache.getUser(pinnedMessage.fromId)
        val pinnedGroup = VkMemoryCache.getGroup(abs(pinnedMessage.fromId))
        val pinnedTitle = pinnedUser?.fullName ?: pinnedGroup?.name

        val pinnedSummary = buildAnnotatedString {
            pinnedMessage.text?.let(::append) ?: append("...")
        }

        screenState.setValue { old ->
            old.copy(
                pinnedMessage = pinnedMessage,
                conversation = old.conversation.copy(
                    pinnedMessage = pinnedMessage,
                    pinnedMessageId = pinnedMessage.id
                ),
                pinnedSummary = pinnedSummary,
                pinnedTitle = pinnedTitle.orDots()
            )
        }
    }

    private fun loadMessagesHistory(offset: Int = currentOffset.value) {
        Log.d("MessagesHistoryViewModel", "loadMessagesHistory: $offset")

        messagesUseCase.getMessagesHistory(
            conversationId = screenState.value.conversationId,
            count = MESSAGES_LOAD_COUNT,
            offset = offset,
        ).listenValue(viewModelScope) { state ->
            state.processState(
                error = ::handleError,
                success = { response ->
                    val messages = response.messages
                    val fullMessages = if (offset == 0) {
                        messages
                    } else {
                        this.messages.value.plus(messages)
                    }.sorted()

                    val conversations = response.conversations

                    imagesToPreload.setValue {
                        messages.mapNotNull { it.extractAvatar().extractUrl() }
                    }

                    messagesUseCase.storeMessages(messages)
                    conversationsUseCase.storeConversations(conversations)

                    val itemsCountSufficient = messages.size == MESSAGES_LOAD_COUNT

                    val paginationExhausted = !itemsCountSufficient &&
                            this.messages.value.isNotEmpty()
                    screenState.setValue { old ->
                        old.copy(isPaginationExhausted = paginationExhausted)
                    }

                    this.messages.emit(fullMessages)
                    syncUiMessages()
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

    private fun List<VkMessage>.sorted(): List<VkMessage> {
        return sortedWith { m1, m2 ->
            val dateDiff = m2.date - m1.date
            if (dateDiff != 0) {
                dateDiff
            } else {
                val idDiff = m2.id - m1.id
                idDiff.toInt()
            }
        }
    }

    private fun sendMessage() {
        lastMessageText = screenState.value.message.text

        val newMessage = VkMessage(
            id = -1L - sendingMessages.size,
            cmId = -1L - sendingMessages.size,
            text = lastMessageText,
            isOut = true,
            peerId = screenState.value.conversationId,
            fromId = UserConfig.userId,
            date = (System.currentTimeMillis() / 1000).toInt(),
            randomId = Random.nextInt().toLong(),
            action = null,
            actionMemberId = null,
            actionText = null,
            actionConversationMessageId = null,
            actionMessage = null,
            updateTime = null,
            isImportant = false,
            forwards = null,
            attachments = null,
            replyMessage = when {
                replyToCmId != null -> messages.value.find { it.cmId == replyToCmId }
                else -> null
            },
            geoType = null,
            user = VkMemoryCache.getUser(UserConfig.userId),
            group = null,
            actionUser = null,
            actionGroup = null,
            isPinned = false,
            isSpam = false,
            pinnedAt = null,
            formatData = formatData,
        )
        formatData = formatData.copy(items = emptyList())
        sendingMessages += newMessage
        messages.setValue { old -> listOf(newMessage).plus(old) }
        syncUiMessages()

        screenState.setValue { old ->
            old.copy(
                message = TextFieldValue(),
                actionMode = ActionMode.RECORD_AUDIO,
                replyTitle = null,
                replyText = null
            )
        }

        val replyCmId = replyToCmId
        replyToCmId = null

        val forward = when {
            replyCmId != null -> {
                buildJsonObject {
                    put("peer_id", screenState.value.conversationId)
                    put("conversation_message_ids", buildJsonArray { add(replyCmId) })
                    put("is_reply", true)
                }.toString()
            }

            else -> null
        }

        messagesUseCase.sendMessage(
            peerId = screenState.value.conversationId,
            randomId = newMessage.randomId,
            message = newMessage.text,
            forward = forward,
            attachments = null,
            formatData = newMessage.formatData
        ).listenValue(viewModelScope) { state ->
            state.processState(
                any = { sendingMessages.remove(newMessage) },
                error = { error ->
                    val failedId = -500_000L - failedMessages.size
                    val newFailedMessage = newMessage.copy(id = failedId)
                    failedMessages += newFailedMessage

                    val newMessages = messages.value.toMutableList()
                    newMessages[newMessages.indexOf(newMessage)] = newFailedMessage
                    messages.setValue { newMessages }
                    syncUiMessages()
                },
                success = { response ->
                    val newMessages = messages.value.toMutableList()
                    newMessages[newMessages.indexOf(newMessage)] = newMessage.copy(
                        id = response.messageId,
                        cmId = response.cmId
                    )
                    messages.setValue { newMessages }
                    syncUiMessages()
                }
            )
        }
    }

    private fun markAsImportant(
        messageIds: List<Long>,
        important: Boolean,
    ) {
        messagesUseCase.markAsImportant(
            peerId = screenState.value.conversationId,
            messageIds = messageIds,
            important = important
        ).listenValue(viewModelScope) { state ->
            state.processState(
                error = ::handleError,
                success = {
                    val newMessages = messages.value
                        .toMutableList()
                        .map { message ->
                            if (message.id in messageIds) {
                                message.copy(isImportant = important)
                            } else {
                                message
                            }
                        }
                    messages.setValue { newMessages }
                    syncUiMessages()
                }
            )
        }
    }

    private fun deleteMessage(
        messageIds: List<Long>,
        spam: Boolean = false,
        deleteForAll: Boolean = false,
        onSuccess: () -> Unit = {}
    ) {
        messagesUseCase.delete(
            peerId = screenState.value.conversationId,
            messageIds = messageIds,
            spam = spam,
            deleteForAll = deleteForAll
        ).listenValue(viewModelScope) { state ->
            state.processState(
                error = ::handleError,
                success = {
                    onSuccess()
                    val newMessages = messages.value.toMutableList()
                    val messagesToDelete = newMessages.filter { it.id in messageIds }
                    newMessages.removeAll(messagesToDelete)
                    messages.setValue { newMessages }
                    syncUiMessages()
                }
            )
        }
    }

    private fun pinMessage(messageId: Long) {
        messagesUseCase.pin(
            peerId = screenState.value.conversationId,
            messageId = messageId,
            cmId = null
        ).listenValue(viewModelScope) { state ->
            state.processState(
                error = ::handleError,
                success = { pinnedMessage ->
                    handlePinnedMessage(pinnedMessage)

                    val newMessages = messages.value.toMutableList()
                    val index = newMessages.indexOfFirstOrNull { it.id == messageId }

                    if (index == null) {// сообщения нет в списке
                        // pizdets
                    } else {
                        newMessages[index] = pinnedMessage
                        messages.setValue { newMessages }
                        syncUiMessages()
                    }
                }
            )
        }
    }

    private fun unpinMessage(messageId: Long) {
        messagesUseCase.unpin(screenState.value.conversationId)
            .listenValue(viewModelScope) { state ->
                state.processState(
                    error = ::handleError,
                    success = {
                        val newMessages = messages.value.toMutableList()
                        val index = newMessages.indexOfFirstOrNull { it.id == messageId }

                        if (index == null) { // сообщения нет в списке
                            // pizdets
                        } else {
                            newMessages[index] = newMessages[index].copy(isPinned = false)
                            messages.setValue { newMessages }
                            syncUiMessages()
                        }

                        handlePinnedMessage(null)
                    }
                )
            }
    }

    fun editMessage(
        originalMessage: VkMessage,
        peerid: Long,
        messageid: Long,
        newText: String? = null,
        attachments: List<VkAttachment>? = null,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
//            sendRequest {
//                messagesRepository.edit(
//                    MessagesEditRequest(
//                        peerId = peerId,
//                        messageId = messageId,
//                        message = newText,
//                        attachments = attachments
//                    )
//                )
//            } ?: return@launch

            // TODO: 25.08.2023, Danil Nikolaev: update message
        }
    }

    private fun readMessage(message: VkMessage) {
        messagesUseCase.markAsRead(
            peerId = screenState.value.conversationId,
            startMessageId = message.id
        ).listenValue(viewModelScope) { state ->
            state.processState(
                error = ::handleError,
                success = {
                    val oldConversation = screenState.value.conversation
                    val newConversation = oldConversation.copy(
                        inRead =
                            if (!message.isOut) message.id
                            else oldConversation.inRead,
                        outRead =
                            if (message.isOut) message.id
                            else oldConversation.outRead
                    )

                    screenState.setValue { old ->
                        old.copy(conversation = newConversation)
                    }

                    syncUiMessages()
                }
            )
        }
    }

    private fun copyMessage(message: VkMessage) {
        val clipboardManager =
            applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val messageToCopy = message.text.orEmpty().trim()
        if (messageToCopy.isEmpty()) {
            val photo = with(message.attachments.orEmpty()) {
                if (size == 1 && all { it is VkPhotoDomain }) {
                    first() as? VkPhotoDomain
                } else null
            } ?: return

            val photoMaxSize = photo.getMaxSize() ?: return

            viewModelScope.launch(Dispatchers.IO) {
                val drawable = applicationContext.imageLoader.execute(
                    ImageRequest.Builder(applicationContext)
                        .data(photoMaxSize.url)
                        .build()
                ).drawable ?: return@launch

                val imagesDir = File(applicationContext.cacheDir, "images")
                if (!imagesDir.exists()) imagesDir.mkdirs()
                val imageFile = File(imagesDir, "shared_image_id${photo.id}.png")
                FileOutputStream(imageFile).use {
                    drawable.toBitmapOrNull()?.compress(Bitmap.CompressFormat.PNG, 100, it)
                }

                val uri = FileProvider.getUriForFile(
                    applicationContext,
                    "${applicationContext.packageName}.provider",
                    imageFile
                )

                val clip = ClipData.newUri(applicationContext.contentResolver, "Image", uri)
                clipboardManager.setPrimaryClip(clip)

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        applicationContext,
                        "Image copied to clipboard",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            return
        }

        clipboardManager.setPrimaryClip(ClipData.newPlainText("Message", messageToCopy))

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S) {
            Toast.makeText(applicationContext, R.string.copied_to_clipboard, Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun syncUiMessages(): List<UiItem> {
        val messages = messages.value
        val selectedMessages = selectedMessages.value

        val newUiMessages = messages.mapIndexed { index, message ->
            message.asPresentation(
                resourceProvider = resourceProvider,
                showName = false,
                prevMessage = messages.getOrNull(index + 1),
                nextMessage = messages.getOrNull(index - 1),
                showTimeInActionMessages = AppSettings.Experimental.showTimeInActionMessages,
                conversation = screenState.value.conversation,
                isSelected = selectedMessages.indexOfFirstOrNull { it.id == message.id } != null
            )
        }
        uiMessages.setValue { newUiMessages }

        return newUiMessages
    }

    companion object {
        const val MESSAGES_LOAD_COUNT = 30
    }
}


// TODO: 25.08.2023, Danil Nikolaev: this and down below - rewrite

//    suspend fun uploadPhoto(
//        peerid: Long,
//        photo: File,
//        name: String,
//    ) {
//        suspendCoroutine {
//            viewModelScope.launch {
//                val uploadServerUrl = getPhotoMessageUploadServer(peerId)
//                val uploadedFileInfo = uploadPhotoToServer(uploadServerUrl, photo, name)
//
//                val savedAttachment = saveMessagePhoto(
//                    uploadedFileInfo.first,
//                    uploadedFileInfo.second,
//                    uploadedFileInfo.third
//                )
//
//                it.resume(savedAttachment)
//            }
//        }
//    }

//    private suspend fun getPhotoMessageUploadServer(peerid: Long) {
//        suspendCoroutine { continuation ->
//            viewModelScope.launch {
//                sendRequestNotNull(
//                    onError = { exception ->
//                        continuation.resumeWithException(exception)
//                        true
//                    },
//                    request = { photosRepository.getMessagesUploadServer(peerId) }
//                ).response?.let { response ->
//                    continuation.resume(response.uploadUrl)
//                }
//            }
//        }
//    }

//    private suspend fun uploadPhotoToServer(
//        uploadUrl: String,
//        photo: File,
//        name: String,
//    ) {
//        suspendCoroutine { continuation ->
//            viewModelScope.launch {
//                val requestBody = photo.asRequestBody("image/*".toMediaType())
//                val body = MultipartBody.Part.createFormData("photo", name, requestBody)

//            sendRequestNotNull(
//                onError = { exception ->
//                    continuation.resumeWithException(exception)
//                    true
//                },
//                request = { photosRepository.uploadPhoto(uploadUrl, body) }
//            ).let { response ->
//                continuation.resume(Triple(response.server, response.photo, response.hash))
//            }
//            }
//        }
//    }

//    private suspend fun saveMessagePhoto(
//        server: Int,
//        photo: String,
//        hash: String,
//    ) = suspendCoroutine<VkAttachment> { continuation ->
//        viewModelScope.launch {
//            sendRequestNotNull(
//                onError = { exception ->
//                    continuation.resumeWithException(exception)
//                    true
//                },
//                request = {
//                    photosRepository.saveMessagePhoto(
//                        PhotosSaveMessagePhotoRequest(photo, server, hash)
//                    )
//                }
//            ).response?.first()?.toDomain()?.let(continuation::resume)
//        }
//    }

//    suspend fun uploadVideo(
//        file: File,
//        name: String,
//    ) {
//        suspendCoroutine {
//            viewModelScope.launch {
//                val uploadInfo = getVideoMessageUploadServer()
//
//                uploadVideoToServer(
//                    uploadInfo.first,
//                    file,
//                    name
//                )
//
//                it.resume(uploadInfo.second)
//            }
//        }
//    }

//    private suspend fun getVideoMessageUploadServer() {
//        suspendCoroutine { continuation ->
//            viewModelScope.launch {
//            sendRequestNotNull(
//                onError = { exception ->
//                    continuation.resumeWithException(exception)
//                    true
//                },
//                request = { videosRepository.save() }
//            ).response?.let { response ->
//                val uploadUrl = response.uploadUrl
//                val video = VkVideoDomain(
//                    id = response.videoId,
//                    ownerId = response.ownerId,
//                    images = emptyList(),
//                    firstFrames = null,
//                    accessKey = response.accessKey,
//                    title = response.title
//                )
//
//                continuation.resume(uploadUrl to video)
//            }
//            }
//        }
//    }

//    private suspend fun uploadVideoToServer(
//        uploadUrl: String,
//        file: File,
//        name: String,
//    ) {
//        viewModelScope.launch {
//            val requestBody = file.asRequestBody()
//            val body = MultipartBody.Part.createFormData("video_file", name, requestBody)
//
//            sendRequest(
//                onError = { exception -> throw exception },
//                request = { videosRepository.upload(uploadUrl, body) }
//            )
//        }
//    }

//    suspend fun uploadAudio(
//        file: File,
//        name: String,
//    ) {
//        suspendCoroutine {
//            viewModelScope.launch {
//                val uploadUrl = getAudioUploadServer()
//                val uploadInfo = uploadAudioToServer(uploadUrl, file, name)
//                val saveInfo = saveMessageAudio(
//                    uploadInfo.first, uploadInfo.second, uploadInfo.third
//                )
//
//                it.resume(saveInfo)
//            }
//        }
//    }

//    private suspend fun getAudioUploadServer() {
//        suspendCoroutine { continuation ->
//            viewModelScope.launch {
//                sendRequestNotNull(
//                    onError = { exception ->
//                        continuation.resumeWithException(exception)
//                        true
//                    },
//                    request = { audiosRepository.getUploadServer() }
//                ).response?.uploadUrl?.let(continuation::resume)
//            }
//        }
//    }

//    private suspend fun uploadAudioToServer(
//        uploadUrl: String,
//        file: File,
//        name: String,
//    ) {
//        suspendCoroutine { continuation ->
//            viewModelScope.launch {
//                val requestBody = file.asRequestBody()
//                val body = MultipartBody.Part.createFormData("file", name, requestBody)
//
//                sendRequestNotNull(
//                    onError = { exception ->
//                        continuation.resumeWithException(exception)
//                        true
//                    },
//                    request = { audiosRepository.upload(uploadUrl, body) }
//                ).let { response ->
//                    response.error?.let { error -> throw ApiException(error = error) }
//
//                    continuation.resume(
//                        Triple(response.server, response.audio.notNull(), response.hash)
//                    )
//                }
//            }
//        }
//    }

//    private suspend fun saveMessageAudio(
//        server: Int,
//        audio: String,
//        hash: String,
//    ) {
//        suspendCoroutine<VkAttachment> { continuation ->
//            viewModelScope.launch {
//                sendRequestNotNull(
//                    onError = { exception ->
//                        continuation.resumeWithException(exception)
//                        true
//                    },
//                    request = { audiosRepository.save(server, audio, hash) }
//                ).response?.toDomain()?.let(continuation::resume)
//            }
//        }
//    }

//    suspend fun uploadFile(
//        peerid: Long,
//        file: File,
//        name: String,
//        type: FilesRepository.FileType,
//    ) {
//        suspendCoroutine { continuation ->
//            viewModelScope.launch {
//                val uploadServerUrl = getFileMessageUploadServer(peerId, type)
//                val uploadedFileInfo = uploadFileToServer(uploadServerUrl, file, name)
//                val savedAttachmentPair = saveMessageFile(uploadedFileInfo)
//
//                continuation.resume(savedAttachmentPair.second)
//            }
//        }
//    }

//    private suspend fun getFileMessageUploadServer(
//        peerid: Long,
//        type: FilesRepository.FileType,
//    ) {
//        suspendCoroutine { continuation ->
//            viewModelScope.launch {
//                val uploadServerResponse = sendRequestNotNull(
//                    onError = { exception ->
//                        continuation.resumeWithException(exception)
//                        true
//                    },
//                    request = { filesRepository.getMessagesUploadServer(peerId, type) }
//                ).response.notNull()
//
//                continuation.resume(uploadServerResponse.uploadUrl)
//            }
//        }
//    }

//    private suspend fun uploadFileToServer(
//        uploadUrl: String,
//        file: File,
//        name: String,
//    ) {
//        suspendCoroutine { continuation ->
//            viewModelScope.launch {
//                val requestBody = file.asRequestBody()
//                val body = MultipartBody.Part.createFormData("file", name, requestBody)
//
//                sendRequestNotNull(
//                    onError = { exception ->
//                        continuation.resumeWithException(exception)
//                        true
//                    },
//                    request = { filesRepository.uploadFile(uploadUrl, body) }
//                ).let { response ->
//                    response.error?.let { error -> throw ApiException(error = error) }
//
//                    continuation.resume(response.file.notNull())
//                }
//            }
//        }
//    }

//    private suspend fun saveMessageFile(file: String) {
//        suspendCoroutine { continuation ->
//            viewModelScope.launch {
//                sendRequestNotNull(
//                    onError = { exception ->
//                        continuation.resumeWithException(exception)
//                        true
//                    },
//                    request = { filesRepository.saveMessageFile(file) }
//                ).response?.let { response ->
//                    val type = response.type
//                    val attachmentFile =
//                        response.file?.toDomain() ?: response.voiceMessage?.toDomain()
//
//                    continuation.resume(type to attachmentFile.notNull())
//                }
//            }
//        }
//    }
//}

//data class MessagesLoadedEvent(
//    val count: Int,
//    val conversations: HashMap<Int, VkConversationDomain>,
//    val messages: List<VkMessageDomain>,
//    val profiles: HashMap<Int, VkUserDomain>,
//    val groups: HashMap<Int, VkGroupDomain>,
//) : VkEvent()
//
//data class MessagesMarkAsImportantEvent(val messagesIds: List<Int>, val important: Boolean) :
//    VkEvent()
//
//data class MessagesPinEvent(val message: VkMessageDomain) : VkEvent()
//
//object MessagesUnpinEvent : VkEvent()
//
//data class MessagesDeleteEvent(val peerid: Long, val messagesIds: List<Int>) : VkEvent()
//
//data class MessagesEditEvent(val message: VkMessageDomain) : VkEvent()
//
//data class MessagesReadEvent(
//    val isOut: Boolean,
//    val peerid: Long,
//    val messageid: Long,
//) : VkEvent()
//
//data class MessagesNewEvent(
//    val message: VkMessageDomain,
//    val profiles: HashMap<Int, VkUserDomain>,
//    val groups: HashMap<Int, VkGroupDomain>,
//) : VkEvent()
