package com.meloda.fast.screens.messages

import androidx.lifecycle.viewModelScope
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
import com.meloda.fast.api.network.audio.AudiosDataSource
import com.meloda.fast.api.network.files.FilesDataSource
import com.meloda.fast.api.network.messages.*
import com.meloda.fast.api.network.photos.PhotosDataSource
import com.meloda.fast.api.network.photos.PhotosSaveMessagePhotoRequest
import com.meloda.fast.api.network.videos.VideosDataSource
import com.meloda.fast.base.viewmodel.BaseViewModel
import com.meloda.fast.base.viewmodel.VkEvent
import com.meloda.fast.screens.conversations.MessagesNewEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject


@HiltViewModel
class MessagesHistoryViewModel @Inject constructor(
    private val messages: MessagesDataSource,
    updatesParser: LongPollUpdatesParser,
    private val photos: PhotosDataSource,
    private val files: FilesDataSource,
    private val audios: AudiosDataSource,
    private val videos: VideosDataSource
) : BaseViewModel() {

    init {
        updatesParser.onNewMessage {
            viewModelScope.launch { handleNewMessage(it) }
        }

        updatesParser.onMessageEdited {
            viewModelScope.launch { handleEditedMessage(it) }
        }

        updatesParser.onMessageIncomingRead {
            viewModelScope.launch { handleReadIncomingEvent(it) }
        }

        updatesParser.onMessageOutgoingRead {
            viewModelScope.launch { handleReadOutgoingEvent(it) }
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

    fun loadHistory(peerId: Int) = viewModelScope.launch {
        makeJob({
            messages.getHistory(
                MessagesGetHistoryRequest(
                    count = 30,
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

                messages.store(hashMessages.values.toList())

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
        attachments: List<VkAttachment>? = null
    ) = viewModelScope.launch {
        makeJob(
            {
                messages.send(
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
            })
    }

    fun markAsImportant(
        messagesIds: List<Int>,
        important: Boolean
    ) = viewModelScope.launch {
        makeJob({
            messages.markAsImportant(
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
    ) = viewModelScope.launch {
        if (pin) {
            makeJob({
                messages.pin(
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
            makeJob({ messages.unpin(MessagesUnPinMessageRequest(peerId = peerId)) },
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
    ) = viewModelScope.launch {
        makeJob(
            {
                messages.delete(
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
    ) = viewModelScope.launch {
        makeJob(
            {
                messages.edit(
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

    fun getPhotoMessageUploadServer(peerId: Int, photo: File, name: String) {
        makeJob(
            { photos.getMessagesUploadServer(peerId) },
            onAnswer = {
                val response = it.response ?: return@makeJob
                val url = response.uploadUrl

                uploadPhotoToServer(peerId, url, photo, name)
            }
        )
    }

    fun uploadPhotoToServer(peerId: Int, uploadUrl: String, photo: File, name: String) {
        val requestBody = photo.asRequestBody("image/*".toMediaType())
        val body = MultipartBody.Part.createFormData("photo", name, requestBody)
        makeJob(
            { photos.uploadPhoto(uploadUrl, body) },
            onAnswer = {
                val response = it

                saveMessagePhoto(peerId, response.server, response.photo, response.hash)
            },
            onError = {
                val error = it
            }
        )
    }

    fun saveMessagePhoto(peerId: Int, server: Int, photo: String, hash: String) {
        makeJob(
            { photos.saveMessagePhoto(PhotosSaveMessagePhotoRequest(photo, server, hash)) },
            onAnswer = {
                val response = it.response ?: return@makeJob
                val photos = response

                sendMessage(peerId, attachments = listOf(photos.first().asVkPhoto()))
            },
            onError = {
                val error = it
            }
        )
    }

    fun getVideoMessageUploadServer(
        peerId: Int,
        file: File,
        name: String,
    ) {
        makeJob(
            { videos.save() },
            onAnswer = {
                val response = it.response ?: return@makeJob
                val url = response.uploadUrl

                val video = VkVideo(
                    id = response.videoId,
                    ownerId = response.ownerId,
                    emptyList(),
                    null,
                    response.accessKey
                )

                uploadVideoToServer(peerId, url, file, name, video)
            }
        )
    }

    fun uploadVideoToServer(
        peerId: Int,
        uploadUrl: String,
        file: File,
        name: String,
        video: VkVideo
    ) {
        val requestBody = file.asRequestBody()
        val body = MultipartBody.Part.createFormData("video_file", name, requestBody)
        makeJob(
            { videos.upload(uploadUrl, body) },
            onAnswer = {
                val response = it

                saveMessageVideo(peerId, video)
            },
            onError = {
                val error = it
            }
        )
    }

    fun saveMessageVideo(peerId: Int, video: VkVideo) {
        sendMessage(peerId, attachments = listOf(video))
    }

    fun getAudioUploadServer(peerId: Int, file: File, name: String) {
        makeJob(
            { audios.getUploadServer() },
            onAnswer = {
                val response = it.response ?: return@makeJob
                val url = response.uploadUrl

                uploadAudioToServer(peerId, url, file, name)
            }
        )
    }

    fun uploadAudioToServer(
        peerId: Int,
        uploadUrl: String,
        file: File,
        name: String
    ) {
        val requestBody = file.asRequestBody()
        val body = MultipartBody.Part.createFormData("file", name, requestBody)
        makeJob(
            { audios.upload(uploadUrl, body) },
            onAnswer = {
                val response = it

                if (response.audio != null) {
                    saveMessageAudio(peerId, response.server, response.audio, response.hash)
                } else {
                    onError(ApiError(0, response.error.orEmpty()))
                }
            },
            onError = {
                val error = it
            }
        )
    }

    fun saveMessageAudio(peerId: Int, server: Int, audio: String, hash: String) {
        makeJob(
            { audios.save(server, audio, hash) },
            onAnswer = {
                val response = it.response ?: return@makeJob

                sendMessage(peerId, attachments = listOf(response.asVkAudio()))
            },
            onError = {
                val error = it
            }
        )
    }

    fun getFileMessageUploadServer(
        peerId: Int,
        file: File,
        name: String,
        mimeType: String,
        type: FilesDataSource.FileType
    ) {
        makeJob(
            { files.getMessagesUploadServer(peerId, type) },
            onAnswer = {
                val response = it.response ?: return@makeJob
                val url = response.uploadUrl

                uploadFileToServer(peerId, url, file, mimeType, name)
            }
        )
    }

    fun uploadFileToServer(
        peerId: Int,
        uploadUrl: String,
        file: File,
        mimeType: String,
        name: String
    ) {
        val requestBody = file.asRequestBody()
        val body = MultipartBody.Part.createFormData("file", name, requestBody)
        makeJob(
            { files.uploadFile(uploadUrl, body) },
            onAnswer = {
                val response = it

                if (response.file != null) {
                    saveMessageFile(peerId, response.file)
                } else {
                    onError(ApiError(0, response.error.orEmpty()))
                }
            },
            onError = {
                val error = it
            }
        )
    }

    fun saveMessageFile(peerId: Int, file: String) {
        makeJob(
            { files.saveMessageFile(file) },
            onAnswer = {
                val response = it.response ?: return@makeJob

                val attachment: VkAttachment =
                    (response.file?.asVkFile()) ?: response.voiceMessage?.asVkVoiceMessage()
                    ?: return@makeJob

                sendMessage(peerId, attachments = listOf(attachment))
            },
            onError = {
                val error = it
            }
        )
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