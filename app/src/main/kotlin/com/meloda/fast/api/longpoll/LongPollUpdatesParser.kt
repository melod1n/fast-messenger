package com.meloda.fast.api.longpoll

import android.util.Log
import com.google.gson.JsonArray
import com.meloda.fast.api.ApiEvent
import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.network.Answer
import com.meloda.fast.api.network.messages.MessagesDataSource
import com.meloda.fast.api.network.messages.MessagesGetByIdRequest
import com.meloda.fast.base.viewmodel.VkEventCallback
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Suppress("UNCHECKED_CAST", "MemberVisibilityCanBePrivate")
class LongPollUpdatesParser(private val messagesDataSource: MessagesDataSource) : CoroutineScope {

    private val job = SupervisorJob()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.d("LongPollUpdatesParser", "error: $throwable")
        throwable.printStackTrace()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job + exceptionHandler

    private val listenersMap: MutableMap<ApiEvent, MutableCollection<VkEventCallback<*>>> =
        mutableMapOf()

    fun parseNextUpdate(event: JsonArray) {
        val eventId = event[0].asInt
        val eventType: ApiEvent? = ApiEvent.parse(eventId)

        if (eventType == null) {
            Log.d("LongPollUpdatesParser", "parseNextUpdate: unknownEvent: $event")
            return
        }

        when (eventType) {
            ApiEvent.MessageSetFlags -> parseMessageSetFlags(eventType, event)
            ApiEvent.MessageClearFlags -> parseMessageClearFlags(eventType, event)
            ApiEvent.MessageNew -> parseMessageNew(eventType, event)
            ApiEvent.MessageEdit -> parseMessageEdit(eventType, event)
            ApiEvent.MessageReadIncoming -> parseMessageReadIncoming(eventType, event)
            ApiEvent.MessageReadOutgoing -> parseMessageReadOutgoing(eventType, event)
            ApiEvent.FriendOnline -> parseFriendOnline(eventType, event)
            ApiEvent.FriendOffline -> parseFriendOffline(eventType, event)
            ApiEvent.MessagesDeleted -> parseMessagesDeleted(eventType, event)
            ApiEvent.PinUnpinConversation -> onNewEvent(eventType, event)
            ApiEvent.PrivateTyping -> onNewEvent(eventType, event)
            ApiEvent.ChatTyping -> onNewEvent(eventType, event)
            ApiEvent.OneMoreTyping -> onNewEvent(eventType, event)
            ApiEvent.VoiceRecording -> onNewEvent(eventType, event)
            ApiEvent.PhotoUploading -> onNewEvent(eventType, event)
            ApiEvent.VideoUploading -> onNewEvent(eventType, event)
            ApiEvent.FileUploading -> onNewEvent(eventType, event)
            ApiEvent.UnreadCountUpdate -> onNewEvent(eventType, event)
        }

    }

    private fun onNewEvent(eventType: ApiEvent, event: JsonArray) {
        Log.d("LongPollUpdatesParser", "newEvent: $eventType: $event")
    }

    private fun parseMessageSetFlags(eventType: ApiEvent, event: JsonArray) {
        Log.d("LongPollUpdatesParser", "$eventType: $event")
    }

    private fun parseMessageClearFlags(eventType: ApiEvent, event: JsonArray) {
        Log.d("LongPollUpdatesParser", "$eventType: $event")
    }

    private fun parseMessageNew(eventType: ApiEvent, event: JsonArray) {
        Log.d("LongPollUpdatesParser", "$eventType: $event")
        val messageId = event[1].asInt

        launch {
            val newMessageEvent: LongPollEvent.VkMessageNewEvent =
                loadNormalMessage(
                    eventType,
                    messageId
                )

            listenersMap[ApiEvent.MessageNew]?.let {
                it.map { vkEventCallback ->
                    (vkEventCallback as VkEventCallback<LongPollEvent.VkMessageNewEvent>)
                        .onEvent(newMessageEvent)
                }
            }
        }
    }

    private fun parseMessageEdit(eventType: ApiEvent, event: JsonArray) {
        Log.d("LongPollUpdatesParser", "$eventType: $event")
        val messageId = event[1].asInt

        launch {
            val editedMessageEvent: LongPollEvent.VkMessageEditEvent =
                loadNormalMessage(
                    eventType,
                    messageId
                )

            listenersMap[ApiEvent.MessageEdit]?.let {
                it.map { vkEventCallback ->
                    (vkEventCallback as VkEventCallback<LongPollEvent.VkMessageEditEvent>)
                        .onEvent(editedMessageEvent)
                }
            }
        }
    }

    private fun parseMessageReadIncoming(eventType: ApiEvent, event: JsonArray) {
        Log.d("LongPollUpdatesParser", "$eventType: $event")
        val peerId = event[1].asInt
        val messageId = event[2].asInt

        launch {
            listenersMap[ApiEvent.MessageReadIncoming]?.let { listeners ->
                listeners.map { vkEventCallback ->
                    (vkEventCallback as VkEventCallback<LongPollEvent.VkMessageReadIncomingEvent>)
                        .onEvent(
                            LongPollEvent.VkMessageReadIncomingEvent(
                                peerId = peerId,
                                messageId = messageId
                            )
                        )
                }
            }
        }
    }

