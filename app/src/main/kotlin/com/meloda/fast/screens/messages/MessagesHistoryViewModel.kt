package com.meloda.fast.screens.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.VkUtils.fill
import com.meloda.fast.api.longpoll.LongPollEvent
import com.meloda.fast.api.longpoll.LongPollUpdatesParser
import com.meloda.fast.api.model.data.VkGroupData
import com.meloda.fast.api.model.data.VkUserData
import com.meloda.fast.api.model.domain.VkAttachment
import com.meloda.fast.api.model.domain.VkConversationDomain
import com.meloda.fast.api.model.domain.VkGroupDomain
import com.meloda.fast.api.model.domain.VkMessageDomain
import com.meloda.fast.api.model.domain.VkUserDomain
import com.meloda.fast.api.model.presentation.VkConversationUi
import com.meloda.fast.base.processState
import com.meloda.fast.base.viewmodel.VkEvent
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.data.audios.AudiosRepository
import com.meloda.fast.data.files.FilesRepository
import com.meloda.fast.screens.messages.domain.usecase.MessagesUseCase
import com.meloda.fast.data.photos.PhotosRepository
import com.meloda.fast.data.videos.VideosRepository
import com.meloda.fast.ext.emitOnMainScope
import com.meloda.fast.ext.listenValue
import com.meloda.fast.ext.setValue
import com.meloda.fast.ext.toMap
import com.meloda.fast.ext.updateValue
import com.meloda.fast.screens.messages.model.MessagesHistoryArguments
import com.meloda.fast.screens.messages.model.MessagesHistoryScreenState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.suspendCoroutine
import kotlin.properties.Delegates

interface MessagesHistoryViewModel {

    val screenState: StateFlow<MessagesHistoryScreenState>

    fun onAttachmentButtonClicked()
    fun onInputChanged(newText: String)
    fun onEmojiButtonClicked()
    fun onActionButtonClicked()
    fun onTopAppBarMenuClicked(id: Int)
    fun setArguments(arguments: MessagesHistoryArguments)

    fun onChatMaterialsOpened()
}

