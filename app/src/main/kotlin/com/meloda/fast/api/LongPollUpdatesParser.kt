package com.meloda.fast.api

import android.util.Log
import com.google.gson.JsonArray
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

@Suppress("UNCHECKED_CAST")
class LongPollUpdatesParser(
    private val messagesDataSource: MessagesDataSource
) : CoroutineScope {

    companion object {
        private const val TAG = "LongPollUpdatesParser"
    }

    private val job = SupervisorJob()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.d(TAG, "error: $throwable")
        throwable.printStackTrace()
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job + exceptionHandler

    private val listenersMap: MutableMap<ApiEvent, MutableCollection<VkEventCallback<*>>> =
        mutableMapOf()

    fun parseNextUpdate(event: JsonArray) {
        val eventType: ApiEvent? =
            try {
                ApiEvent.parse(event[0].asInt)
            } catch (e: Exception) {
                null
            }

        if (eventType != null) {
            println("$TAG: $eventType: $event")
        } else {
            println("$TAG: unknown event: $event")
        }

        when (eventType) {
            ApiEvent.MESSAGE_SET_FLAGS -> parseMessageSetFlags(eventType, event)
            ApiEvent.MESSAGE_CLEAR_FLAGS -> parseMessageClearFlags(eventType, event)
            ApiEvent.MESSAGE_NEW -> parseMessageNew(eventType, event)
            ApiEvent.MESSAGE_EDIT -> parseMessageEdit(eventType, event)
            ApiEvent.MESSAGE_READ_INCOMING -> parseMessageReadIncoming(eventType, event)
            ApiEvent.MESSAGE_READ_OUTGOING -> parseMessageReadOutgoing(eventType, event)
            ApiEvent.FRIEND_ONLINE -> parseFriendOnline(eventType, event)
            ApiEvent.FRIEND_OFFLINE -> parseFriendOffline(eventType, event)
            ApiEvent.MESSAGES_DELETED -> parseMessagesDeleted(eventType, event)
//            ApiEvent.PIN_UNPIN_CONVERSATION -> TODO()
//            ApiEvent.TYPING -> TODO()
//            ApiEvent.VOICE_RECORDING -> TODO()
//            ApiEvent.PHOTO_UPLOADING -> TODO()
//            ApiEvent.VIDEO_UPLOADING -> TODO()
//            ApiEvent.FILE_UPLOADING -> TODO()
//            ApiEvent.UNREAD_COUNT_UPDATE -> TODO()
        }

    }

    private fun parseMessageSetFlags(eventType: ApiEvent, event: JsonArray) {
//        println("$TAG: $eventType: $event")
    }

    private fun parseMessageClearFlags(eventType: ApiEvent, event: JsonArray) {
//        println("$TAG: $eventType: $event")
    }

    private fun parseMessageNew(eventType: ApiEvent, event: JsonArray) {
//        println("$TAG: $eventType: $event")

        val messageId = event[1].asInt

        launch {
            val newMessageEvent: LongPollEvent.VkMessageNewEvent =
                loadNormalMessage(
                    eventType,
                    messageId
                )

            listenersMap[ApiEvent.MESSAGE_NEW]?.let {
                it.map { vkEventCallback ->
                    (vkEventCallback as VkEventCallback<LongPollEvent.VkMessageNewEvent>)
                        .onEvent(newMessageEvent)
                }
            }
        }
    }

    private fun parseMessageEdit(eventType: ApiEvent, event: JsonArray) {
//        println("$TAG: $eventType: $event")

        val messageId = event[1].asInt

        launch {
            val editedMessageEvent: LongPollEvent.VkMessageEditEvent =
                loadNormalMessage(
                    eventType,
                    messageId
                )

            listenersMap[ApiEvent.MESSAGE_EDIT]?.let {
                it.map { vkEventCallback ->
                    (vkEventCallback as VkEventCallback<LongPollEvent.VkMessageEditEvent>)
                        .onEvent(editedMessageEvent)
                }
            }
        }
    }

    private fun parseMessageReadIncoming(eventType: ApiEvent, event: JsonArray) {
        val peerId = event[1].asInt
        val messageId = event[2].asInt

        launch {
            listenersMap[ApiEvent.MESSAGE_READ_INCOMING]?.let { listeners ->
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
        val peerId = event[1].asInt
        val messageId = event[2].asInt

        launch {
            listenersMap[ApiEvent.MESSAGE_READ_OUTGOING]?.let { listeners ->
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
//        println("$TAG: $eventType: $event")
    }

    private fun parseFriendOffline(eventType: ApiEvent, event: JsonArray) {
//        println("$TAG: $eventType: $event")
    }

    private fun parseMessagesDeleted(eventType: ApiEvent, event: JsonArray) {
//        println("$TAG: $eventType: $event")
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
                        ApiEvent.MESSAGE_NEW ->
                            LongPollEvent.VkMessageNewEvent(
                                normalMessage,
                                profiles,
                                groups
                            )
                        ApiEvent.MESSAGE_EDIT -> LongPollEvent.VkMessageEditEvent(normalMessage)
                        else -> null
                    }

                    resumeValue?.let { value -> it.resume(value as T) }
                }
            }
        }


    fun <T : Any> registerListener(eventType: ApiEvent, listener: VkEventCallback<T>) {
        listenersMap.let { map ->
            map[eventType] = (map[eventType] ?: mutableListOf()).also {
                it.add(listener)
            }
        }
    }

    fun onMessageIncomingRead(listener: VkEventCallback<LongPollEvent.VkMessageReadIncomingEvent>) {
        registerListener(ApiEvent.MESSAGE_READ_INCOMING, listener)
    }

    fun onMessageIncomingRead(block: (LongPollEvent.VkMessageReadIncomingEvent) -> Unit) {
        onMessageIncomingRead(assembleEventCallback(block))
    }

    fun onMessageOutgoingRead(listener: VkEventCallback<LongPollEvent.VkMessageReadOutgoingEvent>) {
        registerListener(ApiEvent.MESSAGE_READ_OUTGOING, listener)
    }

    fun onMessageOutgoingRead(block: (LongPollEvent.VkMessageReadOutgoingEvent) -> Unit) {
        onMessageOutgoingRead(assembleEventCallback(block))
    }

    fun onNewMessage(listener: VkEventCallback<LongPollEvent.VkMessageNewEvent>) {
        registerListener(ApiEvent.MESSAGE_NEW, listener)
    }

    fun onNewMessage(block: (LongPollEvent.VkMessageNewEvent) -> Unit) {
        onNewMessage(assembleEventCallback(block))
    }

    fun onMessageEdited(listener: VkEventCallback<LongPollEvent.VkMessageEditEvent>) {
        registerListener(ApiEvent.MESSAGE_EDIT, listener)
    }

    fun onMessageEdited(block: (LongPollEvent.VkMessageEditEvent) -> Unit) {
        onMessageEdited(assembleEventCallback(block))
    }

    fun clearListeners() {
        listenersMap.clear()
    }
}

internal inline fun <R : Any> assembleEventCallback(
    crossinline block: (R) -> Unit
): VkEventCallback<R> {
    return object : VkEventCallback<R> {
        override fun onEvent(event: R) = block.invoke(event)
    }
}