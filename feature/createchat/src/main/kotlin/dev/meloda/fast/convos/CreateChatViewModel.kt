package dev.meloda.fast.convos

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import dev.meloda.fast.common.extensions.listenValue
import dev.meloda.fast.common.extensions.setValue
import dev.meloda.fast.common.extensions.updateValue
import dev.meloda.fast.convos.model.CreateChatEffect
import dev.meloda.fast.convos.model.CreateChatIntent
import dev.meloda.fast.convos.model.CreateChatNavigationIntent
import dev.meloda.fast.convos.model.CreateChatScreenState
import dev.meloda.fast.data.UserConfig
import dev.meloda.fast.data.VkUtils
import dev.meloda.fast.data.processState
import dev.meloda.fast.datastore.UserSettings
import dev.meloda.fast.domain.FriendsUseCase
import dev.meloda.fast.domain.GetLocalUserByIdUseCase
import dev.meloda.fast.domain.MessagesUseCase
import dev.meloda.fast.domain.util.asPresentation
import dev.meloda.fast.model.api.domain.VkUser
import dev.meloda.fast.ui.model.vk.UiFriend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateChatViewModel(
    private val friendsUseCase: FriendsUseCase,
    private val messagesUseCase: MessagesUseCase,
    private val imageLoader: ImageLoader,
    private val applicationContext: Context,
    private val getLocalUserByIdUseCase: GetLocalUserByIdUseCase,
    userSettings: UserSettings
) : ViewModel() {

    private val screenState = MutableStateFlow(CreateChatScreenState.EMPTY)
    val screenStateFlow: StateFlow<CreateChatScreenState> get() = screenState.asStateFlow()

    private val screenEffect: MutableSharedFlow<CreateChatEffect> =
        MutableSharedFlow(extraBufferCapacity = 1)
    val screenEffectFlow: SharedFlow<CreateChatEffect> get() = screenEffect.asSharedFlow()

    private val useContactNames: Boolean = userSettings.useContactNames.value

    private var currentOffset = 0
    private var accountUser: VkUser? = null

    init {
        fetchAccountUser()
        fetchUsers()
    }

    fun handleIntent(intent: CreateChatIntent) {
        when (intent) {
            CreateChatIntent.Back -> screenEffect.tryEmit(
                CreateChatEffect.Navigate(CreateChatNavigationIntent.Back)
            )

            CreateChatIntent.Refresh -> onRefresh()
            CreateChatIntent.PaginationConditionsMet -> onPaginationConditionsMet()
            is CreateChatIntent.TitleInput -> onTitleTextInputChanged(intent.input)
            CreateChatIntent.CreateChatButtonClick -> onCreateChatButtonClicked()
            is CreateChatIntent.ListItemClick -> toggleFriendSelection(intent.id)
            is CreateChatIntent.RemoveUserClick -> {

            }

            is CreateChatIntent.Dialog -> {
                when (intent) {
                    CreateChatIntent.Dialog.ConfirmClick -> onConfirmDialogConfirmed()
                    CreateChatIntent.Dialog.Dismiss -> onConfirmDialogDismissed()
                }
            }
        }
    }

    private fun onPaginationConditionsMet() {
        currentOffset = screenState.value.friends.size
        fetchUsers()
    }

    private fun onRefresh() {
        onErrorConsumed()
        fetchUsers(offset = 0)
    }

    private fun onErrorConsumed() {
        screenState.setValue { old -> old.copy(error = null) }
    }

    private fun toggleFriendSelection(userId: Long) {
        val newSelectionList = screenState.value.selectedFriendsIds.toMutableList()

        if (newSelectionList.contains(userId)) {
            newSelectionList.remove(userId)
        } else {
            newSelectionList.add(userId)
        }

        screenState.setValue { old ->
            old.copy(selectedFriendsIds = newSelectionList)
        }

        refreshFinalTitle()
    }

    private fun onTitleTextInputChanged(newTitle: String) {
        screenState.setValue { old -> old.copy(chatTitle = newTitle) }

        refreshFinalTitle()
    }

    private fun onCreateChatButtonClicked() {
        screenState.setValue { old -> old.copy(showConfirmDialog = true) }
    }

    private fun onConfirmDialogDismissed() {
        screenState.setValue { old -> old.copy(showConfirmDialog = false) }
    }

    private fun onConfirmDialogConfirmed() {
        screenState.setValue { old -> old.copy(showConfirmDialog = false) }
        createChat()
    }

    private fun fetchAccountUser() {
        viewModelScope.launch {
            accountUser = getLocalUserByIdUseCase.proceed(UserConfig.userId)
            if (accountUser != null) {
                screenState.setValue { old ->
                    old.copy(finalChatTitle = accountUser?.firstName.orEmpty())
                }
            }
        }
    }

    private fun refreshFinalTitle() {
        if (screenState.value.chatTitle.trim().isNotEmpty()) {
            screenState.setValue { old ->
                old.copy(finalChatTitle = screenState.value.chatTitle.trim())
            }
        } else {
            val accountAsFriend = accountUser?.asPresentation(useContactNames)

            val accountList = accountAsFriend?.let(::listOf) ?: emptyList()

            val selectedFriends = screenState.value.selectedFriendsIds
                .take(3)
                .takeIf { it.isNotEmpty() }
                ?.mapNotNull { userId -> screenState.value.friends.find { it.userId == userId } }

            val finalTitle =
                (accountList + selectedFriends.orEmpty()).joinToString(transform = UiFriend::firstName)
                    .plus(if (screenState.value.selectedFriendsIds.size > 3) ", ..." else "")

            screenState.setValue { old -> old.copy(finalChatTitle = finalTitle) }
        }
    }

    private fun fetchUsers(
        offset: Int = currentOffset
    ) {
        friendsUseCase.getFriends(count = LOAD_COUNT, offset = offset)
            .listenValue(viewModelScope) { state ->
                state.processState(
                    error = { error ->
                        screenState.updateValue { copy(error = VkUtils.parseError(error)) }
                    },
                    success = { response ->
                        val itemsCountSufficient = response.size == LOAD_COUNT
                        screenState.setValue { old -> old.copy(canPaginate = itemsCountSufficient) }

                        val paginationExhausted = !itemsCountSufficient &&
                                screenState.value.friends.isNotEmpty()

                        val imagesToPreload =
                            response.mapNotNull { it.photo100.takeIf { p -> !p.isNullOrEmpty() } }

                        imagesToPreload.forEach { url ->
                            imageLoader.enqueue(
                                ImageRequest.Builder(applicationContext)
                                    .data(url)
                                    .build()
                            )
                        }

                        friendsUseCase.storeUsers(response)

                        val loadedFriends = response.map {
                            it.asPresentation(useContactNames)
                        }

                        val newState = screenState.value.copy(
                            isPaginationExhausted = paginationExhausted
                        )
                        if (offset == 0) {
                            screenState.setValue {
                                newState.copy(friends = loadedFriends)
                            }
                        } else {
                            screenState.setValue {
                                newState.copy(
                                    friends = newState.friends.plus(loadedFriends)
                                )
                            }
                        }
                    }
                )

                screenState.setValue { old ->
                    old.copy(
                        isLoading = offset == 0 && state.isLoading(),
                        isPaginating = offset > 0 && state.isLoading()
                    )
                }
            }
    }

    private fun createChat() {
        viewModelScope.launch {
            val selectedFriends = screenState.value.selectedFriendsIds
                .takeIf { it.isNotEmpty() }
                ?.mapNotNull { userId -> screenState.value.friends.find { it.userId == userId } }

            messagesUseCase.createChat(
                userIds = selectedFriends?.map { it.userId },
                title = screenState.value.finalChatTitle
            ).listenValue(viewModelScope) { state ->
                state.processState(
                    error = { error ->
                        screenState.updateValue { copy(error = VkUtils.parseError(error)) }
                    },
                    success = { response ->
                        withContext(Dispatchers.Main) {
                            screenEffect.emit(
                                CreateChatEffect.Navigate(
                                    CreateChatNavigationIntent.ToNewChat(2_000_000_000 + response)
                                )
                            )
                        }
                    }
                )
            }
        }
    }

    companion object {
        const val LOAD_COUNT = 30
    }
}
