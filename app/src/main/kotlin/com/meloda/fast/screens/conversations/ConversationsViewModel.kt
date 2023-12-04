package com.meloda.fast.screens.conversations

import android.util.Log
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.imageLoader
import coil.request.ImageRequest
import com.meloda.fast.api.UserConfig
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
import com.meloda.fast.data.account.AccountsDao
import com.meloda.fast.data.conversations.ConversationsRepository
import com.meloda.fast.data.messages.MessagesRepository
import com.meloda.fast.data.users.UsersRepository
import com.meloda.fast.ext.createTimerFlow
import com.meloda.fast.ext.emitOnScope
import com.meloda.fast.ext.findIndex
import com.meloda.fast.ext.findWithIndex
import com.meloda.fast.ext.setValue
import com.meloda.fast.ext.toMap
import com.meloda.fast.screens.conversations.model.ConversationsScreenState
import com.meloda.fast.screens.conversations.model.ConversationsShowOptions
import com.meloda.fast.screens.settings.SettingsKeys
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

interface ConversationsViewModel {

    val screenState: StateFlow<ConversationsScreenState>

    fun onOptionsDialogDismissed()

    fun onOptionsDialogOptionClicked(conversation: VkConversationUi, key: String): Boolean

    fun onDeleteDialogDismissed()

    fun onDeleteDialogPositiveClick(conversationId: Int)

    fun onRefresh()

    fun onConversationItemLongClick(conversationUi: VkConversationUi)

    fun onPinDialogDismissed()
    fun onPinDialogPositiveClick(conversation: VkConversationUi)
}

