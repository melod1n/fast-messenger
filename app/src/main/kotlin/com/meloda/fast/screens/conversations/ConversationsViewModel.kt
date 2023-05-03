package com.meloda.fast.screens.conversations

import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import com.github.terrakok.cicerone.Router
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VKConstants
import com.meloda.fast.api.VkUtils
import com.meloda.fast.api.longpoll.LongPollEvent
import com.meloda.fast.api.longpoll.LongPollUpdatesParser
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.model.base.BaseVkGroup
import com.meloda.fast.api.model.base.BaseVkUser
import com.meloda.fast.api.model.data.BaseVkConversation
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
import com.meloda.fast.ext.findIndex
import com.meloda.fast.ext.toMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface ConversationsViewModel {

    val pinnedConversationsCount: StateFlow<Int>

    val conversationsList: StateFlow<List<VkConversationUi>>
    val isLoading: StateFlow<Boolean>

    val isNeedToShowOptionsDialog: StateFlow<VkConversationDomain?>
    val isNeedToShowDeleteDialog: StateFlow<Int?>
    val isNeedToShowPinDialog: StateFlow<VkConversationDomain?>

    fun onOptionsDialogDismissed()

    fun onOptionsDialogOptionClicked(conversation: VkConversationDomain, key: String)

    fun onDeleteDialogDismissed()

    fun onDeleteDialogPositiveClick(conversationId: Int)

    fun onRefresh()

    fun onConversationItemClick(conversationUi: VkConversationUi)
    fun onConversationItemLongClick(conversationUi: VkConversationUi): Boolean

    fun onPinDialogDismissed()
    fun onPinDialogPositiveClick(conversation: VkConversationDomain)
    fun onToolbarMenuItemClicked(itemId: Int): Boolean
}