    private fun parseMessageReadOutgoing(eventType: ApiEvent, event: JsonArray) {
        Log.d("LongPollUpdatesParser", "$eventType: $event")
        val peerId = event[1].asInt
        val messageId = event[2].asInt

        launch {
            listenersMap[ApiEvent.MessageReadOutgoing]?.let { listeners ->
                listeners.map { vkEventCallback ->
                    (vkEventCallback as VkEventCallback<LongPollEvent.VkMessageReadOutgoingEvent>)
                        .onEvent(
                            LongPollEvent.VkMessageReadOutgoingEvent(
                                peerId = peerId,
                                messageId = messageId
                            )
                        )
                }
            }
        }
    }

    private fun parseFriendOnline(eventType: ApiEvent, event: JsonArray) {
        Log.d("LongPollUpdatesParser", "$eventType: $event")
    }

    private fun parseFriendOffline(eventType: ApiEvent, event: JsonArray) {
        Log.d("LongPollUpdatesParser", "$eventType: $event")
    }

    private fun parseMessagesDeleted(eventType: ApiEvent, event: JsonArray) {
        Log.d("LongPollUpdatesParser", "$eventType: $event")
    }

    private suspend fun <T : LongPollEvent> loadNormalMessage(eventType: ApiEvent, messageId: Int) =
        coroutineScope {
            suspendCoroutine<T> {
                launch {
                    val normalMessageResponse = messagesDataSource.getById(
                        MessagesGetByIdRequest(
                            messagesIds = listOf(messageId),
                            extended = true,
                            fields = VKConstants.ALL_FIELDS
                        )
                    )

                    if (normalMessageResponse !is Answer.Success) {
                        (normalMessageResponse as Answer.Error).throwable.let { throw it }
                    }

                    val messagesResponse = normalMessageResponse.data.response ?: return@launch

                    val messagesList = messagesResponse.items
                    if (messagesList.isEmpty()) return@launch

                    val normalMessage = messagesList[0].asVkMessage()
                    messagesDataSource.store(listOf(normalMessage))

                    val profiles = hashMapOf<Int, VkUser>()
                    messagesResponse.profiles?.forEach { baseUser ->
                        baseUser.asVkUser().let { user -> profiles[user.id] = user }
                    }

                    val groups = hashMapOf<Int, VkGroup>()
                    messagesResponse.groups?.forEach { baseGroup ->
                        baseGroup.asVkGroup().let { group -> groups[group.id] = group }
                    }

                    val resumeValue: LongPollEvent? = when (eventType) {
                        ApiEvent.MessageNew ->
                            LongPollEvent.VkMessageNewEvent(
                                normalMessage,
                                profiles,
                                groups
                            )
                        ApiEvent.MessageEdit -> LongPollEvent.VkMessageEditEvent(normalMessage)
                        else -> null
                    }

                    resumeValue?.let { value -> it.resume(value as T) }
                }
            }
        }


    private fun <T : Any> registerListener(eventType: ApiEvent, listener: VkEventCallback<T>) {
        listenersMap.let { map ->
            map[eventType] = (map[eventType] ?: mutableListOf()).also {
                it.add(listener)
            }
        }
    }

    fun onMessageIncomingRead(listener: VkEventCallback<LongPollEvent.VkMessageReadIncomingEvent>) {
        registerListener(ApiEvent.MessageReadIncoming, listener)
    }

    fun onMessageIncomingRead(block: (LongPollEvent.VkMessageReadIncomingEvent) -> Unit) {
        onMessageIncomingRead(assembleEventCallback(block))
    }

    fun onMessageOutgoingRead(listener: VkEventCallback<LongPollEvent.VkMessageReadOutgoingEvent>) {
        registerListener(ApiEvent.MessageReadOutgoing, listener)
    }

    fun onMessageOutgoingRead(block: (LongPollEvent.VkMessageReadOutgoingEvent) -> Unit) {
        onMessageOutgoingRead(assembleEventCallback(block))
    }

    fun onNewMessage(listener: VkEventCallback<LongPollEvent.VkMessageNewEvent>) {
        registerListener(ApiEvent.MessageNew, listener)
    }

    fun onNewMessage(block: (LongPollEvent.VkMessageNewEvent) -> Unit) {
        onNewMessage(assembleEventCallback(block))
    }

    fun onMessageEdited(listener: VkEventCallback<LongPollEvent.VkMessageEditEvent>) {
        registerListener(ApiEvent.MessageEdit, listener)
    }

    fun onMessageEdited(block: (LongPollEvent.VkMessageEditEvent) -> Unit) {
        onMessageEdited(assembleEventCallback(block))
    }

    fun clearListeners() {
        listenersMap.clear()
    }
}

internal inline fun <R : Any> assembleEventCallback(crossinline block: (R) -> Unit): VkEventCallback<R> {
    return object : VkEventCallback<R> {
        override fun onEvent(event: R) = block.invoke(event)
    }
}