package com.meloda.fast.screens.messages

import androidx.lifecycle.viewModelScope
import com.meloda.fast.api.longpoll.LongPollEvent
import com.meloda.fast.api.longpoll.LongPollUpdatesParser
import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.model.VkConversation
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.model.attachments.VkAttachment
import com.meloda.fast.api.network.messages.*
import com.meloda.fast.base.viewmodel.BaseViewModel
import com.meloda.fast.base.viewmodel.VkEvent
import com.meloda.fast.screens.conversations.MessagesNewEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessagesHistoryViewModel @Inject constructor(
    private val messages: MessagesDataSource,
    updatesParser: LongPollUpdatesParser
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
        setId: ((messageId: Int) -> Unit)? = null
    ) = viewModelScope.launch {
        makeJob(
            {
                messages.send(
                    MessagesSendRequest(
                        peerId = peerId,
                        randomId = randomId,
                        message = message,
                        replyTo = replyTo
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