class ConversationsViewModelImpl constructor(
    private val conversationsRepository: ConversationsRepository,
    private val usersRepository: UsersRepository,
    updatesParser: LongPollUpdatesParser,
    private val router: Router,
    private val messagesRepository: MessagesRepository,
) : ConversationsViewModel, BaseViewModel() {

    private val dataConversations: MutableStateFlow<List<BaseVkConversation>> =
        MutableStateFlow(emptyList())

    private val domainConversations: MutableStateFlow<List<VkConversationDomain>> =
        MutableStateFlow(emptyList())

    override val conversationsList: StateFlow<List<VkConversationUi>> =
        domainConversations.map { list ->
            list.map(VkConversationDomain::mapToPresentation)
        }.stateIn(viewModelScope, SharingStarted.Lazily, initialValue = emptyList())

    override val isLoading = MutableStateFlow(false)

    override val isNeedToShowOptionsDialog = MutableStateFlow<VkConversationDomain?>(null)
    override val isNeedToShowDeleteDialog = MutableStateFlow<Int?>(null)
    override val isNeedToShowPinDialog = MutableStateFlow<VkConversationDomain?>(null)

    private val profiles: MutableStateFlow<HashMap<Int, VkUser>> = MutableStateFlow(hashMapOf())
    private val groups: MutableStateFlow<HashMap<Int, VkGroup>> = MutableStateFlow(hashMapOf())

    override val pinnedConversationsCount = domainConversations.map { conversations ->
        val pinnedConversations = conversations.filter { it.isPinned() }
        pinnedConversations.size
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val imageLoader by lazy {
        ImageLoader.Builder(AppGlobal.Instance)
            .crossfade(true)
            .build()
    }

    override fun onOptionsDialogDismissed() {
        viewModelScope.launch(Dispatchers.Main) {
            isNeedToShowOptionsDialog.emit(null)
        }
    }

    override fun onOptionsDialogOptionClicked(conversation: VkConversationDomain, key: String) {
        when (key) {
            "read" -> readConversation(conversation)
            "delete" -> isNeedToShowDeleteDialog.tryEmit(conversation.id)
            "pin" -> isNeedToShowPinDialog.tryEmit(conversation)
        }
    }

    override fun onDeleteDialogDismissed() {
        viewModelScope.launch(Dispatchers.Main) {
            isNeedToShowDeleteDialog.emit(null)
        }
    }

    override fun onDeleteDialogPositiveClick(conversationId: Int) {
        deleteConversation(conversationId)
    }

    override fun onRefresh() {
        loadConversations()
    }

    override fun onConversationItemClick(conversationUi: VkConversationUi) {
        openMessagesHistoryScreen(
            conversationUi,
            conversationUi.conversationUser,
            conversationUi.conversationGroup
        )
    }

    override fun onConversationItemLongClick(conversationUi: VkConversationUi): Boolean {
        val domainConversation = domainConversations.value.find { it.id == conversationUi.id }
        isNeedToShowOptionsDialog.tryEmit(domainConversation)
        return true
    }

    override fun onPinDialogDismissed() {
        viewModelScope.launch(Dispatchers.Main) {
            isNeedToShowPinDialog.emit(null)
        }
    }

    override fun onPinDialogPositiveClick(conversation: VkConversationDomain) {
        pinConversation(conversation.id, !conversation.isPinned())
    }

    override fun onToolbarMenuItemClicked(itemId: Int): Boolean {
        return when (itemId) {
            R.id.settings -> {
                router.navigateTo(Screens.Settings())
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

        loadProfileUser()
        loadConversations()
    }

    private fun loadConversations(offset: Int? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            sendRequest {
                conversationsRepository.get(
                    ConversationsGetRequest(
                        count = 30,
                        extended = true,
                        offset = offset,
                        fields = VKConstants.ALL_FIELDS
                    )
                )
            }?.response?.let { response ->
                val dataConversationsMessages = response.items.map { item ->
                    item.conversation to item.lastMessage
                }
                val dataConversationsList = dataConversationsMessages.map { pair -> pair.first }
                dataConversations.emit(dataConversationsList)

                val messages =
                    dataConversationsMessages
                        .map { pair -> pair.second }
                        .mapNotNull { message -> message?.asVkMessage() }

                messagesRepository.store(messages)

                val newProfiles = response.profiles
                    ?.map(BaseVkUser::mapToDomain)
                    ?.toMap(hashMapOf(), VkUser::id) ?: hashMapOf()
                profiles.emit(newProfiles)

                val newGroups = response.groups
                    ?.map(BaseVkGroup::mapToDomain)
                    ?.toMap(hashMapOf(), VkGroup::id) ?: hashMapOf()
                groups.emit(newGroups)

                val photos = newProfiles.mapNotNull { profile -> profile.value.photo200 } +
                        newGroups.mapNotNull { group -> group.value.photo200 }

                photos.forEach { url ->
                    ImageRequest.Builder(AppGlobal.Instance)
                        .data(url)
                        .build()
                        .let(imageLoader::enqueue)
                }

                val domainConversationsList = dataConversationsList.mapToDomain()
                emitConversations(domainConversationsList)
            }
        }
    }

    private suspend fun emitConversations(conversations: List<VkConversationDomain>) =
        withContext(Dispatchers.Default) {
            domainConversations.emit(conversations)
        }

    private suspend fun List<BaseVkConversation>.mapToDomain(): List<VkConversationDomain> =
        this.map { baseConversation -> getFilledDomainVkConversation(baseConversation) }

    private suspend fun VkConversationDomain.fill(): VkConversationDomain {
        val conversation = this
        val messages = messagesRepository.getCached(conversation.id)

        val lastMessage = messages.find { it.id == conversation.lastMessageId }
        conversation.lastMessage = lastMessage

        val userGroup =
            VkUtils.getConversationUserGroup(
                conversation,
                profiles.value,
                groups.value
            )
        val actionUserGroup =
            VkUtils.getMessageActionUserGroup(
                lastMessage,
                profiles.value,
                groups.value
            )
        val messageUserGroup =
            VkUtils.getMessageUserGroup(
                lastMessage,
                profiles.value,
                groups.value
            )

        conversation.conversationUser = userGroup.first
        conversation.conversationGroup = userGroup.second
        conversation.action = lastMessage?.getPreparedAction()
        conversation.actionUser = actionUserGroup.first
        conversation.actionGroup = actionUserGroup.second
        conversation.messageUser = messageUserGroup.first
        conversation.messageGroup = messageUserGroup.second

        return conversation
    }

    private suspend fun getFilledDomainVkConversation(
        baseConversation: BaseVkConversation,
        defDomainConversation: VkConversationDomain? = null,
    ): VkConversationDomain {
        val conversation = defDomainConversation ?: baseConversation.mapToDomain()
        return conversation.fill()
    }

    private fun loadProfileUser() {
        viewModelScope.launch(Dispatchers.IO) {
            sendRequest {
                usersRepository.getById(UsersGetRequest(fields = VKConstants.USER_FIELDS))
            }?.response?.let { response ->
                val users = response.map(BaseVkUser::mapToDomain)
                usersRepository.storeUsers(users)

                UserConfig.vkUser.emit(users.first())
            }
        }
    }

    private fun deleteConversation(peerId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            sendRequest {
                conversationsRepository.delete(ConversationsDeleteRequest(peerId))
            }?.let {
                domainConversations.value.toMutableList().let { list ->
                    val index = list.indexOfFirst { conversation -> conversation.id == peerId }
                    if (index != -1) {
                        list.removeAt(index)
                        domainConversations.emit(list)
                    }
                }
            }
        }
    }

    private fun pinConversation(peerId: Int, pin: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (pin) {
                sendRequest {
                    conversationsRepository.pin(ConversationsPinRequest(peerId))
                }?.let { handleConversationPinStateUpdate(peerId, true) }
            } else {
                sendRequest {
                    conversationsRepository.unpin(ConversationsUnpinRequest(peerId))
                }?.let {
                    handleConversationPinStateUpdate(peerId, false)
                }
            }
        }
    }

    // TODO: 07.01.2023, Danil Nikolaev: handle major AND minor id
    private suspend fun handleConversationPinStateUpdate(peerId: Int, pin: Boolean) {
        withContext(Dispatchers.IO) {
            val conversationsList = domainConversations.value.toMutableList()
            val conversationIndex =
                conversationsList.findIndex { it.id == peerId } ?: return@withContext

            val conversation = conversationsList[conversationIndex].copy(
                majorId = if (pin) (pinnedConversationsCount.value + 1) * 16
                else 0
            ).fill()

            conversationsList.removeAt(conversationIndex)

            if (pin) {
                conversationsList.add(0, conversation)
            } else {
                conversationsList.add(pinnedConversationsCount.value - 1, conversation)
            }

            emitConversations(conversationsList)
        }
    }

    private fun handleNewMessage(event: LongPollEvent.VkMessageNewEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            val message = event.message

            messagesRepository.store(message)

            val newProfiles: HashMap<Int, VkUser> =
                (profiles.value + event.profiles) as HashMap<Int, VkUser>
            profiles.update { newProfiles }

            val newGroups: HashMap<Int, VkGroup> =
                (groups.value + event.groups) as HashMap<Int, VkGroup>
            groups.update { newGroups }

            val dataConversationsList = domainConversations.value.toMutableList()
            val dataConversationIndex = dataConversationsList.findIndex { it.id == message.peerId }

            if (dataConversationIndex == null) { // диалога нет в списке
                // pizdets
            } else {
                val dataConversation = dataConversationsList[dataConversationIndex]
                var newConversation = dataConversation.copy(
                    lastMessageId = message.id,
                    lastConversationMessageId = -1
                ).fill().also {
                    it.lastMessage = message
                }
                if (!message.isOut) {
                    newConversation = newConversation.copy(
                        unreadCount = newConversation.unreadCount + 1
                    ).fill().also {
                        it.lastMessage = message
                    }
                }

                if (dataConversation.isPinned()) {
                    dataConversationsList[dataConversationIndex] = newConversation
                    emitConversations(dataConversationsList)
                    return@launch
                }

                dataConversationsList.removeAt(dataConversationIndex)

                val toPosition = pinnedConversationsCount.value
                dataConversationsList.add(toPosition, newConversation)

                emitConversations(dataConversationsList)
            }
        }
    }

    private fun handleEditedMessage(event: LongPollEvent.VkMessageEditEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            val message = event.message

            messagesRepository.store(message)

            val conversationsList = domainConversations.value.toMutableList()

            val conversationIndex = conversationsList.findIndex { it.id == message.peerId }
            if (conversationIndex == null) { // диалога нет в списке

            } else {
                val conversation = conversationsList[conversationIndex]
                conversationsList[conversationIndex] = conversation.copy(
                    lastMessageId = message.id,
                    lastConversationMessageId = -1
                ).fill().also {
                    it.lastMessage = message
                }

                emitConversations(conversationsList)
            }
        }
    }

    private fun handleReadIncomingMessage(event: LongPollEvent.VkMessageReadIncomingEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            val conversationsList = domainConversations.value.toMutableList()

            val conversationIndex =
                conversationsList.findIndex { it.id == event.peerId } ?: return@launch

            var conversation = conversationsList[conversationIndex]
            conversation = conversation.copy(
                inRead = event.messageId,
                unreadCount = event.unreadCount
            ).fill()

            conversationsList[conversationIndex] = conversation

            emitConversations(conversationsList)
        }
    }

    private fun handleReadOutgoingMessage(event: LongPollEvent.VkMessageReadOutgoingEvent) =
        viewModelScope.launch(Dispatchers.IO) {
            val conversationsList = domainConversations.value.toMutableList()

            val conversationIndex =
                conversationsList.findIndex { it.id == event.peerId } ?: return@launch

            var conversation = conversationsList[conversationIndex]
            conversation = conversation.copy(
                outRead = event.messageId,
                unreadCount = event.unreadCount
            ).fill()

            conversationsList[conversationIndex] = conversation

            emitConversations(conversationsList)
        }

    // TODO: 07.01.2023, Danil Nikolaev: handle major AND minor id
    private fun handlePinStateChanged(event: LongPollEvent.VkConversationPinStateChangedEvent) =
        viewModelScope.launch(Dispatchers.IO) {
            val conversationsList = domainConversations.value.toMutableList()

            val conversationIndex =
                conversationsList.findIndex { it.id == event.peerId } ?: return@launch

            val pin = event.majorId > 0

            var conversation = conversationsList[conversationIndex]
            conversation = conversation.copy(
                majorId = event.majorId
            ).fill()

            conversationsList.removeAt(conversationIndex)

            if (pin) {
                conversationsList.add(0, conversation)
            } else {
                conversationsList.add(pinnedConversationsCount.value - 1, conversation)
            }

            emitConversations(conversationsList)
        }


    private fun openMessagesHistoryScreen(
        conversationUi: VkConversationUi,
        user: VkUser?,
        group: VkGroup?,
    ) {
        val conversation = domainConversations.value.find { domainConversation ->
            domainConversation.id == conversationUi.id
        } ?: return

        router.navigateTo(Screens.MessagesHistory(conversation, user, group))
    }

    private fun readConversation(conversation: VkConversationDomain) {
        viewModelScope.launch(Dispatchers.IO) {
            sendRequest {
                messagesRepository.markAsRead(
                    peerId = conversation.id,
                    startMessageId = conversation.lastMessageId
                )
            }?.response?.let { messageId ->
                domainConversations.value.toMutableList().let { list ->
                    val index = list.indexOf(conversation)
                    val newConversation = list[index].copy(inRead = messageId)
                    list[index] = newConversation

                    domainConversations.emit(list)
                }
            }
        }
    }
}
