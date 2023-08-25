package com.meloda.fast.screens.conversations

import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import com.github.terrakok.cicerone.Router
import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.VkUtils.fill
import com.meloda.fast.api.longpoll.LongPollEvent
import com.meloda.fast.api.longpoll.LongPollUpdatesParser
import com.meloda.fast.api.model.InteractionType
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.model.base.BaseVkGroup
import com.meloda.fast.api.model.base.BaseVkUser
import com.meloda.fast.api.model.domain.VkConversationDomain
import com.meloda.fast.api.model.presentation.VkConversationUi
import com.meloda.fast.api.network.conversations.ConversationsDeleteRequest
import com.meloda.fast.api.network.conversations.ConversationsGetRequest
import com.meloda.fast.api.network.conversations.ConversationsPinRequest
import com.meloda.fast.api.network.conversations.ConversationsUnpinRequest
import com.meloda.fast.api.network.users.UsersGetRequest
import com.meloda.fast.base.viewmodel.BaseViewModel
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.common.Screens
import com.meloda.fast.data.conversations.ConversationsRepository
import com.meloda.fast.data.messages.MessagesRepository
import com.meloda.fast.data.users.UsersRepository
import com.meloda.fast.ext.createTimerFlow
import com.meloda.fast.ext.findIndex
import com.meloda.fast.ext.findWithIndex
import com.meloda.fast.ext.setValue
import com.meloda.fast.ext.toMap
import com.meloda.fast.screens.conversations.model.ConversationsScreenState
import com.meloda.fast.screens.conversations.model.ConversationsShowOptions
import com.meloda.fast.screens.messages.model.MessagesHistoryArguments
import com.meloda.fast.screens.messages.screen.MessagesHistoryScreen
import com.meloda.fast.screens.settings.presentation.SettingsFragment
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface ConversationsViewModel {

    val screenState: StateFlow<ConversationsScreenState>

    fun onOptionsDialogDismissed()

    fun onOptionsDialogOptionClicked(conversation: VkConversationUi, key: String): Boolean

    fun onDeleteDialogDismissed()

    fun onDeleteDialogPositiveClick(conversationId: Int)

    fun onRefresh()

    fun onConversationItemClick(conversationUi: VkConversationUi)
    fun onConversationItemLongClick(conversationUi: VkConversationUi)

    fun onPinDialogDismissed()
    fun onPinDialogPositiveClick(conversation: VkConversationUi)
    fun onToolbarMenuItemClicked(itemId: Int): Boolean
}

