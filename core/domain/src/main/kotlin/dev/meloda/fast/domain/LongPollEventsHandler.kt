package dev.meloda.fast.domain

import dev.meloda.fast.database.dao.ConvoDao
import dev.meloda.fast.database.dao.MessageDao
import dev.meloda.fast.logger.FastLogger
import dev.meloda.fast.model.InteractionType
import dev.meloda.fast.model.LongPollEvent
import dev.meloda.fast.model.LongPollParsedEvent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

typealias EventListener = (event: LongPollParsedEvent) -> Unit
typealias EventListenerMap = MutableMap<LongPollEvent, MutableList<EventListener>>

class LongPollEventsHandler(
    private val logger: FastLogger,
    private val convoUseCase: ConvoUseCase,
    private val messagesUseCase: MessagesUseCase,
    private val convoDao: ConvoDao,
    private val messageDao: MessageDao,
) {
    private val job = SupervisorJob()

    private val exceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            logger.error(this::class, "CoroutineException", throwable)
        }

    private val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job + exceptionHandler

    private val coroutineScope = CoroutineScope(coroutineContext)

    private val listenersMap: EventListenerMap = mutableMapOf()

    fun handleEvents(events: List<LongPollParsedEvent>) {
        coroutineScope.launch {
            // TODO: 30.05.2026, Danil Nikolaev: switch to interactors or something else
            withContext(Dispatchers.IO) {
                events.forEach { handleNextEvent(it) }
            }
        }
    }

    private suspend fun handleNextEvent(event: LongPollParsedEvent) {
        when (event) {
            is LongPollParsedEvent.AudioMessageListened -> {

            }

            is LongPollParsedEvent.ChatArchived -> {
                val affectedRows = convoDao.updateIsArchived(
                    convoId = event.convo.id,
                    isArchived = event.convo.isArchived
                )

                logger.debug(
                    this::class,
                    "isArchived ${event.convo.isArchived}: updated $affectedRows rows."
                )
            }

            is LongPollParsedEvent.ChatCleared -> {
                val affectedRows = convoDao.updateLastCmId(
                    convoId = event.peerId,
                    cmId = event.toCmId
                )

                logger.debug(
                    this::class,
                    "updateLastCmId: updated $affectedRows rows."
                )
            }

            is LongPollParsedEvent.ChatMajorChanged -> {
                val affectedRows = convoDao.updateMajorId(
                    convoId = event.peerId,
                    majorId = event.majorId
                )

                logger.debug(
                    this::class,
                    "updateMajorId: updated $affectedRows rows."
                )
            }

            is LongPollParsedEvent.ChatMinorChanged -> {
                val affectedRows = convoDao.updateMinorId(
                    convoId = event.peerId,
                    minorId = event.minorId
                )

                logger.debug(
                    this::class,
                    "updateMinorId: updated $affectedRows rows."
                )
            }

            is LongPollParsedEvent.Interaction -> {
                val eventType = when (event.interactionType) {
                    InteractionType.Typing -> LongPollEvent.TYPING
                    InteractionType.VoiceMessage -> LongPollEvent.AUDIO_MESSAGE_RECORDING
                    InteractionType.Photo -> LongPollEvent.PHOTO_UPLOADING
                    InteractionType.Video -> LongPollEvent.VIDEO_UPLOADING
                    InteractionType.File -> LongPollEvent.FILE_UPLOADING
                }

                emitEvent(eventType, event)
            }

            is LongPollParsedEvent.MessageCacheClear -> {
                messagesUseCase.storeMessage(event.message)

                emitEvent(LongPollEvent.MESSAGE_CACHE_CLEAR, event)
            }

            is LongPollParsedEvent.MessageDeleted -> {
                val affectedRows = messageDao.markAsDeleted(
                    convoId = event.peerId,
                    cmId = event.cmId,
                    isDeleted = true
                )

                logger.debug(
                    this::class,
                    "markDeleted: updated $affectedRows rows."
                )

                emitEvent(LongPollEvent.MESSAGE_DELETED, event)
            }

            is LongPollParsedEvent.MessageEdited -> {
                messagesUseCase.storeMessage(event.message)

                emitEvent(LongPollEvent.MESSAGE_EDITED, event)
            }

            is LongPollParsedEvent.MessageMarkedAsImportant -> {
                val affectedRows = messageDao.markAsImportant(
                    convoId = event.peerId,
                    cmId = event.cmId,
                    isImportant = event.marked
                )

                logger.debug(
                    this::class,
                    "markImportant: updated $affectedRows rows."
                )

                emitEvent(LongPollEvent.MARKED_AS_IMPORTANT, event)
            }

            is LongPollParsedEvent.MessageMarkedAsNotSpam -> {
                messagesUseCase.storeMessage(event.message)

                emitEvent(LongPollEvent.MARKED_AS_NOT_SPAM, event)
            }

            is LongPollParsedEvent.MessageMarkedAsSpam -> {
                val affectedRows = messageDao.markAsSpam(
                    convoId = event.peerId,
                    cmId = event.cmId,
                    isSpam = true
                )

                logger.debug(
                    this::class,
                    "markSpam: updated $affectedRows rows."
                )

                emitEvent(LongPollEvent.MARKED_AS_SPAM, event)
            }

            is LongPollParsedEvent.MessageRestored -> {
                messagesUseCase.storeMessage(event.message)

                emitEvent(LongPollEvent.MESSAGE_RESTORED, event)
            }

            is LongPollParsedEvent.MessageUpdated -> {
                messagesUseCase.storeMessage(event.message)

                emitEvent(LongPollEvent.MESSAGE_UPDATED, event)
            }

            is LongPollParsedEvent.MessageNew -> {
                messagesUseCase.storeMessage(event.message)

                emitEvent(LongPollEvent.MESSAGE_NEW, event)
            }

            is LongPollParsedEvent.IncomingMessageRead -> {
                val affectedRows = convoDao.updateReadIncoming(
                    convoId = event.peerId,
                    cmId = event.cmId,
                    unreadCount = event.unreadCount
                )

                logger.debug(
                    this::class,
                    "inMessageRead: updated $affectedRows rows."
                )

                emitEvent(LongPollEvent.INCOMING_MESSAGE_READ, event)
            }

            is LongPollParsedEvent.OutgoingMessageRead -> {
                val affectedRows = convoDao.updateReadOutgoing(
                    convoId = event.peerId,
                    cmId = event.cmId,
                    unreadCount = event.unreadCount
                )

                logger.debug(
                    this::class,
                    "outMessageRead: updated $affectedRows rows."
                )

                emitEvent(LongPollEvent.OUTGOING_MESSAGE_READ, event)
            }

            is LongPollParsedEvent.UnreadCounter -> {
                emitEvent(LongPollEvent.UNREAD_COUNTER_UPDATE, event)
            }
        }
    }

    private fun <T : LongPollParsedEvent> emitEvent(eventType: LongPollEvent, event: T) {
        listenersMap[eventType]?.forEach { it(event) }
    }

    private fun <T : LongPollParsedEvent> registerListener(
        eventType: LongPollEvent,
        listener: (T) -> Unit
    ) {
        if (listenersMap[eventType] == null) {
            listenersMap[eventType] = mutableListOf()
        }

        @Suppress("UNCHECKED_CAST")
        listenersMap[eventType]?.add(listener as EventListener)
    }

    private fun <T : LongPollParsedEvent> registerListeners(
        eventTypes: List<LongPollEvent>,
        listener: (T) -> Unit
    ) {
        eventTypes.forEach { eventType -> registerListener(eventType, listener) }
    }

    fun onMessageSetFlags(block: (LongPollParsedEvent) -> Unit) {
        registerListener(LongPollEvent.MESSAGE_SET_FLAGS, block)
    }

    fun onMessageMarkAsImportant(block: (LongPollParsedEvent.MessageMarkedAsImportant) -> Unit) {
        registerListener(LongPollEvent.MARKED_AS_IMPORTANT, block)
    }

    fun onMessageMarkAsSpam(block: (LongPollParsedEvent.MessageMarkedAsSpam) -> Unit) {
        registerListener(LongPollEvent.MARKED_AS_SPAM, block)
    }

    fun onMessageDelete(block: (LongPollParsedEvent.MessageDeleted) -> Unit) {
        registerListener(LongPollEvent.MESSAGE_DELETED, block)
    }

    fun onMessageClearFlags(block: (LongPollParsedEvent) -> Unit) {
        registerListener(LongPollEvent.MESSAGE_CLEAR_FLAGS, block)
    }

    fun onMessageMarkAsNotSpam(block: (LongPollParsedEvent.MessageMarkedAsNotSpam) -> Unit) {
        registerListener(LongPollEvent.MARKED_AS_NOT_SPAM, block)
    }

    fun onMessageRestore(block: (LongPollParsedEvent.MessageRestored) -> Unit) {
        registerListener(LongPollEvent.MESSAGE_RESTORED, block)
    }

    fun onMessageNew(block: (LongPollParsedEvent.MessageNew) -> Unit) {
        registerListener(LongPollEvent.MESSAGE_NEW, block)
    }

    fun onMessageEdit(block: (LongPollParsedEvent.MessageEdited) -> Unit) {
        registerListener(LongPollEvent.MESSAGE_EDITED, block)
    }

    fun onMessageIncomingRead(block: (LongPollParsedEvent.IncomingMessageRead) -> Unit) {
        registerListener(LongPollEvent.INCOMING_MESSAGE_READ, block)
    }

    fun onMessageOutgoingRead(block: (LongPollParsedEvent.OutgoingMessageRead) -> Unit) {
        registerListener(LongPollEvent.OUTGOING_MESSAGE_READ, block)
    }

    fun onChatClear(block: (LongPollParsedEvent.ChatCleared) -> Unit) {
        registerListener(LongPollEvent.CHAT_CLEARED, block)
    }

    fun onChatMajorChange(block: (LongPollParsedEvent.ChatMajorChanged) -> Unit) {
        registerListener(LongPollEvent.CHAT_MAJOR_CHANGED, block)
    }

    fun onChatMinorChange(block: (LongPollParsedEvent.ChatMinorChanged) -> Unit) {
        registerListener(LongPollEvent.CHAT_MINOR_CHANGED, block)
    }

    fun onChatArchive(block: (LongPollParsedEvent.ChatArchived) -> Unit) {
        registerListener(LongPollEvent.CHAT_ARCHIVED, block)
    }

    fun onInteraction(block: (LongPollParsedEvent.Interaction) -> Unit) {
        registerListeners(
            eventTypes = listOf(
                LongPollEvent.TYPING,
                LongPollEvent.AUDIO_MESSAGE_RECORDING,
                LongPollEvent.PHOTO_UPLOADING,
                LongPollEvent.VIDEO_UPLOADING,
                LongPollEvent.FILE_UPLOADING
            ),
            listener = block
        )
    }

    fun onDestroy() {
        listenersMap.clear()
    }
}