class MessagesHistoryViewModelImpl(
    private val messagesUseCase: MessagesUseCase,
    updatesParser: LongPollUpdatesParser,
    private val photosRepository: PhotosRepository,
    private val filesRepository: FilesRepository,
    private val audiosRepository: AudiosRepository,
    private val videosRepository: VideosRepository
) : MessagesHistoryViewModel, ViewModel() {

    override val screenState = MutableStateFlow(MessagesHistoryScreenState.EMPTY)

    private var conversation: VkConversationUi by Delegates.notNull()

    private val messages = MutableStateFlow<List<VkMessageDomain>>(emptyList())

    // TODO: 25.08.2023, Danil Nikolaev: extract to DI
    private val imageLoader by lazy {
        ImageLoader.Builder(AppGlobal.Instance)
            .crossfade(true)
            .build()
    }

    init {
        updatesParser.onNewMessage(::handleNewMessage)
        updatesParser.onMessageEdited(::handleEditedMessage)
        updatesParser.onMessageIncomingRead(::handleReadIncomingEvent)
        updatesParser.onMessageOutgoingRead(::handleReadOutgoingEvent)
    }

    override fun onAttachmentButtonClicked() {

    }

    override fun onInputChanged(newText: String) {
        screenState.value.copy(message = newText).let { newValue ->
            screenState.updateValue(newValue)
        }
    }

    override fun onEmojiButtonClicked() {

    }

    override fun onActionButtonClicked() {

    }

    override fun onTopAppBarMenuClicked(id: Int) {
        when (id) {
            0 -> loadMessagesHistory()
            1 -> screenState.setValue { old -> old.copy(isNeedToOpenChatMaterials = true) }
            else -> Unit
        }
    }

    override fun setArguments(arguments: MessagesHistoryArguments) {
        conversation = arguments.conversation

        val title = conversation.title
        val avatar = conversation.avatar

        screenState.emitOnMainScope(
            screenState.value.copy(
                title = title,
                avatar = avatar
            )
        )

        loadMessagesHistory()
    }

    override fun onChatMaterialsOpened() {
        screenState.setValue { old -> old.copy(isNeedToOpenChatMaterials = false) }
    }

    private fun handleNewMessage(event: LongPollEvent.VkMessageNewEvent) {

    }

    private fun handleEditedMessage(event: LongPollEvent.VkMessageEditEvent) {

    }

    private fun handleReadIncomingEvent(event: LongPollEvent.VkMessageReadIncomingEvent) {

    }

    private fun handleReadOutgoingEvent(event: LongPollEvent.VkMessageReadOutgoingEvent) {

    }

    private fun loadMessagesHistory() {
        messagesUseCase.getHistory(
            count = 100,
            offset = null,
            peerId = conversation.conversationId,
            extended = true,
            startMessageId = null,
            rev = null,
            fields = VKConstants.ALL_FIELDS
        ).listenValue { state ->
            state.processState(
                error = { error -> {} },
                success = { response ->
                    val profiles = response.profiles
                        ?.map(VkUserData::mapToDomain)
                        ?.toMap(hashMapOf(), VkUserDomain::id) ?: hashMapOf()
                    val groups = response.groups
                        ?.map(VkGroupData::mapToDomain)
                        ?.toMap(hashMapOf(), VkGroupDomain::id) ?: hashMapOf()
                    val newMessages = response.items
                        .map { message -> message.asVkMessage() }
                        .map { message ->
                            message.copy(
                                user = profiles[message.fromId],
                                group = groups[message.fromId],
                                actionUser = profiles[message.actionMemberId],
                                actionGroup = groups[message.actionMemberId]
                            )
                        }.sortedBy { message -> message.date }
                    messages.emit(newMessages)
//                    messagesRepository.store(newMessages)
                    val conversations = response.conversations?.map { base ->
                        val lastMessage =
                            newMessages.find { message -> message.id == base.last_message_id }
                        base.mapToDomain(lastMessage = lastMessage)
                            .fill(lastMessage = lastMessage, profiles = profiles, groups = groups)
                            .mapToPresentation()
                    } ?: emptyList()
                    val photos = profiles.mapNotNull { profile -> profile.value.photo200 } +
                            groups.mapNotNull { group -> group.value.photo200 } +
                            conversations.mapNotNull { conversation -> conversation.avatar.extractUrl() }
                    photos.forEach { url ->
                        ImageRequest.Builder(AppGlobal.Instance)
                            .data(url)
                            .build()
                            .let(imageLoader::enqueue)
                    }
                    screenState.emitOnMainScope(
                        screenState.value.copy(
                            messages = newMessages,
                            isLoading = false
                        )
                    )
                }
            )
            screenState.emit(screenState.value.copy(isLoading = state.isLoading()))
        }

//        viewModelScope.launch(Dispatchers.IO) {
//            screenState.setValue { old -> old.copy(isLoading = true) }

//            sendRequest(
//                request = {
//                    messagesRepository.getHistory(
//                        MessagesGetHistoryRequest(
//                            count = 100,
//                            peerId = conversation.conversationId,
//                            extended = true,
//                            fields = VKConstants.ALL_FIELDS,
//                        )
//                    )
//                },
//                onResponse = { response ->
//                    val answer = response.response ?: return@sendRequest
//
//                    val profiles = answer.profiles
//                        ?.map(VkUserData::mapToDomain)
//                        ?.toMap(hashMapOf(), VkUserDomain::id) ?: hashMapOf()
//
//                    val groups = answer.groups
//                        ?.map(VkGroupData::mapToDomain)
//                        ?.toMap(hashMapOf(), VkGroupDomain::id) ?: hashMapOf()
//
//                    val newMessages = answer.items
//                        .map { message -> message.asVkMessage() }
//                        .map { message ->
//                            message.copy(
//                                user = profiles[message.fromId],
//                                group = groups[message.fromId],
//                                actionUser = profiles[message.actionMemberId],
//                                actionGroup = groups[message.actionMemberId]
//                            )
//                        }.sortedBy { message -> message.date }
//
//                    messages.emit(newMessages)
//                    messagesRepository.store(newMessages)
//
//                    val conversations = answer.conversations?.map { base ->
//                        val lastMessage =
//                            newMessages.find { message -> message.id == base.last_message_id }
//
//                        base.mapToDomain(lastMessage = lastMessage)
//                            .fill(lastMessage = lastMessage, profiles = profiles, groups = groups)
//                            .mapToPresentation()
//                    } ?: emptyList()
//
//                    val photos = profiles.mapNotNull { profile -> profile.value.photo200 } +
//                            groups.mapNotNull { group -> group.value.photo200 } +
//                            conversations.mapNotNull { conversation -> conversation.avatar.extractUrl() }
//
//                    photos.forEach { url ->
//                        ImageRequest.Builder(AppGlobal.Instance)
//                            .data(url)
//                            .build()
//                            .let(imageLoader::enqueue)
//                    }
//
//                    screenState.emitOnMainScope(
//                        screenState.value.copy(
//                            messages = newMessages,
//                            isLoading = false
//                        )
//                    )
//                },
//                onAnyResult = {
//                    screenState.setValue { old -> old.copy(isLoading = true) }
//                }
//            )
//        }
    }

    fun sendMessage(
        peerId: Int,
        message: String? = null,
        randomId: Int = 0,
        replyTo: Int? = null,
        setId: ((messageId: Int) -> Unit)? = null,
        attachments: List<VkAttachment>? = null,
    ) {
        viewModelScope.launch(Dispatchers.IO) {

//            sendRequest(
//                request = {
//                    messagesRepository.send(
//                        MessagesSendRequest(
//                            peerId = peerId,
//                            randomId = randomId,
//                            message = message,
//                            replyTo = replyTo,
//                            attachments = attachments
//
//                        )
//                    )
//                },
//                onResponse = { response ->
//                    val sentMessageId = response.response ?: -1
//                    setId?.invoke(sentMessageId)
//                },
//            )
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
        originalMessage: VkMessageDomain,
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


    // TODO: 25.08.2023, Danil Nikolaev: this and down below - rewrite
    suspend fun uploadPhoto(
        peerId: Int,
        photo: File,
        name: String,
    ) {
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
    }

    private suspend fun getPhotoMessageUploadServer(peerId: Int) {
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
    }

    private suspend fun uploadPhotoToServer(
        uploadUrl: String,
        photo: File,
        name: String,
    ) {
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
    }

    private suspend fun saveMessagePhoto(
        server: Int,
        photo: String,
        hash: String,
    ) = suspendCoroutine<VkAttachment> { continuation ->
        viewModelScope.launch {
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
        }
    }

    suspend fun uploadVideo(
        file: File,
        name: String,
    ) {
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
    }

    private suspend fun getVideoMessageUploadServer() {
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
    }

    private suspend fun uploadVideoToServer(
        uploadUrl: String,
        file: File,
        name: String,
    ) {
//        viewModelScope.launch {
//            val requestBody = file.asRequestBody()
//            val body = MultipartBody.Part.createFormData("video_file", name, requestBody)
//
//            sendRequest(
//                onError = { exception -> throw exception },
//                request = { videosRepository.upload(uploadUrl, body) }
//            )
//        }
    }

    suspend fun uploadAudio(
        file: File,
        name: String,
    ) {
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
    }

    private suspend fun getAudioUploadServer() {
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
    }

    private suspend fun uploadAudioToServer(
        uploadUrl: String,
        file: File,
        name: String,
    ) {
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
    }

    private suspend fun saveMessageAudio(
        server: Int,
        audio: String,
        hash: String,
    ) {
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
    }

    suspend fun uploadFile(
        peerId: Int,
        file: File,
        name: String,
        type: FilesRepository.FileType,
    ) {
//        suspendCoroutine { continuation ->
//            viewModelScope.launch {
//                val uploadServerUrl = getFileMessageUploadServer(peerId, type)
//                val uploadedFileInfo = uploadFileToServer(uploadServerUrl, file, name)
//                val savedAttachmentPair = saveMessageFile(uploadedFileInfo)
//
//                continuation.resume(savedAttachmentPair.second)
//            }
//        }
    }

    private suspend fun getFileMessageUploadServer(
        peerId: Int,
        type: FilesRepository.FileType,
    ) {
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
    }

    private suspend fun uploadFileToServer(
        uploadUrl: String,
        file: File,
        name: String,
    ) {
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
    }

    private suspend fun saveMessageFile(file: String) {
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
    }
}

data class MessagesLoadedEvent(
    val count: Int,
    val conversations: HashMap<Int, VkConversationDomain>,
    val messages: List<VkMessageDomain>,
    val profiles: HashMap<Int, VkUserDomain>,
    val groups: HashMap<Int, VkGroupDomain>,
) : VkEvent()

data class MessagesMarkAsImportantEvent(val messagesIds: List<Int>, val important: Boolean) :
    VkEvent()

data class MessagesPinEvent(val message: VkMessageDomain) : VkEvent()

object MessagesUnpinEvent : VkEvent()

data class MessagesDeleteEvent(val peerId: Int, val messagesIds: List<Int>) : VkEvent()

data class MessagesEditEvent(val message: VkMessageDomain) : VkEvent()

data class MessagesReadEvent(
    val isOut: Boolean,
    val peerId: Int,
    val messageId: Int,
) : VkEvent()

data class MessagesNewEvent(
    val message: VkMessageDomain,
    val profiles: HashMap<Int, VkUserDomain>,
    val groups: HashMap<Int, VkGroupDomain>,
) : VkEvent()
