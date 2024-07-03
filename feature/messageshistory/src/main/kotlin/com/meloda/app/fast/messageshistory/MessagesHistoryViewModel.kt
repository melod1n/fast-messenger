package com.meloda.app.fast.messageshistory

import android.content.SharedPreferences
import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.conena.nanokt.collections.indexOfOrNull
import com.conena.nanokt.text.isEmptyOrBlank
import com.meloda.app.fast.common.UserConfig
import com.meloda.app.fast.common.extensions.listenValue
import com.meloda.app.fast.common.extensions.setValue
import com.meloda.app.fast.common.extensions.updateValue
import com.meloda.app.fast.data.api.conversations.ConversationsUseCase
import com.meloda.app.fast.data.api.messages.MessagesUseCase
import com.meloda.app.fast.data.processState
import com.meloda.app.fast.datastore.SettingsKeys
import com.meloda.app.fast.messageshistory.model.ActionMode
import com.meloda.app.fast.messageshistory.model.MessagesHistoryArguments
import com.meloda.app.fast.messageshistory.model.MessagesHistoryScreenState
import com.meloda.app.fast.messageshistory.util.extractAvatar
import com.meloda.app.fast.messageshistory.util.extractTitle
import com.meloda.app.fast.model.BaseError
import com.meloda.app.fast.model.api.domain.VkAttachment
import com.meloda.app.fast.model.api.domain.VkMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

interface MessagesHistoryViewModel {

    val screenState: StateFlow<MessagesHistoryScreenState>

    val baseError: StateFlow<BaseError?>
    val imagesToPreload: StateFlow<List<String>>

    val currentOffset: StateFlow<Int>

    val canPaginate: StateFlow<Boolean>

    fun onAttachmentButtonClicked()
    fun onInputChanged(newText: String)
    fun onEmojiButtonClicked()
    fun onActionButtonClicked()
    fun onTopAppBarMenuClicked(id: Int)
    fun setArguments(arguments: MessagesHistoryArguments)

    fun onMetPaginationCondition()
}

