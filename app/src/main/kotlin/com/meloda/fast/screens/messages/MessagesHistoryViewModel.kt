package com.meloda.fast.screens.messages

import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.base.ApiError
import com.meloda.fast.api.longpoll.LongPollEvent
import com.meloda.fast.api.longpoll.LongPollUpdatesParser
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.model.attachments.VkAttachment
import com.meloda.fast.api.model.attachments.VkVideo
import com.meloda.fast.api.model.domain.VkConversationDomain
import com.meloda.fast.api.network.messages.MessagesDeleteRequest
import com.meloda.fast.api.network.messages.MessagesEditRequest
import com.meloda.fast.api.network.messages.MessagesGetHistoryRequest
import com.meloda.fast.api.network.messages.MessagesMarkAsImportantRequest
import com.meloda.fast.api.network.messages.MessagesPinMessageRequest
import com.meloda.fast.api.network.messages.MessagesSendRequest
import com.meloda.fast.api.network.messages.MessagesUnPinMessageRequest
import com.meloda.fast.api.network.photos.PhotosSaveMessagePhotoRequest
import com.meloda.fast.base.viewmodel.DeprecatedBaseViewModel
import com.meloda.fast.base.viewmodel.VkEvent
import com.meloda.fast.data.audios.AudiosRepository
import com.meloda.fast.data.files.FilesRepository
import com.meloda.fast.data.messages.MessagesRepository
import com.meloda.fast.data.photos.PhotosRepository
import com.meloda.fast.data.videos.VideosRepository
import com.meloda.fast.ext.notNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MessagesHistoryViewModel constructor(
    private val messagesRepository: MessagesRepository,
    updatesParser: LongPollUpdatesParser,
    private val photosRepository: PhotosRepository,
    private val filesRepository: FilesRepository,
    private val audiosRepository: AudiosRepository,
    private val videosRepository: VideosRepository,
) : DeprecatedBaseViewModel() {

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
                        baseProfile.mapToDomain().let { profile -> profiles[profile.id] = profile }
                    }
                }

                val groups = hashMapOf<Int, VkGroup>()
                response.groups?.let { baseGroups ->
                    baseGroups.forEach { baseGroup ->
                        baseGroup.mapToDomain().let { group -> groups[group.id] = group }
                    }
                }

                val hashMessages = hashMapOf<Int, VkMessage>()
                response.items.forEach { baseMessage ->
                    baseMessage.asVkMessage()
                        .let { message -> hashMessages[message.id] = message }
                }

                messagesRepository.store(hashMessages.values.toList())

                val conversations = hashMapOf<Int, VkConversationDomain>()
                response.conversations?.let { baseConversations ->
                    baseConversations.forEach { baseConversation ->
                        baseConversation.mapToDomain(
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
        attachments: List<VkAttachment>? = null,
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
        important: Boolean,
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
        pin: Boolean,
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
        deleteForAll: Boolean? = null,
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
        attachments: List<VkAttachment>? = null,
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
        name: String,
    ) = suspendCoroutine {
        launch {
            val uploadServerUrl = getPhotoMessageUploadServer(peerId)
            val uploadedFileInfo = uploadPhotoToServer(uploadServerUrl, photo, name)

            val savedAttachment = saveMessagePhoto(
                uploadedFileInfo.first,
                uploadedFileInfo.second,
                uploadedFileInfo.third
            )

            it.resume(savedAttachment)
        }
    }

    private suspend fun getPhotoMessageUploadServer(peerId: Int) =
        suspendCoroutine { continuation ->
            launch {
                sendRequestNotNull(
                    onError = { exception ->
                        continuation.resumeWithException(exception)
                        true
                    },
                    request = { photosRepository.getMessagesUploadServer(peerId) }
                ).response?.let { response ->
                    continuation.resume(response.uploadUrl)
                }
            }
        }

    private suspend fun uploadPhotoToServer(
        uploadUrl: String,
        photo: File,
        name: String,
    ) = suspendCoroutine { continuation ->
        launch {
            val requestBody = photo.asRequestBody("image/*".toMediaType())
            val body = MultipartBody.Part.createFormData("photo", name, requestBody)

            sendRequestNotNull(
                onError = { exception ->
                    continuation.resumeWithException(exception)
                    true
                },
                request = { photosRepository.uploadPhoto(uploadUrl, body) }
            ).let { response ->
                continuation.resume(Triple(response.server, response.photo, response.hash))
            }
        }
    }

    private suspend fun saveMessagePhoto(
        server: Int,
        photo: String,
        hash: String,
    ) = suspendCoroutine<VkAttachment> { continuation ->
        launch {
            sendRequestNotNull(
                onError = { exception ->
                    continuation.resumeWithException(exception)
                    true
                },
                request = {
                    photosRepository.saveMessagePhoto(
                        PhotosSaveMessagePhotoRequest(photo, server, hash)
                    )
                }
            ).response?.first()?.asVkPhoto()?.let(continuation::resume)
        }
    }

    suspend fun uploadVideo(
        file: File,
        name: String,
    ) = suspendCoroutine {
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

    private suspend fun getVideoMessageUploadServer() = suspendCoroutine { continuation ->
        launch {
            sendRequestNotNull(
                onError = { exception ->
                    continuation.resumeWithException(exception)
                    true
                },
                request = { videosRepository.save() }
            ).response?.let { response ->
                val uploadUrl = response.uploadUrl
                val video = VkVideo(
                    id = response.videoId,
                    ownerId = response.ownerId,
                    images = emptyList(),
                    firstFrames = null,
                    accessKey = response.accessKey,
                    title = response.title
                )

                continuation.resume(uploadUrl to video)
            }
        }
    }

    private suspend fun uploadVideoToServer(
        uploadUrl: String,
        file: File,
        name: String,
    ) = launch {
        val requestBody = file.asRequestBody()
        val body = MultipartBody.Part.createFormData("video_file", name, requestBody)

        sendRequest(
            onError = { exception -> throw exception },
            request = { videosRepository.upload(uploadUrl, body) }
        )
    }

    suspend fun uploadAudio(
        file: File,
        name: String,
    ) = suspendCoroutine {
        launch {
            val uploadUrl = getAudioUploadServer()
            val uploadInfo = uploadAudioToServer(uploadUrl, file, name)
            val saveInfo = saveMessageAudio(
                uploadInfo.first, uploadInfo.second, uploadInfo.third
            )

            it.resume(saveInfo)
        }
    }

    private suspend fun getAudioUploadServer() = suspendCoroutine { continuation ->
        launch {
            sendRequestNotNull(
                onError = { exception ->
                    continuation.resumeWithException(exception)
                    true
                },
                request = { audiosRepository.getUploadServer() }
            ).response?.uploadUrl?.let(continuation::resume)
        }
    }

    private suspend fun uploadAudioToServer(
        uploadUrl: String,
        file: File,
        name: String,
    ) = suspendCoroutine { continuation ->
        launch {
            val requestBody = file.asRequestBody()
            val body = MultipartBody.Part.createFormData("file", name, requestBody)

            sendRequestNotNull(
                onError = { exception ->
                    continuation.resumeWithException(exception)
                    true
                },
                request = { audiosRepository.upload(uploadUrl, body) }
            ).let { response ->
                response.error?.let { error -> throw ApiError(error = error) }

                continuation.resume(
                    Triple(response.server, response.audio.notNull(), response.hash)
                )
            }
        }
    }

    private suspend fun saveMessageAudio(
        server: Int,
        audio: String,
        hash: String,
    ) = suspendCoroutine<VkAttachment> { continuation ->
        launch {
            sendRequestNotNull(
                onError = { exception ->
                    continuation.resumeWithException(exception)
                    true
                },
                request = { audiosRepository.save(server, audio, hash) }
            ).response?.asVkAudio()?.let(continuation::resume)
        }
    }

    suspend fun uploadFile(
        peerId: Int,
        file: File,
        name: String,
        type: FilesRepository.FileType,
    ) = suspendCoroutine { continuation ->
        launch {
            val uploadServerUrl = getFileMessageUploadServer(peerId, type)
            val uploadedFileInfo = uploadFileToServer(uploadServerUrl, file, name)
            val savedAttachmentPair = saveMessageFile(uploadedFileInfo)

            continuation.resume(savedAttachmentPair.second)
        }
    }

    private suspend fun getFileMessageUploadServer(
        peerId: Int,
        type: FilesRepository.FileType,
    ) = suspendCoroutine { continuation ->
        launch {
            val uploadServerResponse = sendRequestNotNull(
                onError = { exception ->
                    continuation.resumeWithException(exception)
                    true
                },
                request = { filesRepository.getMessagesUploadServer(peerId, type) }
            ).response.notNull()

            continuation.resume(uploadServerResponse.uploadUrl)
        }
    }

    private suspend fun uploadFileToServer(
        uploadUrl: String,
        file: File,
        name: String,
    ) = suspendCoroutine { continuation ->
        launch {
            val requestBody = file.asRequestBody()
            val body = MultipartBody.Part.createFormData("file", name, requestBody)

            sendRequestNotNull(
                onError = { exception ->
                    continuation.resumeWithException(exception)
                    true
                },
                request = { filesRepository.uploadFile(uploadUrl, body) }
            ).let { response ->
                response.error?.let { error -> throw ApiError(error = error) }

                continuation.resume(response.file.notNull())
            }
        }
    }

    private suspend fun saveMessageFile(file: String) =
        suspendCoroutine { continuation ->
            launch {
                sendRequestNotNull(
                    onError = { exception ->
                        continuation.resumeWithException(exception)
                        true
                    },
                    request = { filesRepository.saveMessageFile(file) }
                ).response?.let { response ->
                    val type = response.type
                    val attachmentFile =
                        response.file?.asVkFile() ?: response.voiceMessage?.asVkVoiceMessage()

                    continuation.resume(type to attachmentFile.notNull())
                }
            }
        }
}

data class MessagesLoadedEvent(
    val count: Int,
    val conversations: HashMap<Int, VkConversationDomain>,
    val messages: List<VkMessage>,
    val profiles: HashMap<Int, VkUser>,
    val groups: HashMap<Int, VkGroup>,
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
    val messageId: Int,
) : VkEvent()

data class MessagesNewEvent(
    val message: VkMessage,
    val profiles: HashMap<Int, VkUser>,
    val groups: HashMap<Int, VkGroup>,
) : VkEvent()
