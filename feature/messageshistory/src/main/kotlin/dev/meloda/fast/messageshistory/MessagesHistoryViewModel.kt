package dev.meloda.fast.messageshistory

import android.util.Log
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.conena.nanokt.collections.indexOfFirstOrNull
import com.conena.nanokt.collections.indexOfOrNull
import com.conena.nanokt.text.isEmptyOrBlank
import com.conena.nanokt.text.isNotEmptyOrBlank
import dev.meloda.fast.common.extensions.listenValue
import dev.meloda.fast.common.extensions.orDots
import dev.meloda.fast.common.extensions.setValue
import dev.meloda.fast.common.provider.ResourceProvider
import dev.meloda.fast.data.State
import dev.meloda.fast.data.UserConfig
import dev.meloda.fast.data.VkMemoryCache
import dev.meloda.fast.data.processState
import dev.meloda.fast.datastore.AppSettings
import dev.meloda.fast.datastore.UserSettings
import dev.meloda.fast.domain.ConversationsUseCase
import dev.meloda.fast.domain.LoadConversationsByIdUseCase
import dev.meloda.fast.domain.LongPollUpdatesParser
import dev.meloda.fast.domain.MessagesUseCase
import dev.meloda.fast.messageshistory.model.ActionMode
import dev.meloda.fast.messageshistory.model.MessagesHistoryScreenState
import dev.meloda.fast.messageshistory.model.SendingStatus
import dev.meloda.fast.messageshistory.model.UiItem
import dev.meloda.fast.messageshistory.navigation.MessagesHistory
import dev.meloda.fast.messageshistory.util.asPresentation
import dev.meloda.fast.messageshistory.util.extractAvatar
import dev.meloda.fast.messageshistory.util.extractTitle
import dev.meloda.fast.messageshistory.util.findMessageById
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.model.LongPollParsedEvent
import dev.meloda.fast.model.api.domain.VkAttachment
import dev.meloda.fast.model.api.domain.VkMessage
import dev.meloda.fast.network.VkErrorCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.random.Random

interface MessagesHistoryViewModel {

    val screenState: StateFlow<MessagesHistoryScreenState>
    val selectedMessages: StateFlow<List<Int>>

    val isNeedToScrollToIndex: StateFlow<Int?>

    val baseError: StateFlow<BaseError?>
    val imagesToPreload: StateFlow<List<String>>

    val showMessageOptions: StateFlow<VkMessage?>

    val currentOffset: StateFlow<Int>
    val canPaginate: StateFlow<Boolean>

    fun onScrolledToIndex()

    fun onCloseButtonClicked()
    fun onRefresh()
    fun onAttachmentButtonClicked()
    fun onMessageInputChanged(newText: TextFieldValue)
    fun onEmojiButtonLongClicked()
    fun onActionButtonClicked()

    fun onPaginationConditionsMet()

    fun onMessageClicked(messageId: Int)
    fun onMessageLongClicked(messageId: Int)
    fun onMessageOptionsDialogDismissed()
    fun onPinnedMessageClicked(messageId: Int)
    fun onUnpinMessageClicked()
}

