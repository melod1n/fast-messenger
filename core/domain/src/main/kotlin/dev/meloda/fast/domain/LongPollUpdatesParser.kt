package dev.meloda.fast.domain

import android.util.Log
import dev.meloda.fast.common.VkConstants
import dev.meloda.fast.common.extensions.asInt
import dev.meloda.fast.common.extensions.listenValue
import dev.meloda.fast.common.extensions.toList
import dev.meloda.fast.data.UserConfig
import dev.meloda.fast.data.VkMemoryCache
import dev.meloda.fast.data.processState
import dev.meloda.fast.model.ApiEvent
import dev.meloda.fast.model.InteractionType
import dev.meloda.fast.model.LongPollEvent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LongPollUpdatesParser(
    private val messagesUseCase: MessagesUseCase
) {
    private val job = SupervisorJob()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.d("LongPollUpdatesParser", "error: $throwable")
        throwable.printStackTrace()
    }

    private val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job + exceptionHandler

    private val coroutineScope = CoroutineScope(coroutineContext)

    private val listenersMap: MutableMap<ApiEvent, MutableCollection<VkEventCallback<*>>> =
        mutableMapOf()

    fun parseNextUpdate(event: List<Any>) {
        val eventId = event.first().asInt()

        when (val eventType =  ApiEvent.parseOrNull(eventId)) {
            null -> Log.d("LongPollUpdatesParser", "parseNextUpdate: unknownEvent: $event")

            ApiEvent.MESSAGE_SET_FLAGS -> parseMessageSetFlags(eventType, event)
            ApiEvent.MESSAGE_CLEAR_FLAGS -> parseMessageClearFlags(eventType, event)
            ApiEvent.MESSAGE_NEW -> parseMessageNew(eventType, event)
            ApiEvent.MESSAGE_EDIT -> parseMessageEdit(eventType, event)
            ApiEvent.MESSAGE_READ_INCOMING -> parseMessageReadIncoming(eventType, event)
            ApiEvent.MESSAGE_READ_OUTGOING -> parseMessageReadOutgoing(eventType, event)
            ApiEvent.MESSAGES_DELETED -> parseMessagesDeleted(eventType, event)
            ApiEvent.PIN_UNPIN_CONVERSATION -> parseConversationPinStateChanged(eventType, event)

            ApiEvent.TYPING,
            ApiEvent.AUDIO_MESSAGE_RECORDING,
            ApiEvent.PHOTO_UPLOADING,
            ApiEvent.VIDEO_UPLOADING,
            ApiEvent.FILE_UPLOADING -> parseInteraction(eventType, event)

            ApiEvent.UNREAD_COUNT_UPDATE -> onNewEvent(eventType, event)
        }
    }

    private fun onNewEvent(eventType: ApiEvent, event: List<Any>) {
        Log.d("LongPollUpdatesParser", "newEvent: $eventType: $event")
    }

    private fun parseInteraction(eventType: ApiEvent, event: List<Any>) {
        Log.d("LongPollUpdatesParser", "$eventType: $event")

        val interactionType = when (eventType) {
            ApiEvent.TYPING -> InteractionType.Typing
            ApiEvent.AUDIO_MESSAGE_RECORDING -> InteractionType.VoiceMessage
            ApiEvent.PHOTO_UPLOADING -> InteractionType.Photo
            ApiEvent.VIDEO_UPLOADING -> InteractionType.Video
            ApiEvent.FILE_UPLOADING -> InteractionType.File
            else -> return
        }

        val peerId = event[1].asInt()
        val userIds = event[2].toList(Any::asInt).filter { it != UserConfig.userId }
        val totalCount = event[3].asInt()
        val timestamp = event[4].asInt()

        // if userIds contains only account's id, then we don't need to show our status
        if (userIds.isEmpty()) return

        coroutineScope.launch {
            listenersMap[eventType]?.let { listeners ->
                listeners.forEach { vkEventCallback ->
                    (vkEventCallback as VkEventCallback<LongPollEvent.Interaction>)
                        .onEvent(
                            LongPollEvent.Interaction(
                                interactionType = interactionType,
                                peerId = peerId,
                                userIds = userIds,
                                totalCount = totalCount,
                                timestamp = timestamp
                            )
                        )
                }
            }
        }
    }

    private fun parseConversationPinStateChanged(eventType: ApiEvent, event: List<Any>) {
        Log.d("LongPollUpdatesParser", "$eventType: $event")

        val peerId = event[1].asInt()
        val majorId = event[2].asInt()

        coroutineScope.launch {
            listenersMap[ApiEvent.PIN_UNPIN_CONVERSATION]?.let { listeners ->
                listeners.forEach { vkEventCallback ->
                    (vkEventCallback as VkEventCallback<LongPollEvent.VkConversationPinStateChangedEvent>)
                        .onEvent(
                            LongPollEvent.VkConversationPinStateChangedEvent(
                                peerId = peerId,
                                majorId = majorId
                            )
                        )
                }
            }
        }
    }

    private fun parseMessageSetFlags(eventType: ApiEvent, event: List<Any>) {
        Log.d("LongPollUpdatesParser", "$eventType: $event")
    }

    private fun parseMessageClearFlags(eventType: ApiEvent, event: List<Any>) {
        Log.d("LongPollUpdatesParser", "$eventType: $event")
    }

    private fun parseMessageNew(eventType: ApiEvent, event: List<Any>) {
        Log.d("LongPollUpdatesParser", "$eventType: $event")
        val messageId = event[1].asInt()

        coroutineScope.launch(Dispatchers.IO) {
            val newMessageEvent: LongPollEvent.VkMessageNewEvent? =
                loadNormalMessage(
                    eventType,
                    messageId
                )

            newMessageEvent?.let { event ->
                listenersMap[ApiEvent.MESSAGE_NEW]?.let {
                    it.map { vkEventCallback ->
                        (vkEventCallback as VkEventCallback<LongPollEvent.VkMessageNewEvent>)
                            .onEvent(event)
                    }
                }
            }
        }
    }

    private fun parseMessageEdit(eventType: ApiEvent, event: List<Any>) {
        Log.d("LongPollUpdatesParser", "$eventType: $event")
        val messageId = event[1].asInt()

        coroutineScope.launch {
            val editedMessageEvent: LongPollEvent.VkMessageEditEvent? =
                loadNormalMessage(
                    eventType,
                    messageId
                )

            editedMessageEvent?.let { event ->
                listenersMap[ApiEvent.MESSAGE_EDIT]?.let {
                    it.map { vkEventCallback ->
                        (vkEventCallback as VkEventCallback<LongPollEvent.VkMessageEditEvent>)
                            .onEvent(event)
                    }
                }
            }
        }
    }

    private fun parseMessageReadIncoming(eventType: ApiEvent, event: List<Any>) {
        Log.d("LongPollUpdatesParser", "$eventType: $event")
        val peerId = event[1].asInt()
        val messageId = event[2].asInt()
        val unreadCount = event[3].asInt()

        coroutineScope.launch {
            listenersMap[ApiEvent.MESSAGE_READ_INCOMING]?.let { listeners ->
                listeners.map { vkEventCallback ->
                    (vkEventCallback as VkEventCallback<LongPollEvent.VkMessageReadIncomingEvent>)
                        .onEvent(
                            LongPollEvent.VkMessageReadIncomingEvent(
                                peerId = peerId,
                                messageId = messageId,
                                unreadCount = unreadCount
                            )
                        )
                }
            }
        }
    }

    private fun parseMessageReadOutgoing(eventType: ApiEvent, event: List<Any>) {
        Log.d("LongPollUpdatesParser", "$eventType: $event")
        val peerId = event[1].asInt()
        val messageId = event[2].asInt()
        val unreadCount = event[3].asInt()

        coroutineScope.launch {
            listenersMap[ApiEvent.MESSAGE_READ_OUTGOING]?.let { listeners ->
                listeners.map { vkEventCallback ->
                    (vkEventCallback as VkEventCallback<LongPollEvent.VkMessageReadOutgoingEvent>)
                        .onEvent(
                            LongPollEvent.VkMessageReadOutgoingEvent(
                                peerId = peerId,
                                messageId = messageId,
                                unreadCount = unreadCount
                            )
                        )
                }
            }
        }
    }

    private fun parseMessagesDeleted(eventType: ApiEvent, event: List<Any>) {
        Log.d("LongPollUpdatesParser", "$eventType: $event")
    }

    private suspend inline fun <reified T : LongPollEvent> loadNormalMessage(
        eventType: ApiEvent,
        messageId: Int
    ): T? = suspendCoroutine { continuation ->
        coroutineScope.launch(Dispatchers.IO) {
            messagesUseCase.getById(
                messageIds = listOf(messageId),
                extended = true,
                fields = VkConstants.ALL_FIELDS
            ).listenValue(this) { state ->
                state.processState(
                    error = { error ->
                        Log.e("LongPollUpdatesParser", "loadNormalMessage: error: $error")
                    },
                    success = { messages ->
                        val message = messages.singleOrNull() ?: run {
                            continuation.resume(null)
                            return@listenValue
                        }

                        VkMemoryCache[message.id] = message
                        messagesUseCase.storeMessage(message)

                        val resumeValue: LongPollEvent? = when (eventType) {
                            ApiEvent.MESSAGE_NEW -> LongPollEvent.VkMessageNewEvent(message)
                            ApiEvent.MESSAGE_EDIT -> LongPollEvent.VkMessageEditEvent(message)

                            else -> {
                                continuation.resume(null)
                                null
                            }
                        }

                        resumeValue?.let { value -> continuation.resume(value as T) }
                    }
                )
            }
        }
    }

    private fun <T : Any> registerListener(
        eventType: ApiEvent,
        listener: VkEventCallback<T>
    ) {
        listenersMap.let { map ->
            map[eventType] = (map[eventType] ?: mutableListOf()).also { it.add(listener) }
        }
    }

    private fun <T : Any> registerListeners(
        eventTypes: List<ApiEvent>,
        listener: VkEventCallback<T>
    ) {
        eventTypes.forEach { eventType -> registerListener(eventType, listener) }
    }

    fun onConversationPinStateChanged(listener: VkEventCallback<LongPollEvent.VkConversationPinStateChangedEvent>) {
        registerListener(ApiEvent.PIN_UNPIN_CONVERSATION, listener)
    }

    fun onConversationPinStateChanged(block: (LongPollEvent.VkConversationPinStateChangedEvent) -> Unit) {
        onConversationPinStateChanged(assembleEventCallback(block))
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

    fun onInteractions(listener: VkEventCallback<LongPollEvent.Interaction>) {
        registerListeners(
            eventTypes = listOf(
                ApiEvent.TYPING,
                ApiEvent.AUDIO_MESSAGE_RECORDING,
                ApiEvent.PHOTO_UPLOADING,
                ApiEvent.VIDEO_UPLOADING,
                ApiEvent.FILE_UPLOADING
            ),
            listener = listener
        )
    }

    fun onInteractions(block: (LongPollEvent.Interaction) -> Unit) {
        onInteractions(assembleEventCallback(block))
    }

    fun clearListeners() {
        listenersMap.clear()
    }
}

internal inline fun <R : Any> assembleEventCallback(
    crossinline block: (R) -> Unit,
): VkEventCallback<R> {
    return VkEventCallback { event -> block.invoke(event) }
}

fun interface VkEventCallback<in T : Any> {
    fun onEvent(event: T)
}
