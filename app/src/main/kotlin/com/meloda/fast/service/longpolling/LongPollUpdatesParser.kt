package com.meloda.fast.service.longpolling

import android.util.Log
import com.meloda.fast.api.ApiEvent
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.VkGroupsMap
import com.meloda.fast.api.VkUsersMap
import com.meloda.fast.api.model.InteractionType
import com.meloda.fast.api.model.data.VkGroupData
import com.meloda.fast.api.model.data.VkUserData
import com.meloda.fast.base.processState
import com.meloda.fast.ext.asInt
import com.meloda.fast.ext.asList
import com.meloda.fast.ext.listenValue
import com.meloda.fast.screens.messages.domain.usecase.MessagesUseCase
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Suppress("UNCHECKED_CAST")
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
            ApiEvent.MessagesDeleted -> parseMessagesDeleted(eventType, event)
            ApiEvent.PinUnpinConversation -> parseConversationPinStateChanged(eventType, event)

            ApiEvent.Typing,
            ApiEvent.VoiceRecording,
            ApiEvent.PhotoUploading,
            ApiEvent.VideoUploading,
            ApiEvent.FileUploading -> parseInteraction(eventType, event)

            ApiEvent.UnreadCountUpdate -> onNewEvent(eventType, event)
        }

    }

    private fun onNewEvent(eventType: ApiEvent, event: List<Any>) {
        Log.d("LongPollUpdatesParser", "newEvent: $eventType: $event")
    }

    private fun parseInteraction(eventType: ApiEvent, event: List<Any>) {
        Log.d("LongPollUpdatesParser", "$eventType: $event")

        val interactionType = when (eventType) {
            ApiEvent.Typing -> InteractionType.Typing
            ApiEvent.VoiceRecording -> InteractionType.VoiceMessage
            ApiEvent.PhotoUploading -> InteractionType.Photo
            ApiEvent.VideoUploading -> InteractionType.Video
            ApiEvent.FileUploading -> InteractionType.File
            else -> return
        }

        val peerId = event[1].asInt()
        val userIds = event[2].asList(Any::asInt).filter { it != UserConfig.userId }
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
            listenersMap[ApiEvent.PinUnpinConversation]?.let { listeners ->
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

        coroutineScope.launch {
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

    private fun parseMessageEdit(eventType: ApiEvent, event: List<Any>) {
        Log.d("LongPollUpdatesParser", "$eventType: $event")
        val messageId = event[1].asInt()

        coroutineScope.launch {
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

    private fun parseMessageReadIncoming(eventType: ApiEvent, event: List<Any>) {
        Log.d("LongPollUpdatesParser", "$eventType: $event")
        val peerId = event[1].asInt()
        val messageId = event[2].asInt()
        val unreadCount = event[3].asInt()

        coroutineScope.launch {
            listenersMap[ApiEvent.MessageReadIncoming]?.let { listeners ->
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
            listenersMap[ApiEvent.MessageReadOutgoing]?.let { listeners ->
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

    private suspend fun <T : LongPollEvent> loadNormalMessage(
        eventType: ApiEvent,
        messageId: Int
    ): T = suspendCoroutine {
        coroutineScope.launch(Dispatchers.IO) {
            messagesUseCase.getById(
                messagesIds = listOf(messageId),
                extended = true,
                fields = VKConstants.ALL_FIELDS
            ).listenValue(this) { state ->
                state.processState(
                    error = { error ->
                        Log.e("LongPollUpdatesParser", "loadNormalMessage: error: $error")
                    },
                    success = { response ->
                        val messagesList = response.items
                        if (messagesList.isEmpty()) return@processState

                        val usersMap = VkUsersMap.forUsers(
                            response.profiles.orEmpty().map(VkUserData::mapToDomain)
                        )
                        val groupsMap = VkGroupsMap.forGroups(
                            response.groups.orEmpty().map(VkGroupData::mapToDomain)
                        )

                        val message = messagesList.first().mapToDomain().run {
                            val (user, group) = getUserAndGroup(
                                usersMap = usersMap,
                                groupsMap = groupsMap
                            )
                            val (actionUser, actionGroup) = getActionUserAndGroup(
                                usersMap = usersMap,
                                groupsMap = groupsMap
                            )

                            copy(
                                user = user,
                                group = group,
                                actionUser = actionUser,
                                actionGroup = actionGroup
                            )
                        }

                        //messagesRepository.store(listOf(normalMessage))

                        val resumeValue: LongPollEvent? = when (eventType) {
                            ApiEvent.MessageNew -> {
                                LongPollEvent.VkMessageNewEvent(
                                    message,
                                    usersMap.users(),
                                    groupsMap.groups()
                                )
                            }

                            ApiEvent.MessageEdit -> {
                                LongPollEvent.VkMessageEditEvent(message)
                            }

                            else -> null
                        }

                        resumeValue?.let { value -> it.resume(value as T) }
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
        registerListener(ApiEvent.PinUnpinConversation, listener)
    }

    fun onConversationPinStateChanged(block: (LongPollEvent.VkConversationPinStateChangedEvent) -> Unit) {
        onConversationPinStateChanged(assembleEventCallback(block))
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

    fun onInteractions(listener: VkEventCallback<LongPollEvent.Interaction>) {
        registerListeners(
            eventTypes = listOf(
                ApiEvent.Typing,
                ApiEvent.VoiceRecording,
                ApiEvent.PhotoUploading,
                ApiEvent.VideoUploading,
                ApiEvent.FileUploading
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