class MessagesHistoryViewModelImpl(
    private val messagesUseCase: MessagesUseCase,
    private val conversationsUseCase: ConversationsUseCase,
    private val resourceProvider: ResourceProvider,
    private val userSettings: UserSettings,
    private val loadConversationsByIdUseCase: LoadConversationsByIdUseCase,
    updatesParser: LongPollUpdatesParser,
    savedStateHandle: SavedStateHandle
) : MessagesHistoryViewModel, ViewModel() {

    override val screenState = MutableStateFlow(MessagesHistoryScreenState.EMPTY)
    override val selectedMessages = MutableStateFlow<List<Int>>(emptyList())

    override val isNeedToScrollToIndex = MutableStateFlow<Int?>(null)

    override val baseError = MutableStateFlow<BaseError?>(null)
    override val imagesToPreload = MutableStateFlow<List<String>>(emptyList())

    override val showMessageOptions = MutableStateFlow<VkMessage?>(null)

    override val currentOffset = MutableStateFlow(0)

    override val canPaginate = MutableStateFlow(false)

    private val messages = MutableStateFlow<List<VkMessage>>(emptyList())

    private var lastMessageText: String? = null

    private val sendingMessages: MutableList<VkMessage> = mutableListOf()

    init {
        val arguments = MessagesHistory.from(savedStateHandle).arguments

        screenState.setValue { old -> old.copy(conversationId = arguments.conversationId) }

        loadConversation()
        loadMessagesHistory()

        updatesParser.onNewMessage(::handleNewMessage)
        updatesParser.onMessageEdited(::handleEditedMessage)
        updatesParser.onMessageIncomingRead(::handleReadIncomingEvent)
        updatesParser.onMessageOutgoingRead(::handleReadOutgoingEvent)

        userSettings.showTimeInActionMessages.listenValue(
            viewModelScope,
            ::toggleShowTimeInActionMessages
        )
    }

    override fun onScrolledToIndex() {
        isNeedToScrollToIndex.setValue { null }
    }

    override fun onCloseButtonClicked() {
        screenState.setValue { old ->
            old.copy(
                messages = old.messages.map {
                    if (it is UiItem.Message) it.copy(isSelected = false)
                    else it
                }
            )
        }
        selectedMessages.setValue { emptyList() }
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
                actionMode = if (newText.text.isEmptyOrBlank()) ActionMode.Record
                else ActionMode.Send
            )
        }

        screenState.setValue { old -> old.copy(message = newText) }
    }

    override fun onEmojiButtonLongClicked() {
        AppSettings.Features.fastText.takeIf { it.isNotEmptyOrBlank() }?.let { text ->
            screenState.setValue { old ->
                val newText = "${old.message.text}$text"
                old.copy(
                    message = TextFieldValue(text = newText, selection = TextRange(newText.length))
                )
            }
        }
    }

    override fun onActionButtonClicked() {
        when (screenState.value.actionMode) {
            ActionMode.Delete -> {

            }

            ActionMode.Edit -> {

            }

            ActionMode.Record -> {

            }

            ActionMode.Send -> sendMessage()
        }
    }

    override fun onPaginationConditionsMet() {
        currentOffset.update { screenState.value.messages.size }
        loadMessagesHistory()
    }

    override fun onMessageClicked(messageId: Int) {
        val messageIndex = screenState.value.messages.indexOfFirstOrNull {
            it is UiItem.Message && it.id == messageId
        } ?: return

        val newMessages = screenState.value.messages.toMutableList()
        val currentMessage: UiItem.Message = newMessages[messageIndex] as UiItem.Message

        if (selectedMessages.value.isNotEmpty()) {
            val isSelected = selectedMessages.value.contains(currentMessage.id)

            newMessages[messageIndex] = currentMessage.copy(
                isSelected = !isSelected
            )
            screenState.setValue { old -> old.copy(messages = newMessages) }
            selectedMessages.setValue { old ->
                old.toMutableList().also {
                    if (isSelected) {
                        it.remove(currentMessage.id)
                    } else {
                        it.add(currentMessage.id)
                    }
                }
            }
        } else {
            messages.value.firstOrNull { it.id == currentMessage.id }?.let { message ->
                showMessageOptions.setValue { message }
            }
        }
    }

    override fun onMessageLongClicked(messageId: Int) {
        val messageIndex = screenState.value.messages.indexOfFirstOrNull {
            it is UiItem.Message && it.id == messageId
        } ?: return

        val newMessages = screenState.value.messages.toMutableList()
        val currentMessage: UiItem.Message = newMessages[messageIndex] as UiItem.Message

        val isSelected = selectedMessages.value.contains(currentMessage.id)

        newMessages[messageIndex] = currentMessage.copy(
            isSelected = !isSelected
        )
        screenState.setValue { old -> old.copy(messages = newMessages) }
        selectedMessages.setValue { old ->
            old.toMutableList().also {
                if (isSelected) {
                    it.remove(currentMessage.id)
                } else {
                    it.add(currentMessage.id)
                }
            }
        }
    }

    override fun onMessageOptionsDialogDismissed() {
        showMessageOptions.setValue { null }
    }

    override fun onPinnedMessageClicked(messageId: Int) {
        val messageIndex = screenState.value.messages.indexOfFirstOrNull {
            it is UiItem.Message && it.id == messageId
        }

        if (messageIndex == null) { // сообщения нет в списке
            // pizdets
        } else {
            isNeedToScrollToIndex.setValue { messageIndex }
        }
    }

    override fun onUnpinMessageClicked() {
        // TODO: 27.03.2025, Danil Nikolaev: confirmation alert
        val pinnedMessageId = screenState.value.pinnedMessage?.id ?: return
        unpinMessage(pinnedMessageId)
    }

    private fun unpinMessage(messageId: Int) {
        val messageIndex = screenState.value.messages.indexOfFirstOrNull {
            it is UiItem.Message && it.id == messageId
        }

        messagesUseCase.unpin(screenState.value.conversationId)
            .listenValue(viewModelScope) { state ->
                state.processState(
                    error = ::handleError,
                    success = {
                        var newState = screenState.value.copy(
                            pinnedMessage = null,
                            conversation = screenState.value.conversation.copy(
                                pinnedMessage = null,
                                pinnedMessageId = null
                            ),
                            pinnedSummary = null,
                            pinnedTitle = null
                        )

                        if (messageIndex != null) {
                            val newMessages = screenState.value.messages.toMutableList()
                            val currentMessage: UiItem.Message =
                                newMessages[messageIndex] as UiItem.Message
                            newMessages[messageIndex] = currentMessage.copy(isPinned = false)
                            newState = newState.copy(messages = newMessages)
                        }

                        screenState.setValue { old -> newState }
                    }
                )
            }
    }

    private fun handleNewMessage(event: LongPollParsedEvent.NewMessage) {
        val message = event.message

        Log.d("MessagesHistoryViewModel", "handleNewMessage: $message")

        if (message.peerId != screenState.value.conversationId) return
        if (screenState.value.messages.findMessageById(message.id) != null) return

        val randomIds = messages.value.map(VkMessage::randomId)
        if (message.randomId != 0 && message.randomId in randomIds) return

        val newMessages = screenState.value.messages.toMutableList()
        val prevMessage = messages.value.firstOrNull()

        messages.setValue { old ->
            old.toMutableList().also { it.add(0, message) }
        }

        val newMessage = message.asPresentation(
            resourceProvider = resourceProvider,
            showName = false,
            prevMessage = prevMessage,
            nextMessage = null,
            showTimeInActionMessages = userSettings.showTimeInActionMessages.value,
            conversation = screenState.value.conversation,
        )
        newMessages.add(0, newMessage)

        prevMessage?.let { prev ->
            newMessages[1] = prev.asPresentation(
                resourceProvider = resourceProvider,
                showName = false,
                prevMessage = prevMessage,
                nextMessage = messages.value.first(),
                showTimeInActionMessages = userSettings.showTimeInActionMessages.value,
                conversation = screenState.value.conversation
            )
        }

        screenState.setValue { old -> old.copy(messages = newMessages) }
    }

    private fun handleEditedMessage(event: LongPollParsedEvent.MessageEdited) {
        val message = event.message
        if (message.peerId != screenState.value.conversationId) return

        screenState.value.messages
            .indexOfFirstOrNull { it.id == message.id }
            ?.let { index ->
                val newMessage = message.asPresentation(
                    resourceProvider = resourceProvider,
                    showName = false,
                    prevMessage = messages.value.getOrNull(index + 1),
                    nextMessage = messages.value.getOrNull(index - 1),
                    showTimeInActionMessages = userSettings.showTimeInActionMessages.value,
                    conversation = screenState.value.conversation
                )

                val newMessages = screenState.value.messages.toMutableList()
                newMessages[index] = newMessage

                screenState.setValue { old -> old.copy(messages = newMessages) }
            }
    }

    private fun handleReadIncomingEvent(event: LongPollParsedEvent.IncomingMessageRead) {
        if (event.peerId != screenState.value.conversationId) return

        val messages = messages.value
        val messageIndex =
            messages.indexOfFirstOrNull { it.id == event.messageId }

        if (messageIndex == null) { // диалога нет в списке
            // pizdets
        } else {
            val newConversation = screenState.value.conversation.copy(
                inRead = event.messageId
            )

            val uiMessages = messages.mapIndexed { index, item ->
                item.asPresentation(
                    resourceProvider = resourceProvider,
                    showName = false,
                    prevMessage = messages.getOrNull(index + 1),
                    nextMessage = messages.getOrNull(index - 1),
                    showTimeInActionMessages = userSettings.showTimeInActionMessages.value,
                    conversation = newConversation
                )
            }

            screenState.setValue { old ->
                old.copy(
                    conversation = newConversation,
                    messages = uiMessages,
                )
            }
        }
    }

    private fun handleReadOutgoingEvent(event: LongPollParsedEvent.OutgoingMessageRead) {
        if (event.peerId != screenState.value.conversationId) return

        val messages = messages.value
        val messageIndex =
            messages.indexOfFirstOrNull { it.id == event.messageId }

        if (messageIndex == null) { // диалога нет в списке
            // pizdets
        } else {
            val newConversation = screenState.value.conversation.copy(
                outRead = event.messageId
            )

            val uiMessages = messages.mapIndexed { index, item ->
                item.asPresentation(
                    resourceProvider = resourceProvider,
                    showName = false,
                    prevMessage = messages.getOrNull(index + 1),
                    nextMessage = messages.getOrNull(index - 1),
                    showTimeInActionMessages = userSettings.showTimeInActionMessages.value,
                    conversation = newConversation
                )
            }

            screenState.setValue { old ->
                old.copy(
                    conversation = newConversation,
                    messages = uiMessages,
                )
            }
        }
    }

    private fun loadConversation() {
        Log.d("MessagesHistoryViewModelImpl", "loadConversation()")

        loadConversationsByIdUseCase(listOf(screenState.value.conversationId))
            .listenValue(viewModelScope) { state ->
                state.processState(
                    error = ::handleError,
                    success = { response ->
                        val conversation = response.firstOrNull() ?: return@listenValue
                        val title = conversation.extractTitle(
                            useContactName = AppSettings.General.useContactNames,
                            resources = resourceProvider.resources
                        )
                        val avatar = conversation.extractAvatar()
                        val pinnedMessage = conversation.pinnedMessage
                        val pinnedUser = if (pinnedMessage == null) null else
                            VkMemoryCache.getUser(pinnedMessage.fromId)
                        val pinnedGroup = if (pinnedMessage == null) null else
                            VkMemoryCache.getGroup(abs(pinnedMessage.fromId))
                        val pinnedTitle = pinnedUser?.fullName ?: pinnedGroup?.name

                        val pinnedSummary = buildAnnotatedString {
                            pinnedMessage?.text?.let(::append) ?: append("...")
                        }

                        screenState.setValue { old ->
                            old.copy(
                                conversation = conversation,
                                title = title,
                                avatar = avatar,
                                pinnedMessage = pinnedMessage,
                                pinnedTitle = pinnedTitle.orDots(),
                                pinnedSummary = pinnedSummary
                            )
                        }
                    }
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
                            screenState.value.messages.isNotEmpty()
                    val newState = screenState.value.copy(
                        isPaginationExhausted = paginationExhausted,
                    )

                    val loadedMessages = fullMessages.mapIndexed { index, message ->
                        message.asPresentation(
                            resourceProvider = resourceProvider,
                            showName = false,
                            prevMessage = messages.getOrNull(index + 1),
                            nextMessage = messages.getOrNull(index - 1),
                            showTimeInActionMessages = userSettings.showTimeInActionMessages.value,
                            conversation = screenState.value.conversation
                        )
                    }

                    this.messages.emit(fullMessages)
                    screenState.setValue { newState.copy(messages = loadedMessages) }
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
                idDiff
            }
        }
    }

    private fun sendMessage() {
        lastMessageText = screenState.value.message.text

        val newMessage = VkMessage(
            id = -1 - sendingMessages.size,
            conversationMessageId = -1,
            text = lastMessageText,
            isOut = true,
            peerId = screenState.value.conversationId,
            fromId = UserConfig.userId,
            date = (System.currentTimeMillis() / 1000).toInt(),
            randomId = Random.nextInt(),
            action = null,
            actionMemberId = null,
            actionText = null,
            actionConversationMessageId = null,
            actionMessage = null,
            updateTime = null,
            isImportant = false,
            forwards = null,
            attachments = null,
            replyMessage = null,
            geoType = null,
            user = VkMemoryCache.getUser(UserConfig.userId),
            group = null,
            actionUser = null,
            actionGroup = null,
            isPinned = false,
            pinnedAt = null
        )
        sendingMessages += newMessage

        val newMessages = screenState.value.messages.toMutableList()
        val newUiMessage = newMessage.asPresentation(
            resourceProvider = resourceProvider,
            showName = false,
            prevMessage = messages.value.firstOrNull(),
            nextMessage = null,
            showTimeInActionMessages = userSettings.showTimeInActionMessages.value,
            conversation = screenState.value.conversation
        )
        newMessages.add(0, newUiMessage)

        screenState.setValue { old ->
            old.copy(
                message = TextFieldValue(),
                actionMode = ActionMode.Record,
                messages = listOf(newUiMessage).plus(old.messages)
            )
        }

        messagesUseCase.sendMessage(
            peerId = screenState.value.conversationId,
            randomId = newMessage.randomId,
            message = newMessage.text,
            replyTo = null,
            attachments = null
        ).listenValue(viewModelScope) { state ->
            state.processState(
                error = { error ->
                    sendingMessages -= newMessage

                    val uiMessages = screenState.value.messages.toMutableList()

                    uiMessages.indexOfOrNull(newUiMessage)?.let { index ->
                        (uiMessages[index] as? UiItem.Message)?.let { message ->
                            uiMessages[index] = message.copy(sendingStatus = SendingStatus.FAILED)
                        }
                    }

                    screenState.setValue { old -> old.copy(messages = uiMessages) }
                },
                success = { messageId ->
                    sendingMessages -= newMessage

                    val uiMessages = screenState.value.messages.toMutableList()
                    messages.setValue { old ->
                        listOf(newMessage.copy(id = messageId)).plus(old)
                    }

                    uiMessages.indexOfOrNull(newUiMessage)?.let { index ->
                        (uiMessages[index] as? UiItem.Message)?.let { message ->
                            uiMessages[index] = message
                                .copy(
                                    id = messageId,
                                    sendingStatus = SendingStatus.SENT
                                )
                                .copy(isRead = newMessage.isRead(screenState.value.conversation))
                        }
                    }

                    screenState.setValue { old -> old.copy(messages = uiMessages) }
                }
            )
        }
    }

    fun markAsImportant(
        messagesIds: List<Int>,
        important: Boolean,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
//            sendRequest(
//                request = {
//                    messagesRepository.markAsImportant(
//                        MessagesMarkAsImportantRequest(
//                            messagesIds = messagesIds,
//                            important = important
//                        )
//                    )
//                },
//                onResponse = { response ->
//                    val markedIds = response.response ?: emptyList()
//                    // TODO: 25.08.2023, Danil Nikolaev: update messages
//                }
//            )
        }
    }

    fun pinMessage(
        peerId: Int,
        messageId: Int? = null,
        conversationMessageId: Int? = null,
        pin: Boolean,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
//            if (pin) {
//                val pinnedMessage = sendRequest {
//                    messagesRepository.pin(
//                        MessagesPinMessageRequest(
//                            peerId = peerId,
//                            messageId = messageId,
//                            conversationMessageId = conversationMessageId
//                        )
//                    )
//                } ?: return@launch
//
//                // TODO: 25.08.2023, Danil Nikolaev: update message
//            } else {
//                val unpinnedMessage = sendRequest {
//                    messagesRepository.unpin(MessagesUnPinMessageRequest(peerId = peerId))
//                } ?: return@launch
//
//                // TODO: 25.08.2023, Danil Nikolaev: update message
//            }
        }
    }

    fun deleteMessage(
        peerId: Int,
        messagesIds: List<Int>? = null,
        conversationsMessagesIds: List<Int>? = null,
        isSpam: Boolean? = null,
        deleteForAll: Boolean? = null,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
//            sendRequest {
//                messagesRepository.delete(
//                    MessagesDeleteRequest(
//                        peerId = peerId,
//                        messagesIds = messagesIds,
//                        conversationsMessagesIds = conversationsMessagesIds,
//                        isSpam = isSpam,
//                        deleteForAll = deleteForAll
//                    )
//                )
//            } ?: return@launch

            // TODO: 25.08.2023, Danil Nikolaev: handle deleting
        }
    }

    fun editMessage(
        originalMessage: VkMessage,
        peerId: Int,
        messageId: Int,
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

    fun readMessage(peerId: Int, messageId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
//            sendRequest {
//                messagesRepository.markAsRead(peerId, startMessageId = messageId)
//            } ?: return@launch

            // TODO: 25.08.2023, Danil Nikolaev: update messages
        }
    }

    private fun toggleShowTimeInActionMessages(show: Boolean) {
        val messages = messages.value
        val uiMessages = messages.mapIndexed { index, item ->
            item.asPresentation(
                resourceProvider = resourceProvider,
                showName = false,
                prevMessage = messages.getOrNull(index + 1),
                nextMessage = messages.getOrNull(index - 1),
                showTimeInActionMessages = show,
                conversation = screenState.value.conversation
            )
        }

        screenState.setValue { old ->
            old.copy(messages = uiMessages)
        }
    }

    companion object {
        const val MESSAGES_LOAD_COUNT = 30
    }
}


// TODO: 25.08.2023, Danil Nikolaev: this and down below - rewrite
//    suspend fun uploadPhoto(
//        peerId: Int,
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

//    private suspend fun getPhotoMessageUploadServer(peerId: Int) {
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
//        peerId: Int,
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
//        peerId: Int,
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
//data class MessagesDeleteEvent(val peerId: Int, val messagesIds: List<Int>) : VkEvent()
//
//data class MessagesEditEvent(val message: VkMessageDomain) : VkEvent()
//
//data class MessagesReadEvent(
//    val isOut: Boolean,
//    val peerId: Int,
//    val messageId: Int,
//) : VkEvent()
//
//data class MessagesNewEvent(
//    val message: VkMessageDomain,
//    val profiles: HashMap<Int, VkUserDomain>,
//    val groups: HashMap<Int, VkGroupDomain>,
//) : VkEvent()
