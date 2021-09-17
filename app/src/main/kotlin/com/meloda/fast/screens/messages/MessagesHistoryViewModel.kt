package com.meloda.fast.screens.messages

import androidx.lifecycle.viewModelScope
import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.model.VkConversation
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.model.request.MessagesGetHistoryRequest
import com.meloda.fast.api.model.request.MessagesMarkAsImportantRequest
import com.meloda.fast.api.model.request.MessagesSendRequest
import com.meloda.fast.api.network.datasource.MessagesDataSource
import com.meloda.fast.base.viewmodel.BaseViewModel
import com.meloda.fast.base.viewmodel.StartProgressEvent
import com.meloda.fast.base.viewmodel.StopProgressEvent
import com.meloda.fast.base.viewmodel.VKEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessagesHistoryViewModel @Inject constructor(
    private val dataSource: MessagesDataSource
) : BaseViewModel() {

    fun loadHistory(
        peerId: Int
    ) = viewModelScope.launch {
        makeJob({
            dataSource.getHistory(
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

                val messages = hashMapOf<Int, VkMessage>()
                response.items.forEach { baseMessage ->
                    baseMessage.asVkMessage().let { message -> messages[message.id] = message }
                }

                val conversations = hashMapOf<Int, VkConversation>()
                response.conversations?.let { baseConversations ->
                    baseConversations.forEach { baseConversation ->
                        baseConversation.asVkConversation(
                            messages[baseConversation.lastMessageId]
                        ).let { conversation -> conversations[conversation.id] = conversation }
                    }
                }

                sendEvent(
                    MessagesLoaded(
                        count = response.count,
                        profiles = profiles,
                        groups = groups,
                        conversations = conversations,
                        messages = messages.values.toList()
                    )
                )
            },
            onError = {
                val throwable = it
                throw it
            },
            onStart = { sendEvent(StartProgressEvent) },
            onEnd = { sendEvent(StopProgressEvent) })
    }

    fun sendMessage(
        peerId: Int,
        message: String? = null,
        randomId: Int = 0,
        setId: ((messageId: Int) -> Unit)? = null
    ) = viewModelScope.launch {
        makeJob(
            {
                dataSource.send(
                    MessagesSendRequest(
                        peerId = peerId,
                        randomId = randomId,
                        message = message
                    )
                )
            },
            onAnswer = {
                val response = it.response ?: return@makeJob
                setId?.invoke(response)
            },
            onError = {
                val throwable = it
                val i = 0
            })
    }

    fun markAsImportant(
        messagesIds: List<Int>,
        important: Boolean
    ) = viewModelScope.launch {
        makeJob({
            dataSource.markAsImportant(
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
            },
            onError = {
                val throwable = it
                val i = 0
            })
    }

}

data class MessagesLoaded(
    val count: Int,
    val conversations: HashMap<Int, VkConversation>,
    val messages: List<VkMessage>,
    val profiles: HashMap<Int, VkUser>,
    val groups: HashMap<Int, VkGroup>
) : VKEvent()

data class MessagesMarkAsImportant(
    val messagesIds: List<Int>,
    val important: Boolean
) : VKEvent()