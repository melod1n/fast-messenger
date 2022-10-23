package com.meloda.fast.screens.messages

import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.base.ApiError
import com.meloda.fast.api.longpoll.LongPollEvent
import com.meloda.fast.api.longpoll.LongPollUpdatesParser
import com.meloda.fast.api.model.VkConversation
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.model.attachments.VkAttachment
import com.meloda.fast.api.model.attachments.VkVideo
import com.meloda.fast.api.network.ApiAnswer
import com.meloda.fast.api.network.messages.*
import com.meloda.fast.api.network.photos.PhotosSaveMessagePhotoRequest
import com.meloda.fast.base.viewmodel.BaseViewModel
import com.meloda.fast.base.viewmodel.VkEvent
import com.meloda.fast.data.audios.AudiosRepository
import com.meloda.fast.data.files.FilesRepository
import com.meloda.fast.data.messages.MessagesRepository
import com.meloda.fast.data.photos.PhotosRepository
import com.meloda.fast.data.videos.VideosRepository
import com.meloda.fast.ext.requireNotNull
import com.meloda.fast.screens.conversations.MessagesNewEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


@HiltViewModel
class MessagesHistoryViewModel @Inject constructor(
    private val messagesRepository: MessagesRepository,
    updatesParser: LongPollUpdatesParser,
    private val photosRepository: PhotosRepository,
    private val filesRepository: FilesRepository,
    private val audiosRepository: AudiosRepository,
    private val videosRepository: VideosRepository
) : BaseViewModel() {

    init {
        updatesParser.onNewMessage {
            launch { handleNewMessage(it) }
        }

        updatesParser.onMessageEdited {
            launch { handleEditedMessage(it) }
        }

        updatesParser.onMessageIncomingRead {
            launch { handleReadIncomingEvent(it) }
        }

        updatesParser.onMessageOutgoingRead {
            launch { handleReadOutgoingEvent(it) }
        }
    }

    private suspend fun handleNewMessage(event: LongPollEvent.VkMessageNewEvent) {
        sendEvent(MessagesNewEvent(event.message, event.profiles, event.groups))
    }

    private suspend fun handleEditedMessage(event: LongPollEvent.VkMessageEditEvent) {
        sendEvent(MessagesEditEvent(event.message))
    }

    private suspend fun handleReadIncomingEvent(event: LongPollEvent.VkMessageReadIncomingEvent) {
        sendEvent(
            MessagesReadEvent(
                isOut = false,
                peerId = event.peerId,
                messageId = event.messageId
            )
        )
    }

    private suspend fun handleReadOutgoingEvent(event: LongPollEvent.VkMessageReadOutgoingEvent) {
        sendEvent(
            MessagesReadEvent(
                isOut = true,
                peerId = event.peerId,
                messageId = event.messageId
            )
        )
    }

    fun loadHistory(peerId: Int) = launch {
        makeJob({
            messagesRepository.getHistory(
                MessagesGetHistoryRequest(
                    count = 100,
                    peerId = peerId,
                    extended = true,
                    fields = VKConstants.ALL_FIELDS
                )
            )
        },
            onAnswer = {
                val response = it.response ?: return@makeJob

                val profiles = hashMapOf<Int, VkUser>()
                response.profiles?.let { baseProfiles ->
                    baseProfiles.forEach { baseProfile ->
                        baseProfile.asVkUser().let { profile -> profiles[profile.id] = profile }
                    }
                }

                val groups = hashMapOf<Int, VkGroup>()
                response.groups?.let { baseGroups ->
                    baseGroups.forEach { baseGroup ->
                        baseGroup.asVkGroup().let { group -> groups[group.id] = group }
                    }
                }

                val hashMessages = hashMapOf<Int, VkMessage>()
                response.items.forEach { baseMessage ->
                    baseMessage.asVkMessage()
                        .let { message -> hashMessages[message.id] = message }
                }

                messagesRepository.store(hashMessages.values.toList())

                val conversations = hashMapOf<Int, VkConversation>()
                response.conversations?.let { baseConversations ->
                    baseConversations.forEach { baseConversation ->
                        baseConversation.asVkConversation(
                            hashMessages[baseConversation.last_message_id]
                        ).let { conversation -> conversations[conversation.id] = conversation }
                    }
                }

                sendEvent(
                    MessagesLoadedEvent(
                        count = response.count,
                        profiles = profiles,
                        groups = groups,
                        conversations = conversations,
                        messages = hashMessages.values.toList()
                    )
                )
            })
    }

    fun sendMessage(
        peerId: Int,
        message: String? = null,
        randomId: Int = 0,
        replyTo: Int? = null,
        setId: ((messageId: Int) -> Unit)? = null,
        onError: ((error: Throwable) -> Unit)? = null,
        attachments: List<VkAttachment>? = null
    ) = launch {
        makeJob(
            {
                messagesRepository.send(
                    MessagesSendRequest(
                        peerId = peerId,
                        randomId = randomId,
                        message = message,
                        replyTo = replyTo,
                        attachments = attachments
                    )
                )
            },
            onAnswer = {
                val response = it.response ?: return@makeJob
                setId?.invoke(response)
            },
            onError = {
                onError?.invoke(it)
            })
    }

    fun markAsImportant(
        messagesIds: List<Int>,
        important: Boolean
    ) = launch {
        makeJob({
            messagesRepository.markAsImportant(
                MessagesMarkAsImportantRequest(
                    messagesIds = messagesIds,
                    important = important
                )
            )
        },
            onAnswer = {
                val response = it.response ?: return@makeJob
                sendEvent(
                    MessagesMarkAsImportantEvent(
                        messagesIds = response,
                        important = important
                    )
                )
            })
    }

    fun pinMessage(
        peerId: Int,
        messageId: Int? = null,
        conversationMessageId: Int? = null,
        pin: Boolean
    ) = launch {
        if (pin) {
            makeJob({
                messagesRepository.pin(
                    MessagesPinMessageRequest(
                        peerId = peerId,
                        messageId = messageId,
                        conversationMessageId = conversationMessageId
                    )
                )
            },
                onAnswer = {
                    val response = it.response ?: return@makeJob
                    sendEvent(MessagesPinEvent(response.asVkMessage()))
                }
            )
        } else {
            makeJob({ messagesRepository.unpin(MessagesUnPinMessageRequest(peerId = peerId)) },
                onAnswer = {
                    println("Fast::MessagesHistoryViewModel::unPin::Response::${it.response}")
                    sendEvent(MessagesUnpinEvent)
                }
            )
        }
    }

    fun deleteMessage(
        peerId: Int,
        messagesIds: List<Int>? = null,
        conversationsMessagesIds: List<Int>? = null,
        isSpam: Boolean? = null,
        deleteForAll: Boolean? = null
    ) = launch {
        makeJob(
            {
                messagesRepository.delete(
                    MessagesDeleteRequest(
                        peerId = peerId,
                        messagesIds = messagesIds,
                        conversationsMessagesIds = conversationsMessagesIds,
                        isSpam = isSpam,
                        deleteForAll = deleteForAll
                    )
                )
            },
            onAnswer = {
                sendEvent(
                    MessagesDeleteEvent(
                        peerId = peerId,
                        messagesIds = messagesIds ?: emptyList()
                    )
                )
            })
    }

    fun editMessage(
        originalMessage: VkMessage,
        peerId: Int,
        messageId: Int,
        message: String? = null,
        attachments: List<VkAttachment>? = null
    ) = launch {
        makeJob(
            {
                messagesRepository.edit(
                    MessagesEditRequest(
                        peerId = peerId,
                        messageId = messageId,
                        message = message,
                        attachments = attachments
                    )
                )
            },
            onAnswer = {
                originalMessage.text = message
                sendEvent(MessagesEditEvent(originalMessage))
            }
        )
    }

    fun readMessage(peerId: Int, messageId: Int) {
        makeJob(
            { messagesRepository.markAsRead(peerId, startMessageId = messageId) },
            onAnswer = {
                sendEvent(MessagesReadEvent(false, peerId, messageId))
            }
        )
    }

    suspend fun uploadPhoto(
        peerId: Int,
        photo: File,
        name: String
    ) = suspendCoroutine<VkAttachment> {
        launch {
            val uploadServerUrl = getPhotoMessageUploadServer(peerId)
            val uploadedFileInfo = uploadPhotoToServer(uploadServerUrl, photo, name)

            val savedAttachment = saveMessagePhoto(
                uploadedFileInfo.first,
                uploadedFileInfo.second,
                uploadedFileInfo.third
            )

            it.resume(savedAttachment)
        }.also { it.invokeOnCompletion { launch { onStop() } } }
    }

    private suspend fun getPhotoMessageUploadServer(peerId: Int) = suspendCoroutine<String> {
        launch {
            val uploadServerResponse = makeSuspendJob(
                { photosRepository.getMessagesUploadServer(peerId) }
            )
            if (!uploadServerResponse.isSuccessful()) {
                throw requireNotNull(uploadServerResponse.error.throwable)
            } else {
                (uploadServerResponse as ApiAnswer.Success).run {
                    it.resume(requireNotNull(this.data.response?.uploadUrl))
                }
            }
        }
    }

    private suspend fun uploadPhotoToServer(
        uploadUrl: String,
        photo: File,
        name: String
    ) = suspendCoroutine<Triple<Int, String, String>> {
        launch {
            val requestBody = photo.asRequestBody("image/*".toMediaType())
            val body = MultipartBody.Part.createFormData("photo", name, requestBody)

            val uploadFileResponse = makeSuspendJob(
                { photosRepository.uploadPhoto(uploadUrl, body) }
            )
            if (!uploadFileResponse.isSuccessful()) {
                throw uploadFileResponse.error.throwable!!
            } else {
                (uploadFileResponse as ApiAnswer.Success).data.run {
                    it.resume(Triple(this.server, this.photo, this.hash))
                }
            }
        }
    }

    private suspend fun saveMessagePhoto(
        server: Int,
        photo: String,
        hash: String
    ) = suspendCoroutine<VkAttachment> {
        launch {
            val saveResponse = makeSuspendJob(
                {
                    photosRepository.saveMessagePhoto(
                        PhotosSaveMessagePhotoRequest(
                            photo,
                            server,
                            hash
                        )
                    )
                }
            )
            if (!saveResponse.isSuccessful()) {
                throw saveResponse.error.throwable!!
            } else {
                (saveResponse as ApiAnswer.Success).data.response?.run {
                    it.resume(requireNotNull(first().asVkPhoto()))
                }
            }
        }
    }

    suspend fun uploadVideo(
        file: File,
        name: String
    ) = suspendCoroutine<VkVideo> {
        launch {
            val uploadInfo = getVideoMessageUploadServer()

            uploadVideoToServer(
                uploadInfo.first,
                file,
                name
            )

            it.resume(uploadInfo.second)
        }
    }

    private suspend fun getVideoMessageUploadServer() = suspendCoroutine<Pair<String, VkVideo>> {
        launch {
            val saveResponse = makeSuspendJob(
                { videosRepository.save() }
            )

            if (!saveResponse.isSuccessful()) {
                it.resumeWithException(saveResponse.error.throwable!!)
                return@launch
            } else {
                val response = (saveResponse as ApiAnswer.Success).data.response ?: return@launch

                val uploadUrl = response.uploadUrl
                val video = VkVideo(
                    id = response.videoId,
                    ownerId = response.ownerId,
                    images = emptyList(),
                    firstFrames = null,
                    accessKey = response.accessKey,
                    title = response.title
                )

                it.resume(uploadUrl to video)
            }
        }
    }

    private suspend fun uploadVideoToServer(
        uploadUrl: String,
        file: File,
        name: String
    ) = launch {
        val requestBody = file.asRequestBody()
        val body = MultipartBody.Part.createFormData("video_file", name, requestBody)

        val response = makeSuspendJob(
            { videosRepository.upload(uploadUrl, body) }
        )
        if (!response.isSuccessful()) {
            throw response.error.throwable!!
        }
    }

    suspend fun uploadAudio(
        file: File,
        name: String
    ) = suspendCoroutine<VkAttachment> {
        launch {
            val uploadUrl = getAudioUploadServer()
            val uploadInfo = uploadAudioToServer(uploadUrl, file, name)
            val saveInfo = saveMessageAudio(
                uploadInfo.first, uploadInfo.second, uploadInfo.third
            )

            it.resume(saveInfo)
        }
    }

    private suspend fun getAudioUploadServer() = suspendCoroutine<String> {
        launch {
            val uploadResponse = makeSuspendJob(
                { audiosRepository.getUploadServer() }
            )
            if (!uploadResponse.isSuccessful()) {
                throw uploadResponse.error.throwable!!
            } else {
                (uploadResponse as ApiAnswer.Success).data.response.run {
                    it.resume(requireNotNull(this).uploadUrl)
                }
            }
        }
    }

    private suspend fun uploadAudioToServer(
        uploadUrl: String,
        file: File,
        name: String
    ) = suspendCoroutine<Triple<Int, String, String>> {
        launch {
            val requestBody = file.asRequestBody()
            val body = MultipartBody.Part.createFormData("file", name, requestBody)

            val uploadResponse = makeSuspendJob(
                { audiosRepository.upload(uploadUrl, body) }
            )
            if (!uploadResponse.isSuccessful()) {
                throw uploadResponse.error.throwable!!
            } else {
                (uploadResponse as ApiAnswer.Success).data.run {
                    if (this.error != null) {
                        throw ApiError(error = error)
                    } else {
                        it.resume(Triple(this.server, requireNotNull(this.audio), this.hash))
                    }
                }
            }
        }
    }

    private suspend fun saveMessageAudio(
        server: Int,
        audio: String,
        hash: String
    ) = suspendCoroutine<VkAttachment> {
        launch {
            val saveResponse = makeSuspendJob(
                { audiosRepository.save(server, audio, hash) }
            )
            if (!saveResponse.isSuccessful()) {
                throw saveResponse.error.throwable!!
            } else {
                (saveResponse as ApiAnswer.Success).data.response.run {
                    it.resume(requireNotNull(this).asVkAudio())
                }
            }
        }
    }

    suspend fun uploadFile(
        peerId: Int,
        file: File,
        name: String,
        type: FilesRepository.FileType
    ) = suspendCoroutine<VkAttachment> {
        launch {
            val uploadServerUrl = getFileMessageUploadServer(peerId, type)
            val uploadedFileInfo = uploadFileToServer(uploadServerUrl, file, name)
            val savedAttachmentPair = saveMessageFile(uploadedFileInfo)

            it.resume(savedAttachmentPair.second)
        }.also { it.invokeOnCompletion { launch { onStop() } } }
    }

    private suspend fun getFileMessageUploadServer(
        peerId: Int,
        type: FilesRepository.FileType
    ) = suspendCoroutine<String> {
        launch {
            val uploadServerResponse = makeSuspendJob(
                { filesRepository.getMessagesUploadServer(peerId, type) }
            )
            if (!uploadServerResponse.isSuccessful()) {
                throw uploadServerResponse.error.throwable!!
            } else {
                (uploadServerResponse as ApiAnswer.Success).data.response.run {
                    it.resume(requireNotNull(this).uploadUrl)
                }
            }
        }
    }

    private suspend fun uploadFileToServer(
        uploadUrl: String,
        file: File,
        name: String
    ) = suspendCoroutine<String> {
        launch {
            val requestBody = file.asRequestBody()
            val body = MultipartBody.Part.createFormData("file", name, requestBody)

            val uploadFileResponse = makeSuspendJob(
                { filesRepository.uploadFile(uploadUrl, body) }
            )
            if (!uploadFileResponse.isSuccessful()) {
                throw uploadFileResponse.error.throwable!!
            } else {
                (uploadFileResponse as ApiAnswer.Success).data.run {
                    if (this.error != null) {
                        throw ApiError(error = this.error)
                    } else {
                        it.resume(this.file.requireNotNull())
                    }
                }
            }
        }
    }

    private suspend fun saveMessageFile(file: String) =
        suspendCoroutine<Pair<String, VkAttachment>> {
            launch {
                val saveResponse = makeSuspendJob(
                    { filesRepository.saveMessageFile(file) }
                )
                if (!saveResponse.isSuccessful()) {
                    throw saveResponse.error.throwable!!
                } else {
                    (saveResponse as ApiAnswer.Success).data.run {
                        val response = this.response.requireNotNull()
                        it.resume(
                            response.type to (
                                    response.file?.asVkFile()
                                        ?: response.voiceMessage?.asVkVoiceMessage()
                                    ).requireNotNull()
                        )
                    }
                }
            }
        }
}

data class MessagesLoadedEvent(
    val count: Int,
    val conversations: HashMap<Int, VkConversation>,
    val messages: List<VkMessage>,
    val profiles: HashMap<Int, VkUser>,
    val groups: HashMap<Int, VkGroup>
) : VkEvent()

data class MessagesMarkAsImportantEvent(val messagesIds: List<Int>, val important: Boolean) :
    VkEvent()

data class MessagesPinEvent(val message: VkMessage) : VkEvent()

object MessagesUnpinEvent : VkEvent()

data class MessagesDeleteEvent(val peerId: Int, val messagesIds: List<Int>) : VkEvent()

data class MessagesEditEvent(val message: VkMessage) : VkEvent()

data class MessagesReadEvent(
    val isOut: Boolean,
    val peerId: Int,
    val messageId: Int
) : VkEvent()