class MessagesHistoryViewModelImpl(
    private val messagesUseCase: MessagesUseCase,
    private val conversationsUseCase: ConversationsUseCase,
    private val preferences: SharedPreferences,
    private val resources: Resources
//    updatesParser: LongPollUpdatesParser,
) : MessagesHistoryViewModel, ViewModel() {

    override val screenState = MutableStateFlow(MessagesHistoryScreenState.EMPTY)

    override val baseError = MutableStateFlow<BaseError?>(null)
    override val imagesToPreload = MutableStateFlow<List<String>>(emptyList())

    override val currentOffset = MutableStateFlow(0)

    override val canPaginate = MutableStateFlow(false)

    private val messages = MutableStateFlow<List<VkMessage>>(emptyList())

    private var lastMessageText: String? = null

    init {
//        updatesParser.onNewMessage(::handleNewMessage)
//        updatesParser.onMessageEdited(::handleEditedMessage)
//        updatesParser.onMessageIncomingRead(::handleReadIncomingEvent)
//        updatesParser.onMessageOutgoingRead(::handleReadOutgoingEvent)
    }

    override fun onAttachmentButtonClicked() {

    }

    override fun onInputChanged(newText: String) {
        screenState.setValue { old ->
            old.copy(
                message = newText,
                actionMode = if (newText.isEmptyOrBlank()) ActionMode.Record
                else ActionMode.Send
            )
        }

        screenState.value.copy(message = newText).let { newValue ->
            screenState.updateValue(newValue)
        }
    }

    override fun onEmojiButtonClicked() {

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

    override fun onTopAppBarMenuClicked(id: Int) {
        when (id) {
            0 -> loadMessagesHistory(0)
            else -> Unit
        }
    }

    override fun setArguments(arguments: MessagesHistoryArguments) {
        if (arguments.conversationId == screenState.value.conversationId) return

        screenState.setValue { old -> old.copy(conversationId = arguments.conversationId) }
        loadMessagesHistory()
    }

    override fun onMetPaginationCondition() {
        currentOffset.update { screenState.value.messages.size }
        loadMessagesHistory()
    }

//    private fun handleNewMessage(event: LongPollEvent.VkMessageNewEvent) {
//        val message = event.message
//        if (message.peerId != screenState.value.conversationId) return
//
//        val randomIds = screenState.value.messages.map(VkMessageDomain::randomId)
//        if (message.randomId != 0 && message.randomId in randomIds) return
//
//        val messages = screenState.value.messages.toMutableList()
//        messages.add(message)
//
//        screenState.setValue { old -> old.copy(messages = messages) }
//    }

//    private fun handleEditedMessage(event: LongPollEvent.VkMessageEditEvent) {
//        val message = event.message
//        if (message.peerId != screenState.value.conversationId) return
//
//        screenState.value.messages
//            .findIndex { it.id == message.id }
//            ?.let { index ->
//                screenState.setValue { old ->
//                    old.copy(
//                        messages = old.messages.toMutableList().apply {
//                            this[index] = message
//                        }
//                    )
//                }
//            }
//    }
//
//    private fun handleReadIncomingEvent(event: LongPollEvent.VkMessageReadIncomingEvent) {
//
//    }
//
//    private fun handleReadOutgoingEvent(event: LongPollEvent.VkMessageReadOutgoingEvent) {
//
//    }

    private fun loadMessagesHistory(offset: Int = currentOffset.value) {
        Log.d("MessagesHistoryViewModel", "loadMessagesHistory: $offset")

        messagesUseCase.getMessagesHistory(
            conversationId = screenState.value.conversationId,
            count = MESSAGES_LOAD_COUNT,
            offset = offset,
        ).listenValue { state ->
            state.processState(
                error = { error ->

                },
                success = { response ->
                    val messages = response.messages
                    val conversations = response.conversations

                    imagesToPreload.setValue {
                        messages.mapNotNull { it.extractAvatar().extractUrl() }
                    }

                    messagesUseCase.storeMessages(messages)
                    conversationsUseCase.storeConversations(conversations)

                    val loadedMessages = messages.map {
                        // TODO: 02/07/2024, Danil Nikolaev: map to ui

                        it
                    }

                    val itemsCountSufficient = messages.size == MESSAGES_LOAD_COUNT

                    val paginationExhausted = !itemsCountSufficient &&
                            screenState.value.messages.isNotEmpty()
                    var newState = screenState.value.copy(
                        isPaginationExhausted = paginationExhausted,
                    )

                    conversations
                        .firstOrNull { it.id == screenState.value.conversationId }
                        ?.let { conversation ->
                            newState = newState.copy(
                                title = conversation.extractTitle(
                                    useContactName = preferences.getBoolean(
                                        SettingsKeys.KEY_USE_CONTACT_NAMES,
                                        SettingsKeys.DEFAULT_VALUE_USE_CONTACT_NAMES
                                    ),
                                    resources = resources
                                ),
                                avatar = conversation.extractAvatar()
                            )
                        }

                    if (offset == 0) {
                        this.messages.emit(messages)
                        screenState.setValue {
                            newState.copy(messages = loadedMessages)
                        }
                    } else {
                        this.messages.emit(this.messages.value.plus(messages))
                        screenState.setValue {
                            newState.copy(messages = newState.messages.plus(loadedMessages))
                        }
                    }

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

    private fun sendMessage() {
        lastMessageText = screenState.value.message

        val newMessage = VkMessage(
            id = 0,
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
            important = false,
            forwards = emptyList(),
            attachments = emptyList(),
            replyMessage = null,
            geoType = null,
            user = null,
            group = null,
            actionUser = null,
            actionGroup = null,
        )

        screenState.setValue { old ->
            old.copy(
                message = "",
                actionMode = ActionMode.Record,
                messages = old.messages.toMutableList().apply {
                    add(newMessage)
                }
            )
        }

        messagesUseCase.sendMessage(
            peerId = newMessage.peerId,
            randomId = newMessage.randomId,
            message = newMessage.text,
            replyTo = null,
            attachments = null
        ).listenValue { state ->
            state.processState(
                error = {},
                success = { messageId ->
                    val messages = screenState.value.messages.toMutableList()
                    messages.indexOfOrNull(newMessage)?.let { index ->
                        messages[index] = newMessage.copy(id = messageId)
                    }

                    screenState.setValue { old -> old.copy(messages = messages) }
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
