package com.meloda.fast.screens.messages

import androidx.lifecycle.viewModelScope
import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.model.VkConversation
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.model.request.*
import com.meloda.fast.api.network.datasource.MessagesDataSource
import com.meloda.fast.base.viewmodel.BaseViewModel
import com.meloda.fast.base.viewmodel.VkEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessagesHistoryViewModel @Inject constructor(
    private val messages: MessagesDataSource
) : BaseViewModel() {

    fun loadHistory(
        peerId: Int
    ) = viewModelScope.launch {
        makeJob({
            messages.getHistory(
                MessagesGetHistoryRequest(
                    count = 30,
                    peerId = peerId,
                    extended = true,
                    fields = "${VKConstants.USER_FIELDS},${VKConstants.GROUP_FIELDS}"
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
                    baseMessage.asVkMessage().let { message -> hashMessages[message.id] = message }
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
                    MessagesLoaded(
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
                    MessagesMarkAsImportant(
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
                    sendEvent(MessagesPin(response.asVkMessage()))
                }
            )
        } else {
            makeJob({ messages.unpin(MessagesUnPinMessageRequest(peerId = peerId)) },
                onAnswer = {
                    println("Fast::MessagesHistoryViewModel::unPin::Response::${it.response}")
                    sendEvent(MessagesUnpin)
                }
            )
        }
    }
}

data class MessagesLoaded(
    val count: Int,
    val conversations: HashMap<Int, VkConversation>,
    val messages: List<VkMessage>,
    val profiles: HashMap<Int, VkUser>,
    val groups: HashMap<Int, VkGroup>
) : VkEvent()

data class MessagesMarkAsImportant(
    val messagesIds: List<Int>,
    val important: Boolean
) : VkEvent()

data class MessagesPin(
    val message: VkMessage
) : VkEvent()

object MessagesUnpin : VkEvent()