class ConversationsViewModelImpl constructor(
    private val conversationsRepository: ConversationsRepository,
    private val usersRepository: UsersRepository,
    updatesParser: LongPollUpdatesParser,
    private val messagesRepository: MessagesRepository,
    private val accountsDao: AccountsDao,
    private val imageLoader: ImageLoader
) : ConversationsViewModel, BaseViewModel() {

    override val screenState = MutableStateFlow(ConversationsScreenState.EMPTY)

    private val domainConversations = MutableStateFlow<List<VkConversationDomain>>(emptyList())

    private val pinnedConversationsCount = domainConversations.map { conversations ->
        conversations.filter { conversation -> conversation.isPinned() }.size
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    init {
        val multilineEnabled = AppGlobal.preferences.getBoolean(
            SettingsKeys.KEY_APPEARANCE_MULTILINE,
            SettingsKeys.DEFAULT_VALUE_MULTILINE
        )

        screenState.setValue { old ->
            old.copy(multilineEnabled = multilineEnabled)
        }

        updatesParser.onNewMessage(::handleNewMessage)
        updatesParser.onMessageEdited(::handleEditedMessage)
        updatesParser.onMessageIncomingRead(::handleReadIncomingMessage)
        updatesParser.onMessageOutgoingRead(::handleReadOutgoingMessage)
        updatesParser.onConversationPinStateChanged(::handlePinStateChanged)
        updatesParser.onInteractions(::handleInteraction)

        loadProfileUser()
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
                emitShowOptions { old -> old.copy(showDeleteDialog = conversation.conversationId) }
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

    override fun onConversationItemLongClick(conversationUi: VkConversationUi) {
        emitShowOptions { old -> old.copy(showOptionsDialog = conversationUi) }
    }

    override fun onPinDialogDismissed() {
        emitShowOptions { old -> old.copy(showPinDialog = null) }
    }

    override fun onPinDialogPositiveClick(conversation: VkConversationUi) {
        pinConversation(conversation.conversationId, !conversation.isPinned)
    }

    private fun emitDomainConversations(conversations: List<VkConversationDomain>) {
        val pinnedConversationsCount = conversations.filter(VkConversationDomain::isPinned).size

        domainConversations.emitOnScope { conversations }

        val uiConversations = conversations.map(VkConversationDomain::mapToPresentation)
        val avatars = uiConversations.mapNotNull { conversation ->
            conversation.avatar.extractUrl()
        }

        avatars.forEach { avatar ->
            val request = ImageRequest.Builder(AppGlobal.Instance)
                .data(avatar)
                .build()

            AppGlobal.Instance.imageLoader.enqueue(request)
        }

        screenState.setValue { old ->
            old.copy(
                conversations = uiConversations,
                pinnedConversationsCount = pinnedConversationsCount,
                avatars = avatars
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

                    val photos = profiles.mapNotNull { profile -> profile.value.photo200 } +
                            groups.mapNotNull { group -> group.value.photo200 } +
                            conversationsUi.mapNotNull { conversation -> conversation.avatar.extractUrl() }

                    photos.forEach { url ->
                        ImageRequest.Builder(AppGlobal.Instance)
                            .data(url)
                            .build()
                            .let(imageLoader::enqueue)
                    }

                    conversationsRepository.store(conversations)
                    messagesRepository.store(messages)

                    emitDomainConversations(conversations)
                },
                onAnyResult = {
                    screenState.setValue { old -> old.copy(isLoading = false) }
                }
            )
        }
    }

    private fun loadProfileUser() {
        viewModelScope.launch(Dispatchers.IO) {
            val accounts = accountsDao.getAll()

            Log.d("ConversationsViewModel", "initUserConfig: accounts: $accounts")
            if (accounts.isNotEmpty()) {
                val currentAccount = accounts.find { it.userId == UserConfig.currentUserId }
                if (currentAccount != null) {
                    UserConfig.parse(currentAccount)
                }
            }

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

            loadConversations()
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
                        handlePinStateChanged(
                            LongPollEvent.VkConversationPinStateChangedEvent(
                                peerId = peerId,
                                majorId = (pinnedConversationsCount.value + 1) * 16
                            )
                        )
                    }
                )
            } else {
                sendRequest(
                    request = {
                        conversationsRepository.unpin(ConversationsUnpinRequest(peerId))
                    },
                    onResponse = {
                        handlePinStateChanged(
                            LongPollEvent.VkConversationPinStateChangedEvent(
                                peerId = peerId,
                                majorId = 0
                            )
                        )
                    }
                )
            }
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

    private fun handlePinStateChanged(event: LongPollEvent.VkConversationPinStateChangedEvent) =
        viewModelScope.launch(Dispatchers.IO) {
            var pinnedCount = pinnedConversationsCount.value
            val newConversations = domainConversations.value.toMutableList()

            val conversationIndex =
                newConversations.findIndex { it.id == event.peerId } ?: return@launch

            val pin = event.majorId > 0

            val conversation = newConversations[conversationIndex]
                .copyWithEssentials { conversation ->
                    conversation.copy(majorId = event.majorId)
                }

            newConversations.removeAt(conversationIndex)

            if (pin) {
                newConversations.add(0, conversation)
            } else {
                pinnedCount -= 1

                newConversations.add(conversation)

                val pinnedSubList = newConversations.filter(VkConversationDomain::isPinned)
                val unpinnedSubList = newConversations
                    .filterNot(VkConversationDomain::isPinned)
                    .toMutableList()

                unpinnedSubList.sortByDescending { item ->
                    item.lastMessage?.date
                }

                newConversations.clear()
                newConversations += pinnedSubList + unpinnedSubList
            }

            emitDomainConversations(newConversations)
        }

    private val interactionsTimers = hashMapOf<Int, InteractionJob?>()

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
        interactionsTimers[peerId] ?: return

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

            interactionJob.timerJob.cancel()
            interactionsTimers[peerId] = null
        }
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
                onResponse = {
                    val newConversations = domainConversations.value.toMutableList()
                    val conversationIndex =
                        newConversations.findIndex { it.id == peerId } ?: return@sendRequest

                    newConversations[conversationIndex] =
                        newConversations[conversationIndex].copyWithEssentials { old ->
                            old.copy(inRead = startMessageId)
                        }

                    emitDomainConversations(newConversations)
                }
            )
        }
    }
}