class ConversationsViewModelImpl constructor(
    private val conversationsRepository: ConversationsRepository,
    private val usersRepository: UsersRepository,
    updatesParser: LongPollUpdatesParser,
    private val router: Router,
    private val messagesRepository: MessagesRepository,
    private val messagesHistoryScreen: MessagesHistoryScreen
) : ConversationsViewModel, BaseViewModel() {

    override val screenState = MutableStateFlow(ConversationsScreenState.EMPTY)

    private val pinnedConversationsCount = MutableStateFlow(0)

    private val domainConversations = MutableStateFlow<List<VkConversationDomain>>(emptyList())


    // TODO: 25.08.2023, Danil Nikolaev: extract to DI
    private val imageLoader by lazy {
        ImageLoader.Builder(AppGlobal.Instance)
            .crossfade(true)
            .build()
    }

    private val settingsCategories = listOf(
        "account", "appearance", "features", "visibility", "updates", "msappcenter", "wip", "debug"
    )

    init {
        val useLargeTopAppBar = AppGlobal.preferences.getBoolean(
            SettingsFragment.KEY_USE_LARGE_TOP_APP_BAR,
            SettingsFragment.DEFAULT_VALUE_USE_LARGE_TOP_APP_BAR
        )
        val multilineEnabled = AppGlobal.preferences.getBoolean(
            SettingsFragment.KEY_APPEARANCE_MULTILINE,
            SettingsFragment.DEFAULT_VALUE_MULTILINE
        )

        screenState.setValue { old ->
            old.copy(
                useLargeTopAppBar = useLargeTopAppBar,
                multilineEnabled = multilineEnabled
            )
        }
    }

    override fun onOptionsDialogDismissed() {
        emitShowOptions { old -> old.copy(showOptionsDialog = null) }
    }

    override fun onOptionsDialogOptionClicked(
        conversation: VkConversationUi,
        key: String
    ): Boolean {
        return when (key) {
            "read" -> {
                readConversation(
                    peerId = conversation.conversationId,
                    startMessageId = conversation.lastMessageId
                )
                true
            }

            "delete" -> {
                emitShowOptions { old -> old.copy(showDeleteDialog = conversation.id) }
                true
            }

            "pin" -> {
                emitShowOptions { old -> old.copy(showPinDialog = conversation) }
                true
            }

            else -> false
        }
    }

    override fun onDeleteDialogDismissed() {
        emitShowOptions { old -> old.copy(showDeleteDialog = null) }
    }

    override fun onDeleteDialogPositiveClick(conversationId: Int) {
        deleteConversation(conversationId)
    }

    override fun onRefresh() {
        loadConversations()
    }

    override fun onConversationItemClick(conversationUi: VkConversationUi) {
        openMessagesHistoryScreen(conversationUi)
    }

    override fun onConversationItemLongClick(conversationUi: VkConversationUi) {
        emitShowOptions { old -> old.copy(showOptionsDialog = conversationUi) }
    }

    override fun onPinDialogDismissed() {
        emitShowOptions { old -> old.copy(showPinDialog = null) }
    }

    override fun onPinDialogPositiveClick(conversation: VkConversationUi) {
        pinConversation(conversation.id, !conversation.isPinned)
    }

    // TODO: 25.08.2023, Danil Nikolaev: rewrite
    override fun onToolbarMenuItemClicked(itemId: Int): Boolean {
        return when (itemId) {
            0 -> {
                router.navigateTo(Screens.Settings())
                true
            }

            1 -> {
                onRefresh()
                true
            }

            else -> false
        }
    }

    init {
        updatesParser.onNewMessage(::handleNewMessage)
        updatesParser.onMessageEdited(::handleEditedMessage)
        updatesParser.onMessageIncomingRead(::handleReadIncomingMessage)
        updatesParser.onMessageOutgoingRead(::handleReadOutgoingMessage)
        updatesParser.onConversationPinStateChanged(::handlePinStateChanged)
        updatesParser.onInteractions(::handleInteraction)

        loadProfileUser()
        loadConversations()
    }

    private fun emitDomainConversations(conversations: List<VkConversationDomain>) {
        val pinnedConversationsCount = conversations.filter(VkConversationDomain::isPinned).size

        viewModelScope.launch {
            domainConversations.emit(conversations)
        }

        val uiConversations = conversations.map(VkConversationDomain::mapToPresentation)

        screenState.setValue { old ->
            old.copy(
                conversations = uiConversations,
                pinnedConversationsCount = pinnedConversationsCount
            )
        }
    }

    private fun emitShowOptions(function: (ConversationsShowOptions) -> ConversationsShowOptions) {
        val newShowOptions = function.invoke(screenState.value.showOptions)
        screenState.setValue { old -> old.copy(showOptions = newShowOptions) }
    }

    private fun loadConversations(offset: Int? = null) {
        viewModelScope.launch(Dispatchers.IO) {

            screenState.setValue { old -> old.copy(isLoading = true) }

            sendRequest(
                request = {
                    conversationsRepository.get(
                        ConversationsGetRequest(
                            count = 30,
                            extended = true,
                            offset = offset,
                            fields = VKConstants.ALL_FIELDS
                        )
                    )
                },
                onResponse = { response ->
                    val answer = response.response ?: return@sendRequest

                    val profiles = answer.profiles
                        ?.map(BaseVkUser::mapToDomain)
                        ?.toMap(hashMapOf(), VkUser::id) ?: hashMapOf()

                    val groups = answer.groups
                        ?.map(BaseVkGroup::mapToDomain)
                        ?.toMap(hashMapOf(), VkGroup::id) ?: hashMapOf()

                    val conversations = answer.items
                        .map { item ->
                            val lastMessage = item.lastMessage?.asVkMessage()
                            item.conversation.mapToDomain()
                                .fill(
                                    lastMessage = lastMessage,
                                    profiles = profiles,
                                    groups = groups
                                )
                        }

                    val messages = conversations.mapNotNull { conversation ->
                        conversation.lastMessage?.also { message ->
                            message.user = profiles[message.fromId]
                            message.group = groups[message.fromId]
                            message.actionUser = profiles[message.actionMemberId]
                            message.actionGroup = groups[message.actionMemberId]
                        }
                    }

                    val conversationsUi = conversations.map(VkConversationDomain::mapToPresentation)
                    screenState.setValue { old -> old.copy(conversations = conversationsUi) }

                    val photos = profiles.mapNotNull { profile -> profile.value.photo200 } +
                            groups.mapNotNull { group -> group.value.photo200 }
                    conversationsUi.mapNotNull { conversation -> conversation.avatar.extractUrl() }

                    photos.forEach { url ->
                        ImageRequest.Builder(AppGlobal.Instance)
                            .data(url)
                            .build()
                            .let(imageLoader::enqueue)
                    }

                    conversationsRepository.store(conversations)
                    messagesRepository.store(messages)
                },
                onAnyResult = {
                    screenState.setValue { old -> old.copy(isLoading = false) }
                }
            )
        }
    }

    private fun loadProfileUser() {
        viewModelScope.launch(Dispatchers.IO) {
            sendRequest(
                request = {
                    usersRepository.getById(UsersGetRequest(fields = VKConstants.USER_FIELDS))
                },
                onResponse = { response ->
                    val accountUser = response.response?.firstOrNull() ?: return@sendRequest
                    val mappedUser = accountUser.mapToDomain()

                    usersRepository.storeUsers(listOf(mappedUser))
                }
            )
        }
    }

    private fun deleteConversation(peerId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            sendRequest(
                request = {
                    conversationsRepository.delete(ConversationsDeleteRequest(peerId))
                },
                onResponse = {
                    val newConversations = domainConversations.value.toMutableList()
                    val conversationIndex =
                        newConversations.findIndex { it.id == peerId } ?: return@sendRequest

                    newConversations.removeAt(conversationIndex)

                    emitDomainConversations(newConversations)
                }
            )
        }
    }

    private fun pinConversation(peerId: Int, pin: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (pin) {
                sendRequest(
                    request = {
                        conversationsRepository.pin(ConversationsPinRequest(peerId))
                    },
                    onResponse = {
                        handleConversationPinStateUpdate(peerId, true)
                    }
                )
            } else {
                sendRequest(
                    request = {
                        conversationsRepository.unpin(ConversationsUnpinRequest(peerId))
                    },
                    onResponse = {
                        handleConversationPinStateUpdate(peerId, false)
                    }
                )
            }
        }
    }

    // TODO: 07.01.2023, Danil Nikolaev: handle major AND minor id
    private suspend fun handleConversationPinStateUpdate(peerId: Int, pin: Boolean) {
        withContext(Dispatchers.IO) {
            val newConversations = domainConversations.value.toMutableList()
            val conversationIndex =
                newConversations.findIndex { it.id == peerId } ?: return@withContext

            val conversation = newConversations[conversationIndex]
            newConversations.removeAt(conversationIndex)

            if (pin) {
                newConversations.add(0, conversation)
            } else {
                newConversations.add(pinnedConversationsCount.value - 1, conversation)
            }

            emitDomainConversations(newConversations)
        }
    }

    private fun handleNewMessage(event: LongPollEvent.VkMessageNewEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            val message = event.message

            messagesRepository.store(message)

            val newConversations = domainConversations.value.toMutableList()
            val conversationIndex =
                newConversations.findIndex { it.id == message.peerId }

            if (conversationIndex == null) { // диалога нет в списке
                // pizdets
            } else {
                val conversation = newConversations[conversationIndex]
                val newConversation = conversation.copyWithEssentials { old ->
                    old.copy(
                        lastMessageId = message.id,
                        lastConversationMessageId = -1,
                        unreadCount = if (!message.isOut) {
                            old.unreadCount + 1
                        } else {
                            old.unreadCount
                        }
                    )
                }.also { old -> old.lastMessage = message }

                if (conversation.isPinned()) {
                    newConversations[conversationIndex] = newConversation
                } else {
                    newConversations.removeAt(conversationIndex)

                    val toPosition = pinnedConversationsCount.value
                    newConversations.add(toPosition, newConversation)
                }

                emitDomainConversations(newConversations)
            }
        }
    }

    private fun handleEditedMessage(event: LongPollEvent.VkMessageEditEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            val message = event.message

            messagesRepository.store(message)

            val newConversations = domainConversations.value.toMutableList()

            val conversationIndex = newConversations.findIndex { it.id == message.peerId }
            if (conversationIndex == null) { // диалога нет в списке
                //  pizdets
            } else {
                val conversation = newConversations[conversationIndex]
                newConversations[conversationIndex] = conversation.copyWithEssentials { old ->
                    old.copy(
                        lastMessageId = message.id,
                        lastConversationMessageId = -1
                    )
                }.also { old -> old.lastMessage = message }

                emitDomainConversations(newConversations)
            }
        }
    }

    private fun handleReadIncomingMessage(event: LongPollEvent.VkMessageReadIncomingEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            val newConversations = domainConversations.value.toMutableList()

            val conversationIndex =
                newConversations.findIndex { it.id == event.peerId } ?: return@launch

            newConversations[conversationIndex] =
                newConversations[conversationIndex].copyWithEssentials { old ->
                    old.copy(
                        inRead = event.messageId,
                        unreadCount = event.unreadCount
                    )
                }

            emitDomainConversations(newConversations)
        }
    }

    private fun handleReadOutgoingMessage(event: LongPollEvent.VkMessageReadOutgoingEvent) =
        viewModelScope.launch(Dispatchers.IO) {
            val newConversations = domainConversations.value.toMutableList()

            val conversationIndex =
                newConversations.findIndex { it.id == event.peerId } ?: return@launch

            newConversations[conversationIndex] =
                newConversations[conversationIndex].copyWithEssentials { old ->
                    old.copy(
                        outRead = event.messageId,
                        unreadCount = event.unreadCount
                    )
                }

            emitDomainConversations(newConversations)
        }

    // TODO: 07.01.2023, Danil Nikolaev: handle major AND minor id
    private fun handlePinStateChanged(event: LongPollEvent.VkConversationPinStateChangedEvent) =
        viewModelScope.launch(Dispatchers.IO) {
            val newConversations = domainConversations.value.toMutableList()

            val conversationIndex =
                newConversations.findIndex { it.id == event.peerId } ?: return@launch

            val pin = event.majorId > 0

            val conversation = newConversations[conversationIndex]

            newConversations.removeAt(conversationIndex)

            if (pin) {
                newConversations.add(0, conversation)
            } else {
                newConversations.add(pinnedConversationsCount.value - 1, conversation)
            }

            emitDomainConversations(newConversations)
        }

    private val interactionsTimers = hashMapOf<Int, InteractionJob>()

    private data class InteractionJob(
        val interactionType: InteractionType,
        val timerJob: Job
    )

    private object NewInteractionException : CancellationException()

    private fun handleInteraction(event: LongPollEvent.Interaction) {
        val interactionType = event.interactionType
        val peerId = event.peerId
        val userIds = event.userIds

        viewModelScope.launch(Dispatchers.IO) {
            val newConversations = domainConversations.value.toMutableList()
            val conversationAndIndex =
                newConversations.findWithIndex { it.id == peerId } ?: return@launch

            newConversations[conversationAndIndex.first] =
                conversationAndIndex.second.copyWithEssentials { old ->
                    old.copy(
                        interactionType = interactionType.value,
                        interactionIds = userIds
                    )
                }

            emitDomainConversations(newConversations)

            interactionsTimers[peerId]?.let { interactionJob ->
                if (interactionJob.interactionType == interactionType) {
                    interactionJob.timerJob.cancel(NewInteractionException)
                }
            }

            var timeoutAction: (() -> Unit)? = null

            val timerJob = createTimerFlow(
                time = 5,
                onTimeoutAction = { timeoutAction?.invoke() }
            ).launchIn(viewModelScope)

            val newInteractionJob = InteractionJob(
                interactionType = interactionType,
                timerJob = timerJob
            )

            interactionsTimers[peerId] = newInteractionJob

            timeoutAction = {
                stopInteraction(peerId, newInteractionJob)
            }
        }
    }

    private fun stopInteraction(peerId: Int, interactionJob: InteractionJob) {
        viewModelScope.launch(Dispatchers.IO) {
            val newConversations = domainConversations.value.toMutableList()
            val conversationAndIndex =
                newConversations.findWithIndex { it.id == peerId } ?: return@launch

            newConversations[conversationAndIndex.first] =
                conversationAndIndex.second.copyWithEssentials { old ->
                    old.copy(
                        interactionType = -1,
                        interactionIds = emptyList()
                    )
                }

            emitDomainConversations(newConversations)
        }
    }

    private fun openMessagesHistoryScreen(conversationUi: VkConversationUi) {
        messagesHistoryScreen.show(
            router = router,
            args = MessagesHistoryArguments(conversation = conversationUi)
        )
    }

    private fun readConversation(peerId: Int, startMessageId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            sendRequest(
                request = {
                    messagesRepository.markAsRead(
                        peerId = peerId,
                        startMessageId = startMessageId
                    )
                },
                onResponse = { response ->
                    val messageId = response.response ?: return@sendRequest

                    val newConversations = domainConversations.value.toMutableList()
                    val conversationIndex =
                        newConversations.findIndex { it.id == peerId } ?: return@sendRequest

                    newConversations[conversationIndex] =
                        newConversations[conversationIndex].copyWithEssentials { old ->
                            old.copy(inRead = messageId)
                        }

                    emitDomainConversations(newConversations)
                }
            )
        }
    }
}
