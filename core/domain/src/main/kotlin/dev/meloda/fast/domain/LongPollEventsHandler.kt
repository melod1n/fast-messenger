package dev.meloda.fast.domain

import dev.meloda.fast.database.dao.ConvoDao
import dev.meloda.fast.database.dao.MessageDao
import dev.meloda.fast.logger.FastLogger
import dev.meloda.fast.model.LongPollParsedEvent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

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

    suspend fun handleEvents(events: List<LongPollParsedEvent>) {
        events.forEach { handleNextEvent(it) }
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

            }

            is LongPollParsedEvent.MessageCacheClear -> {
                messagesUseCase.storeMessage(event.message)
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
            }

            is LongPollParsedEvent.MessageEdited -> {
                messagesUseCase.storeMessage(event.message)
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
            }

            is LongPollParsedEvent.MessageMarkedAsNotSpam -> {
                messagesUseCase.storeMessage(event.message)
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
            }

            is LongPollParsedEvent.MessageRestored -> {
                messagesUseCase.storeMessage(event.message)
            }

            is LongPollParsedEvent.MessageUpdated -> {
                messagesUseCase.storeMessage(event.message)
            }

            is LongPollParsedEvent.NewMessage -> {
                messagesUseCase.storeMessage(event.message)
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
            }

            is LongPollParsedEvent.UnreadCounter -> {

            }
        }
    }
}
