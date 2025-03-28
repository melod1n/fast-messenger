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
import dev.meloda.fast.model.LongPollParsedEvent
import dev.meloda.fast.model.MessageFlags
import dev.meloda.fast.model.api.domain.VkMessage
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LongPollUpdatesParser(
    private val messagesUseCase: MessagesUseCase
) {
    private val job = SupervisorJob()

    private val exceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            Log.e("LongPollUpdatesParser", "error: $throwable")
            throwable.printStackTrace()
        }

    private val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job + exceptionHandler

    private val coroutineScope = CoroutineScope(coroutineContext)

    private val listenersMap: MutableMap<LongPollEvent, MutableList<VkEventCallback<LongPollParsedEvent>>> =
        mutableMapOf()

    fun parseNextUpdate(event: List<Any>) {
        val eventId = event.first().asInt()

        when (val eventType = ApiEvent.parseOrNull(eventId)) {
            null -> Log.d("LongPollUpdatesParser", "parseNextUpdate: unknownEvent: $event")

            ApiEvent.MESSAGE_SET_FLAGS -> parseMessageSetFlags(eventType, event)
            ApiEvent.MESSAGE_CLEAR_FLAGS -> parseMessageClearFlags(eventType, event)
            ApiEvent.MESSAGE_NEW -> parseMessageNew(eventType, event)
            ApiEvent.MESSAGE_EDIT -> parseMessageEdit(eventType, event)
            ApiEvent.MESSAGE_READ_INCOMING -> parseMessageReadIncoming(eventType, event)
            ApiEvent.MESSAGE_READ_OUTGOING -> parseMessageReadOutgoing(eventType, event)
            ApiEvent.CHAT_CLEAR_FLAGS -> parseChatClearFlags(eventType, event)
            ApiEvent.CHAT_SET_FLAGS -> parseChatSetFlags(eventType, event)
            ApiEvent.MESSAGES_DELETED -> parseMessagesDeleted(eventType, event)
            ApiEvent.CHAT_MAJOR_CHANGED -> parseChatMajorChanged(eventType, event)
            ApiEvent.CHAT_MINOR_CHANGED -> parseChatMinorChanged(eventType, event)

            ApiEvent.TYPING,
            ApiEvent.AUDIO_MESSAGE_RECORDING,
            ApiEvent.PHOTO_UPLOADING,
            ApiEvent.VIDEO_UPLOADING,
            ApiEvent.FILE_UPLOADING -> parseInteraction(eventType, event)

            ApiEvent.UNREAD_COUNT_UPDATE -> parseUnreadCounterUpdate(eventType, event)
        }
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

        val longPollEvent: LongPollEvent = when (eventType) {
            ApiEvent.TYPING -> LongPollEvent.TYPING
            ApiEvent.AUDIO_MESSAGE_RECORDING -> LongPollEvent.AUDIO_MESSAGE_RECORDING
            ApiEvent.PHOTO_UPLOADING -> LongPollEvent.PHOTO_UPLOADING
            ApiEvent.VIDEO_UPLOADING -> LongPollEvent.VIDEO_UPLOADING
            ApiEvent.FILE_UPLOADING -> LongPollEvent.FILE_UPLOADING
            else -> return
        }

        val peerId = event[1].asInt()
        val userIds = event[2].toList(Any::asInt).filter { it != UserConfig.userId }
        val totalCount = event[3].asInt()
        val timestamp = event[4].asInt()

        // if userIds contains only account's id, then we don't need to show our status
        if (userIds.isEmpty()) return

        listenersMap[longPollEvent]?.let { listeners ->
            listeners.forEach { vkEventCallback ->
                (vkEventCallback as VkEventCallback<LongPollParsedEvent.Interaction>)
                    .onEvent(
                        LongPollParsedEvent.Interaction(
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

    private fun parseUnreadCounterUpdate(eventType: ApiEvent, event: List<Any>) {
        Log.d("LongPollUpdatesParser", "$eventType $event")

        val unreadCount = event[1].asInt()
        val unreadUnmutedCount = event[2].asInt()
        val showOnlyMuted = event[3].asInt() == 1
        val businessNotifyUnreadCount = event[4].asInt()
        val archiveUnreadCount = event[7].asInt()
        val archiveUnreadUnmutedCount = event[8].asInt()
        val archiveMentionsCount = event[9].asInt()

        listenersMap[LongPollEvent.UNREAD_COUNTER_UPDATE]?.let { listeners ->
            listeners.forEach { vkEventCallback ->
                (vkEventCallback as VkEventCallback<LongPollParsedEvent.UnreadCounter>)
                    .onEvent(
                        LongPollParsedEvent.UnreadCounter(
                            unread = unreadCount,
                            unreadUnmuted = unreadUnmutedCount,
                            showOnlyMuted = showOnlyMuted,
                            business = businessNotifyUnreadCount,
                            archive = archiveUnreadCount,
                            archiveUnmuted = archiveUnreadUnmutedCount,
                            archiveMentions = archiveMentionsCount
                        )
                    )
            }
        }
    }

    private fun parseMessageSetFlags(eventType: ApiEvent, event: List<Any>) {
        Log.d("LongPollUpdatesParser", "$eventType: $event")

        val messageId = event[1].asInt()
        val flags = event[2].asInt()
        val peerId = event[3].asInt()

        val eventsToSend = mutableListOf<LongPollParsedEvent>()

        val parsedFlags = MessageFlags.parse(flags)
        parsedFlags.forEach { flag ->
            when (flag) {
                MessageFlags.IMPORTANT -> { // marked as important
                    val eventToSend = LongPollParsedEvent.MessageMarkedAsImportant(
                        peerId = peerId,
                        messageId = messageId,
                        marked = true
                    )
                    eventsToSend += eventToSend

                    listenersMap[LongPollEvent.MARKED_AS_IMPORTANT]?.let { listeners ->
                        listeners.map { vkEventCallback ->
                            (vkEventCallback as? VkEventCallback<LongPollParsedEvent.MessageMarkedAsImportant>)
                                ?.onEvent(eventToSend)
                        }
                    }
                }

                MessageFlags.SPAM -> { // marked as spam
                    val eventToSend = LongPollParsedEvent.MessageMarkedAsSpam(
                        peerId = peerId,
                        messageId = messageId
                    )
                    eventsToSend += eventToSend

                    listenersMap[LongPollEvent.MARKED_AS_SPAM]?.let { listeners ->
                        listeners.map { vkEventCallback ->
                            (vkEventCallback as? VkEventCallback<LongPollParsedEvent.MessageMarkedAsSpam>)
                                ?.onEvent(eventToSend)
                        }
                    }
                }

                MessageFlags.DELETED -> {
                    val eventToSend =
                        if (parsedFlags.contains(MessageFlags.DELETED_FOR_ALL)) { // deleted for all
                            LongPollParsedEvent.MessageDeleted(
                                peerId = peerId,
                                messageId = messageId,
                                forAll = true
                            )
                        } else { // deleted only for me
                            LongPollParsedEvent.MessageDeleted(
                                peerId = peerId,
                                messageId = messageId,
                                forAll = false
                            )
                        }
                    eventsToSend += eventToSend

                    listenersMap[LongPollEvent.MESSAGE_DELETED]?.let { listeners ->
                        listeners.map { vkEventCallback ->
                            (vkEventCallback as? VkEventCallback<LongPollParsedEvent.MessageDeleted>)
                                ?.onEvent(eventToSend)
                        }
                    }
                }

                MessageFlags.AUDIO_LISTENED -> { // audio message listened
                    val eventToSend = LongPollParsedEvent.AudioMessageListened(
                        peerId = peerId,
                        messageId = messageId
                    )
                    eventsToSend += eventToSend

                    listenersMap[LongPollEvent.AUDIO_MESSAGE_LISTENED]?.let { listeners ->
                        listeners.map { vkEventCallback ->
                            (vkEventCallback as? VkEventCallback<LongPollParsedEvent.AudioMessageListened>)
                                ?.onEvent(eventToSend)
                        }
                    }
                }

                else -> Unit
            }
        }

        eventsToSend.forEach { eventToSend ->
            listenersMap[LongPollEvent.MESSAGE_SET_FLAGS]?.let { listeners ->
                listeners.map { vkEventCallback ->
                    (vkEventCallback as? VkEventCallback<LongPollParsedEvent>)?.onEvent(eventToSend)
                }
            }
        }
    }

    private fun parseMessageClearFlags(eventType: ApiEvent, event: List<Any>) {
        Log.d("LongPollUpdatesParser", "$eventType: $event")

        val messageId = event[1].asInt()
        val flags = event[2].asInt()
        val peerId = event[3].asInt()

        val eventsToSend = mutableListOf<LongPollParsedEvent>()

        val parsedFlags = MessageFlags.parse(flags)

        coroutineScope.launch {
            parsedFlags.forEach { flag ->
                when (flag) {
                    MessageFlags.IMPORTANT -> { // not important anymore
                        val eventToSend = LongPollParsedEvent.MessageMarkedAsImportant(
                            peerId = peerId,
                            messageId = messageId,
                            marked = false
                        )
                        eventsToSend += eventToSend

                        listenersMap[LongPollEvent.MARKED_AS_IMPORTANT]?.let { listeners ->
                            listeners.map { vkEventCallback ->
                                (vkEventCallback as? VkEventCallback<LongPollParsedEvent.MessageMarkedAsImportant>)
                                    ?.onEvent(eventToSend)
                            }
                        }
                    }

                    MessageFlags.SPAM -> {
                        if (parsedFlags.contains(MessageFlags.CANCEL_SPAM)) { // not spam anymore
                            withContext(Dispatchers.IO) {
                                val message = loadMessage(messageId)
                                message?.let {
                                    val eventToSend =
                                        LongPollParsedEvent.MessageMarkedAsNotSpam(message = message)
                                    eventsToSend += eventToSend

                                    listenersMap[LongPollEvent.MARKED_AS_NOT_SPAM]?.let { listeners ->
                                        listeners.map { vkEventCallback ->
                                            (vkEventCallback as? VkEventCallback<LongPollParsedEvent.MessageMarkedAsNotSpam>)
                                                ?.onEvent(eventToSend)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    MessageFlags.DELETED -> { // restored
                        withContext(Dispatchers.IO) {
                            val message = loadMessage(messageId)
                            message?.let {
                                val eventToSend =
                                    LongPollParsedEvent.MessageRestored(message = message)
                                eventsToSend += eventToSend

                                listenersMap[LongPollEvent.MESSAGE_RESTORED]?.let { listeners ->
                                    listeners.map { vkEventCallback ->
                                        (vkEventCallback as? VkEventCallback<LongPollParsedEvent.MessageRestored>)
                                            ?.onEvent(eventToSend)
                                    }
                                }
                            }
                        }
                    }

                    else -> Unit
                }
            }

            eventsToSend.forEach { eventToSend ->
                listenersMap[LongPollEvent.MESSAGE_CLEAR_FLAGS]?.let { listeners ->
                    listeners.map { vkEventCallback ->
                        vkEventCallback.onEvent(eventToSend)
                    }
                }
            }
        }
    }

    private fun parseMessageNew(eventType: ApiEvent, event: List<Any>) {
        Log.d("LongPollUpdatesParser", "$eventType: $event")
        val messageId = event[1].asInt()

        coroutineScope.launch(Dispatchers.IO) {
            loadMessage(messageId)?.let { message ->
                listenersMap[LongPollEvent.MESSAGE_NEW]?.let {
                    it.map { vkEventCallback ->
                        (vkEventCallback as VkEventCallback<LongPollParsedEvent.NewMessage>)
                            .onEvent(LongPollParsedEvent.NewMessage(message))
                    }
                }
            }
        }
    }

    private fun parseMessageEdit(eventType: ApiEvent, event: List<Any>) {
        Log.d("LongPollUpdatesParser", "$eventType: $event")
        val messageId = event[1].asInt()

        coroutineScope.launch(Dispatchers.IO) {
            loadMessage(messageId)?.let { message ->
                listenersMap[LongPollEvent.MESSAGE_EDITED]?.let {
                    it.map { vkEventCallback ->
                        (vkEventCallback as VkEventCallback<LongPollParsedEvent.MessageEdited>)
                            .onEvent(LongPollParsedEvent.MessageEdited(message))
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

        listenersMap[LongPollEvent.INCOMING_MESSAGE_READ]?.let { listeners ->
            listeners.map { vkEventCallback ->
                (vkEventCallback as VkEventCallback<LongPollParsedEvent.IncomingMessageRead>)
                    .onEvent(
                        LongPollParsedEvent.IncomingMessageRead(
                            peerId = peerId,
                            messageId = messageId,
                            unreadCount = unreadCount
                        )
                    )
            }
        }
    }

    private fun parseMessageReadOutgoing(eventType: ApiEvent, event: List<Any>) {
        Log.d("LongPollUpdatesParser", "$eventType: $event")
        val peerId = event[1].asInt()
        val messageId = event[2].asInt()
        val unreadCount = event[3].asInt()

        listenersMap[LongPollEvent.OUTGOING_MESSAGE_READ]?.let { listeners ->
            listeners.map { vkEventCallback ->
                (vkEventCallback as VkEventCallback<LongPollParsedEvent.OutgoingMessageRead>)
                    .onEvent(
                        LongPollParsedEvent.OutgoingMessageRead(
                            peerId = peerId,
                            messageId = messageId,
                            unreadCount = unreadCount
                        )
                    )
            }
        }
    }

    private fun parseChatClearFlags(eventType: ApiEvent, event: List<Any>) {
        Log.d("LongPollUpdatesParser", "$eventType: $event")
    }

    private fun parseChatSetFlags(eventType: ApiEvent, event: List<Any>) {
        Log.d("LongPollUpdatesParser", "$eventType: $event")
    }

    private fun parseMessagesDeleted(eventType: ApiEvent, event: List<Any>) {
        Log.d("LongPollUpdatesParser", "$eventType: $event")

        val peerId = event[1].asInt()
        val messageId = event[2].asInt()

        listenersMap[LongPollEvent.CHAT_CLEARED]?.let { listeners ->
            listeners.forEach { vkEventCallback ->
                (vkEventCallback as VkEventCallback<LongPollParsedEvent.ChatCleared>)
                    .onEvent(
                        LongPollParsedEvent.ChatCleared(
                            peerId = peerId,
                            toMessageId = messageId
                        )
                    )
            }
        }
    }

    private fun parseChatMajorChanged(eventType: ApiEvent, event: List<Any>) {
        Log.d("LongPollUpdatesParser", "$eventType: $event")

        val peerId = event[1].asInt()
        val majorId = event[2].asInt()

        listenersMap[LongPollEvent.CHAT_MAJOR_CHANGED]?.let { listeners ->
            listeners.forEach { vkEventCallback ->
                (vkEventCallback as VkEventCallback<LongPollParsedEvent.ChatMajorChanged>)
                    .onEvent(
                        LongPollParsedEvent.ChatMajorChanged(
                            peerId = peerId,
                            majorId = majorId,
                        )
                    )
            }
        }
    }

    private fun parseChatMinorChanged(eventType: ApiEvent, event: List<Any>) {
        Log.d("LongPollUpdatesParser", "$eventType: $event")

        val peerId = event[1].asInt()
        val minorId = event[2].asInt()

        listenersMap[LongPollEvent.CHAT_MINOR_CHANGED]?.let { listeners ->
            listeners.forEach { vkEventCallback ->
                (vkEventCallback as VkEventCallback<LongPollParsedEvent.ChatMinorChanged>)
                    .onEvent(
                        LongPollParsedEvent.ChatMinorChanged(
                            peerId = peerId,
                            minorId = minorId,
                        )
                    )
            }
        }
    }

    private suspend fun loadMessage(messageId: Int): VkMessage? = suspendCoroutine { continuation ->
        coroutineScope.launch(Dispatchers.IO) {
            messagesUseCase.getById(
                messageIds = listOf(messageId),
                extended = true,
                fields = VkConstants.ALL_FIELDS
            ).listenValue(this) { state ->
                state.processState(
                    error = { error ->
                        Log.e("LongPollUpdatesParser", "loadMessage: error: $error")
                        continuation.resume(null)
                    },
                    success = { response ->
                        val message = response.singleOrNull() ?: run {
                            continuation.resume(null)
                            return@listenValue
                        }

                        VkMemoryCache[message.id] = message
                        messagesUseCase.storeMessage(message)

                        continuation.resume(message)
                    }
                )
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : LongPollParsedEvent> registerListener(
        eventType: LongPollEvent,
        listener: VkEventCallback<T>
    ) {
        listenersMap.let { map ->
            map[eventType] = (map[eventType] ?: mutableListOf())
                .also {
                    it.add(listener as VkEventCallback<LongPollParsedEvent>)
                }
        }
    }

    private fun <T : LongPollParsedEvent> registerListeners(
        eventTypes: List<LongPollEvent>,
        listener: VkEventCallback<T>
    ) {
        eventTypes.forEach { eventType -> registerListener(eventType, listener) }
    }

    fun onMessageSetFlags(block: (LongPollParsedEvent) -> Unit) {
        registerListener(LongPollEvent.MESSAGE_SET_FLAGS, assembleEventCallback(block))
    }

    fun onMessageMarkedAsImportant(block: (LongPollParsedEvent.MessageMarkedAsImportant) -> Unit) {
        registerListener(LongPollEvent.MARKED_AS_IMPORTANT, assembleEventCallback(block))
    }

    fun onMessageMarkedAsSpam(block: (LongPollParsedEvent.MessageMarkedAsSpam) -> Unit) {
        registerListener(LongPollEvent.MARKED_AS_SPAM, assembleEventCallback(block))
    }

    fun onMessageDeleted(block: (LongPollParsedEvent.MessageDeleted) -> Unit) {
        registerListener(LongPollEvent.MESSAGE_DELETED, assembleEventCallback(block))
    }

    fun onMessageClearFlags(block: (LongPollParsedEvent) -> Unit) {
        registerListener(LongPollEvent.MESSAGE_CLEAR_FLAGS, assembleEventCallback(block))
    }

    fun onMessageMarkedAsNotSpam(block: (LongPollParsedEvent.MessageMarkedAsNotSpam) -> Unit) {
        registerListener(LongPollEvent.MARKED_AS_NOT_SPAM, assembleEventCallback(block))
    }

    fun onMessageRestored(block: (LongPollParsedEvent.MessageRestored) -> Unit) {
        registerListener(LongPollEvent.MESSAGE_RESTORED, assembleEventCallback(block))
    }

    fun onNewMessage(block: (LongPollParsedEvent.NewMessage) -> Unit) {
        registerListener(LongPollEvent.MESSAGE_NEW, assembleEventCallback(block))
    }

    fun onMessageEdited(block: (LongPollParsedEvent.MessageEdited) -> Unit) {
        registerListener(LongPollEvent.MESSAGE_EDITED, assembleEventCallback(block))
    }

    fun onMessageIncomingRead(block: (LongPollParsedEvent.IncomingMessageRead) -> Unit) {
        registerListener(LongPollEvent.INCOMING_MESSAGE_READ, assembleEventCallback(block))
    }

    fun onMessageOutgoingRead(block: (LongPollParsedEvent.OutgoingMessageRead) -> Unit) {
        registerListener(LongPollEvent.OUTGOING_MESSAGE_READ, assembleEventCallback(block))
    }

    fun onChatCleared(block: (LongPollParsedEvent.ChatCleared) -> Unit) {
        registerListener(LongPollEvent.CHAT_CLEARED, assembleEventCallback(block))
    }

    fun onChatMajorChanged(block: (LongPollParsedEvent.ChatMajorChanged) -> Unit) {
        registerListener(LongPollEvent.CHAT_MAJOR_CHANGED, assembleEventCallback(block))
    }

    fun onChatMinorChanged(block: (LongPollParsedEvent.ChatMinorChanged) -> Unit) {
        registerListener(LongPollEvent.CHAT_MINOR_CHANGED, assembleEventCallback(block))
    }

    fun onInteractions(block: (LongPollParsedEvent.Interaction) -> Unit) {
        registerListeners(
            eventTypes = listOf(
                LongPollEvent.TYPING,
                LongPollEvent.AUDIO_MESSAGE_RECORDING,
                LongPollEvent.PHOTO_UPLOADING,
                LongPollEvent.VIDEO_UPLOADING,
                LongPollEvent.FILE_UPLOADING
            ),
            listener = assembleEventCallback(block)
        )
    }

    fun clearListeners() {
        listenersMap.clear()
    }
}

internal inline fun <R : LongPollParsedEvent> assembleEventCallback(
    crossinline block: (R) -> Unit,
): VkEventCallback<R> {
    return VkEventCallback { event -> block.invoke(event) }
}

fun interface VkEventCallback<in T : LongPollParsedEvent> {
    fun onEvent(event: T)
}
