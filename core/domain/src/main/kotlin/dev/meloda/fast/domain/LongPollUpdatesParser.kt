package dev.meloda.fast.domain

import android.util.Log
import dev.meloda.fast.common.VkConstants
import dev.meloda.fast.common.extensions.asInt
import dev.meloda.fast.common.extensions.asLong
import dev.meloda.fast.common.extensions.listenValue
import dev.meloda.fast.common.extensions.toList
import dev.meloda.fast.data.UserConfig
import dev.meloda.fast.data.processState
import dev.meloda.fast.model.ApiEvent
import dev.meloda.fast.model.ConvoFlags
import dev.meloda.fast.model.InteractionType
import dev.meloda.fast.model.LongPollEvent
import dev.meloda.fast.model.LongPollParsedEvent
import dev.meloda.fast.model.MessageFlags
import dev.meloda.fast.model.api.domain.VkConvo
import dev.meloda.fast.model.api.domain.VkMessage
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume

class LongPollUpdatesParser(
    private val convoUseCase: ConvoUseCase,
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

    suspend fun parseNextUpdate(event: List<Any>): List<LongPollParsedEvent> {
        val eventId = event.first().asInt()

        return when (val eventType = ApiEvent.parseOrNull(eventId)) {
            null -> {
                Log.d("LongPollUpdatesParser", "parseNextUpdate: unknownEvent: $event")
                emptyList()
            }

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
            ApiEvent.MESSAGE_UPDATED -> parseMessageUpdated(eventType, event)
            ApiEvent.MESSAGE_CACHE_CLEAR -> parseMessageCacheClear(eventType, event)
        }
    }

    private fun parseMessageSetFlags(
        eventType: ApiEvent,
        event: List<Any>
    ): List<LongPollParsedEvent> {
        Log.d("LongPollUpdatesParser", "$eventType: $event")

        val cmId = event[1].asLong()
        val flags = event[2].asInt()
        val peerId = event[3].asLong()

        val eventsToSend = mutableListOf<LongPollParsedEvent>()

        val parsedFlags = MessageFlags.parse(flags)
        parsedFlags.forEach { flag ->
            when (flag) {
                MessageFlags.IMPORTANT -> {
                    val eventToSend = LongPollParsedEvent.MessageMarkedAsImportant(
                        peerId = peerId,
                        cmId = cmId,
                        marked = true
                    )
                    eventsToSend += eventToSend

                    listenersMap[LongPollEvent.MARKED_AS_IMPORTANT]
                        ?.forEach { it.onEvent(eventToSend) }
                }

                MessageFlags.SPAM -> {
                    val eventToSend = LongPollParsedEvent.MessageMarkedAsSpam(
                        peerId = peerId,
                        cmId = cmId
                    )
                    eventsToSend += eventToSend
                    listenersMap[LongPollEvent.MARKED_AS_SPAM]?.forEach { it.onEvent(eventToSend) }
                }

                MessageFlags.DELETED -> {
                    val eventToSend =
                        if (parsedFlags.contains(MessageFlags.DELETED_FOR_ALL)) {
                            LongPollParsedEvent.MessageDeleted(
                                peerId = peerId,
                                cmId = cmId,
                                forAll = true
                            )
                        } else {
                            LongPollParsedEvent.MessageDeleted(
                                peerId = peerId,
                                cmId = cmId,
                                forAll = false
                            )
                        }
                    eventsToSend += eventToSend
                    listenersMap[LongPollEvent.MESSAGE_DELETED]?.forEach { it.onEvent(eventToSend) }
                }

                MessageFlags.AUDIO_LISTENED -> {
                    val eventToSend = LongPollParsedEvent.AudioMessageListened(
                        peerId = peerId,
                        cmId = cmId
                    )
                    eventsToSend += eventToSend

                    listenersMap[LongPollEvent.AUDIO_MESSAGE_LISTENED]
                        ?.forEach { it.onEvent(eventToSend) }
                }

                MessageFlags.UNREAD -> Unit
                MessageFlags.OUTGOING -> Unit
                MessageFlags.FROM_GROUP_CHAT -> Unit
                MessageFlags.CANCEL_SPAM -> Unit
                MessageFlags.DELETED_FOR_ALL -> Unit
                MessageFlags.DO_NOT_SHOW_NOTIFICATION -> Unit
                MessageFlags.MESSAGE_WITH_REPLY -> Unit
                MessageFlags.REACTION -> Unit
            }
        }

        eventsToSend.forEach { eventToSend ->
            listenersMap[LongPollEvent.MESSAGE_SET_FLAGS]?.let { listeners ->
                listeners.forEach { vkEventCallback ->
                    vkEventCallback.onEvent(eventToSend)
                }
            }
        }

        return eventsToSend
    }

    private suspend fun parseMessageClearFlags(
        eventType: ApiEvent,
        event: List<Any>
    ): List<LongPollParsedEvent> = suspendCancellableCoroutine { continuation ->
        Log.d("LongPollUpdatesParser", "$eventType: $event")

        val cmId = event[1].asLong()
        val flags = event[2].asInt()
        val peerId = event[3].asLong()

        val eventsToSend = mutableListOf<LongPollParsedEvent>()

        val parsedFlags = MessageFlags.parse(flags)

        coroutineScope.launch(Dispatchers.IO) {
            val message = loadMessage(peerId = peerId, cmId = cmId)

            parsedFlags.forEach { flag ->
                when (flag) {
                    MessageFlags.IMPORTANT -> {
                        val eventToSend = LongPollParsedEvent.MessageMarkedAsImportant(
                            peerId = peerId,
                            cmId = cmId,
                            marked = false
                        )
                        eventsToSend += eventToSend

                        listenersMap[LongPollEvent.MARKED_AS_IMPORTANT]
                            ?.forEach { it.onEvent(eventToSend) }
                    }

                    MessageFlags.SPAM -> {
                        if (parsedFlags.contains(MessageFlags.CANCEL_SPAM)) {
                            if (message != null) {
                                val eventToSend =
                                    LongPollParsedEvent.MessageMarkedAsNotSpam(message = message)
                                eventsToSend += eventToSend

                                listenersMap[LongPollEvent.MARKED_AS_NOT_SPAM]
                                    ?.forEach { it.onEvent(eventToSend) }
                            }
                        }
                    }

                    MessageFlags.DELETED -> {
                        if (message != null) {
                            val eventToSend =
                                LongPollParsedEvent.MessageRestored(message = message)
                            eventsToSend += eventToSend

                            listenersMap[LongPollEvent.MESSAGE_RESTORED]
                                ?.forEach { it.onEvent(eventToSend) }
                        }
                    }

                    MessageFlags.UNREAD -> Unit
                    MessageFlags.OUTGOING -> Unit
                    MessageFlags.AUDIO_LISTENED -> Unit
                    MessageFlags.FROM_GROUP_CHAT -> Unit
                    MessageFlags.CANCEL_SPAM -> Unit
                    MessageFlags.DELETED_FOR_ALL -> Unit
                    MessageFlags.DO_NOT_SHOW_NOTIFICATION -> Unit
                    MessageFlags.MESSAGE_WITH_REPLY -> Unit
                    MessageFlags.REACTION -> Unit
                }
            }

            listenersMap[LongPollEvent.MESSAGE_CLEAR_FLAGS]?.forEach { listener ->
                eventsToSend.forEach { listener.onEvent(it) }
            }

            continuation.resume(eventsToSend)
        }
    }

    private suspend fun parseMessageNew(
        eventType: ApiEvent,
        event: List<Any>
    ): List<LongPollParsedEvent> = suspendCancellableCoroutine { continuation ->
        Log.d("LongPollUpdatesParser", "$eventType: $event")

        val cmId = event[1].asLong()
        val peerId = event[4].asLong()

        coroutineScope.launch(Dispatchers.IO) {
            val message = async { loadMessage(peerId = peerId, cmId = cmId) }.await()

            val convo =
                async {
                    loadConvo(
                        peerId = peerId,
                        extended = true,
                        fields = VkConstants.ALL_FIELDS
                    )
                }.await()

            if (message != null) {
                val event = LongPollParsedEvent.NewMessage(
                    message = message,
                    inArchive = convo?.isArchived == true
                    // TODO: 03-Apr-25, Danil Nikolaev:
                    // load user settings about restoring chats with
                    // enabled notifications from archive
                )

                listenersMap[LongPollEvent.MESSAGE_NEW]?.forEach { it.onEvent(event) }
                continuation.resume(listOf(event))
            } else {
                continuation.resume(emptyList())
            }
        }
    }

    private suspend fun parseMessageEdit(
        eventType: ApiEvent,
        event: List<Any>
    ): List<LongPollParsedEvent> = suspendCancellableCoroutine { continuation ->
        Log.d("LongPollUpdatesParser", "$eventType: $event")

        val cmId = event[1].asLong()
        val peerId = event[3].asLong()

        coroutineScope.launch(Dispatchers.IO) {
            val message = loadMessage(peerId = peerId, cmId = cmId)
            if (message != null) {
                val event = LongPollParsedEvent.MessageEdited(message)
                listenersMap[LongPollEvent.MESSAGE_EDITED]?.forEach { it.onEvent(event) }
                continuation.resume(listOf(event))
            } else {
                continuation.resume(emptyList())
            }
        }
    }

    private fun parseMessageReadIncoming(
        eventType: ApiEvent,
        event: List<Any>
    ): List<LongPollParsedEvent> {
        Log.d("LongPollUpdatesParser", "$eventType: $event")
        val peerId = event[1].asLong()
        val cmId = event[2].asLong()
        val unreadCount = event[3].asInt()

        val event = LongPollParsedEvent.IncomingMessageRead(
            peerId = peerId,
            cmId = cmId,
            unreadCount = unreadCount
        )
        listenersMap[LongPollEvent.INCOMING_MESSAGE_READ]?.forEach { it.onEvent(event) }
        return listOf(event)
    }

    private fun parseMessageReadOutgoing(
        eventType: ApiEvent,
        event: List<Any>
    ): List<LongPollParsedEvent> {
        Log.d("LongPollUpdatesParser", "$eventType: $event")
        val peerId = event[1].asLong()
        val cmId = event[2].asLong()
        val unreadCount = event[3].asInt()

        val event = LongPollParsedEvent.OutgoingMessageRead(
            peerId = peerId,
            cmId = cmId,
            unreadCount = unreadCount
        )

        listenersMap[LongPollEvent.OUTGOING_MESSAGE_READ]?.forEach { it.onEvent(event) }
        return listOf(event)
    }

    private suspend fun parseChatClearFlags(
        eventType: ApiEvent,
        event: List<Any>
    ): List<LongPollParsedEvent> = suspendCancellableCoroutine { continuation ->
        Log.d("LongPollUpdatesParser", "$eventType: $event")

        val peerId = event[1].asLong()
        val flags = event[2].asInt()

        val eventsToSend = mutableListOf<LongPollParsedEvent>()

        val parsedFlags = ConvoFlags.parse(flags)

        coroutineScope.launch(Dispatchers.IO) {
            parsedFlags.forEach { flag ->
                when (flag) {
                    ConvoFlags.ARCHIVED -> {
                        val convo = loadConvo(
                            peerId = peerId,
                            extended = true,
                            fields = VkConstants.ALL_FIELDS
                        ) ?: return@forEach

                        val message = loadMessage(
                            peerId = peerId,
                            cmId = convo.lastCmId
                        )

                        val eventToSend = LongPollParsedEvent.ChatArchived(
                            convo = convo.copy(lastMessage = message),
                            archived = false
                        )
                        eventsToSend += eventToSend

                        listenersMap[LongPollEvent.CHAT_ARCHIVED]?.forEach { it.onEvent(eventToSend) }
                    }

                    ConvoFlags.DISABLE_PUSH -> Unit
                    ConvoFlags.DISABLE_SOUND -> Unit
                    ConvoFlags.INCOMING_CHAT_REQUEST -> Unit
                    ConvoFlags.DECLINED_CHAT_REQUEST -> Unit
                    ConvoFlags.MENTION -> Unit
                    ConvoFlags.HIDE_CHAT_FROM_SEARCH -> Unit
                    ConvoFlags.BUSINESS_CHAT -> Unit
                    ConvoFlags.MARKED_MESSAGE -> Unit
                    ConvoFlags.DO_NOT_NOTIFY_MENTIONS_ALL_ONLINE -> Unit
                    ConvoFlags.DO_NOT_NOTIFY_ALL_MENTIONS -> Unit
                    ConvoFlags.MARKED_AS_UNREAD -> Unit
                    ConvoFlags.CALL_IN_PROGRESS -> Unit
                }
            }

            listenersMap[LongPollEvent.CHAT_CLEAR_FLAGS]?.forEach { listener ->
                eventsToSend.forEach { listener.onEvent(it) }
            }

            continuation.resume(eventsToSend)
        }
    }

    private suspend fun parseChatSetFlags(
        eventType: ApiEvent,
        event: List<Any>
    ): List<LongPollParsedEvent> = suspendCancellableCoroutine { continuation ->
        Log.d("LongPollUpdatesParser", "$eventType: $event")

        val peerId = event[1].asLong()
        val flags = event[2].asInt()

        val eventsToSend = mutableListOf<LongPollParsedEvent>()

        val parsedFlags = ConvoFlags.parse(flags)

        coroutineScope.launch(Dispatchers.IO) {
            parsedFlags.forEach { flag ->
                when (flag) {
                    ConvoFlags.ARCHIVED -> {
                        val convo = loadConvo(
                            peerId = peerId,
                            extended = true,
                            fields = VkConstants.ALL_FIELDS
                        ) ?: return@forEach

                        val message = loadMessage(
                            peerId = peerId,
                            cmId = convo.lastCmId
                        )

                        val eventToSend = LongPollParsedEvent.ChatArchived(
                            convo = convo.copy(lastMessage = message),
                            archived = true
                        )
                        eventsToSend += eventToSend

                        listenersMap[LongPollEvent.CHAT_ARCHIVED]?.forEach { it.onEvent(eventToSend) }
                    }

                    ConvoFlags.DISABLE_PUSH -> Unit
                    ConvoFlags.DISABLE_SOUND -> Unit
                    ConvoFlags.INCOMING_CHAT_REQUEST -> Unit
                    ConvoFlags.DECLINED_CHAT_REQUEST -> Unit
                    ConvoFlags.MENTION -> Unit
                    ConvoFlags.HIDE_CHAT_FROM_SEARCH -> Unit
                    ConvoFlags.BUSINESS_CHAT -> Unit
                    ConvoFlags.MARKED_MESSAGE -> Unit
                    ConvoFlags.DO_NOT_NOTIFY_MENTIONS_ALL_ONLINE -> Unit
                    ConvoFlags.DO_NOT_NOTIFY_ALL_MENTIONS -> Unit
                    ConvoFlags.MARKED_AS_UNREAD -> Unit
                    ConvoFlags.CALL_IN_PROGRESS -> Unit
                }
            }

            listenersMap[LongPollEvent.CHAT_SET_FLAGS]?.forEach { listener ->
                eventsToSend.forEach { listener.onEvent(it) }
            }

            continuation.resume(eventsToSend)
        }
    }

    private fun parseMessagesDeleted(
        eventType: ApiEvent,
        event: List<Any>
    ): List<LongPollParsedEvent> {
        Log.d("LongPollUpdatesParser", "$eventType: $event")

        val peerId = event[1].asLong()
        val cmId = event[2].asLong()

        val event = LongPollParsedEvent.ChatCleared(
            peerId = peerId,
            toCmId = cmId
        )
        listenersMap[LongPollEvent.CHAT_CLEARED]?.forEach { it.onEvent(event) }
        return listOf(event)
    }

    private fun parseChatMajorChanged(
        eventType: ApiEvent,
        event: List<Any>
    ): List<LongPollParsedEvent> {
        Log.d("LongPollUpdatesParser", "$eventType: $event")

        val peerId = event[1].asLong()
        val majorId = event[2].asInt()

        val event = LongPollParsedEvent.ChatMajorChanged(
            peerId = peerId,
            majorId = majorId,
        )
        listenersMap[LongPollEvent.CHAT_MAJOR_CHANGED]?.forEach { it.onEvent(event) }
        return listOf(event)
    }

    private fun parseChatMinorChanged(
        eventType: ApiEvent,
        event: List<Any>
    ): List<LongPollParsedEvent> {
        Log.d("LongPollUpdatesParser", "$eventType: $event")

        val peerId = event[1].asLong()
        val minorId = event[2].asInt()

        val event = LongPollParsedEvent.ChatMinorChanged(
            peerId = peerId,
            minorId = minorId,
        )
        listenersMap[LongPollEvent.CHAT_MINOR_CHANGED]?.forEach { it.onEvent(event) }
        return listOf(event)
    }

    private fun parseInteraction(
        eventType: ApiEvent,
        event: List<Any>
    ): List<LongPollParsedEvent> {
        Log.d("LongPollUpdatesParser", "$eventType: $event")

        val interactionType = when (eventType) {
            ApiEvent.TYPING -> InteractionType.Typing
            ApiEvent.AUDIO_MESSAGE_RECORDING -> InteractionType.VoiceMessage
            ApiEvent.PHOTO_UPLOADING -> InteractionType.Photo
            ApiEvent.VIDEO_UPLOADING -> InteractionType.Video
            ApiEvent.FILE_UPLOADING -> InteractionType.File
            else -> return emptyList()
        }

        val longPollEvent: LongPollEvent = when (eventType) {
            ApiEvent.TYPING -> LongPollEvent.TYPING
            ApiEvent.AUDIO_MESSAGE_RECORDING -> LongPollEvent.AUDIO_MESSAGE_RECORDING
            ApiEvent.PHOTO_UPLOADING -> LongPollEvent.PHOTO_UPLOADING
            ApiEvent.VIDEO_UPLOADING -> LongPollEvent.VIDEO_UPLOADING
            ApiEvent.FILE_UPLOADING -> LongPollEvent.FILE_UPLOADING
        }

        val peerId = event[1].asLong()
        val userIds = event[2].toList(Any::asLong).filter { it != UserConfig.userId }
        val totalCount = event[3].asInt()
        val timestamp = event[4].asInt()

        // if userIds contains only account's id, then we don't need to show our status
        if (userIds.isEmpty()) return emptyList()

        val event = LongPollParsedEvent.Interaction(
            interactionType = interactionType,
            peerId = peerId,
            userIds = userIds,
            totalCount = totalCount,
            timestamp = timestamp
        )

        listenersMap[longPollEvent]?.forEach { it.onEvent(event) }
        return listOf(event)
    }

    private fun parseUnreadCounterUpdate(
        eventType: ApiEvent,
        event: List<Any>
    ): List<LongPollParsedEvent> {
        Log.d("LongPollUpdatesParser", "$eventType $event")

        val unreadCount = event[1].asInt()
        val unreadUnmutedCount = event[2].asInt()
        val showOnlyMuted = event[3].asInt() == 1
        val businessNotifyUnreadCount = event[4].asInt()
        val archiveUnreadCount = event[7].asInt()
        val archiveUnreadUnmutedCount = event[8].asInt()
        val archiveMentionsCount = event[9].asInt()

        val event = LongPollParsedEvent.UnreadCounter(
            unread = unreadCount,
            unreadUnmuted = unreadUnmutedCount,
            showOnlyMuted = showOnlyMuted,
            business = businessNotifyUnreadCount,
            archive = archiveUnreadCount,
            archiveUnmuted = archiveUnreadUnmutedCount,
            archiveMentions = archiveMentionsCount
        )
        listenersMap[LongPollEvent.UNREAD_COUNTER_UPDATE]?.forEach { it.onEvent(event) }
        return listOf(event)
    }

    private suspend fun parseMessageUpdated(
        eventType: ApiEvent,
        event: List<Any>
    ): List<LongPollParsedEvent> = suspendCancellableCoroutine { continuation ->
        Log.d("LongPollUpdatesParser", "$eventType $event")

        val cmId = event[1].asLong()
        val peerId = event[4].asLong()

        coroutineScope.launch(Dispatchers.IO) {
            val message = loadMessage(peerId = peerId, cmId = cmId)

            if (message != null) {
                val event = LongPollParsedEvent.MessageUpdated(message)
                listenersMap[LongPollEvent.MESSAGE_UPDATED]?.forEach { it.onEvent(event) }
                continuation.resume(listOf(event))
            } else {
                continuation.resume(emptyList())
            }
        }
    }

    private suspend fun parseMessageCacheClear(
        eventType: ApiEvent,
        event: List<Any>
    ): List<LongPollParsedEvent> = suspendCancellableCoroutine { continuation ->
        Log.d("LongPollUpdatesParser", "$eventType $event")

        val messageId = event[1].asLong()

        coroutineScope.launch(Dispatchers.IO) {
            val message = loadMessage(messageId = messageId)
            if (message != null) {
                val event = LongPollParsedEvent.MessageCacheClear(message)
                listenersMap[LongPollEvent.MESSAGE_CACHE_CLEAR]?.forEach { it.onEvent(event) }
                continuation.resume(listOf(event))
            } else {
                continuation.resume(emptyList())
            }
        }
    }

    private suspend fun loadMessage(
        peerId: Long? = null,
        cmId: Long? = null,
        messageId: Long? = null
    ): VkMessage? = suspendCancellableCoroutine { continuation ->
        require((peerId != null && cmId != null) || messageId != null)

        coroutineScope.launch(Dispatchers.IO) {
            messagesUseCase.getById(
                peerCmIds = null,
                peerId = peerId,
                messageIds = messageId?.let(::listOf),
                cmIds = cmId?.let(::listOf),
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

                        continuation.resume(message)
                    }
                )
            }
        }
    }

    private suspend fun loadConvo(
        peerId: Long,
        extended: Boolean = false,
        fields: String? = null
    ): VkConvo? = suspendCancellableCoroutine { continuation ->
        coroutineScope.launch(Dispatchers.IO) {
            convoUseCase.getById(
                peerIds = listOf(peerId),
                extended = extended,
                fields = fields
            ).listenValue(coroutineScope) { state ->
                state.processState(
                    error = { error ->
                        Log.e("LongPollUpdatesParser", "loadConvo: error: $error")
                        continuation.resume(null)
                    },
                    success = { response ->
                        val convo = response.singleOrNull() ?: run {
                            continuation.resume(null)
                            return@listenValue
                        }

                        continuation.resume(convo)
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

    fun onChatArchived(block: (LongPollParsedEvent.ChatArchived) -> Unit) {
        registerListener(LongPollEvent.CHAT_ARCHIVED, assembleEventCallback(block))
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
}

internal inline fun <R : LongPollParsedEvent> assembleEventCallback(
    crossinline block: (R) -> Unit,
): VkEventCallback<R> {
    return VkEventCallback { event -> block.invoke(event) }
}

fun interface VkEventCallback<in T : LongPollParsedEvent> {
    fun onEvent(event: T)
}
