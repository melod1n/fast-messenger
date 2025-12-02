package dev.meloda.fast.conversations

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import dev.meloda.fast.common.extensions.listenValue
import dev.meloda.fast.common.extensions.setValue
import dev.meloda.fast.conversations.model.CreateChatScreenState
import dev.meloda.fast.data.State
import dev.meloda.fast.data.UserConfig
import dev.meloda.fast.data.processState
import dev.meloda.fast.datastore.UserSettings
import dev.meloda.fast.domain.FriendsUseCase
import dev.meloda.fast.domain.GetLocalUserByIdUseCase
import dev.meloda.fast.domain.MessagesUseCase
import dev.meloda.fast.domain.util.asPresentation
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.model.api.domain.VkUser
import dev.meloda.fast.network.VkErrorCode
import dev.meloda.fast.ui.model.api.UiFriend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateChatViewModel(
    private val friendsUseCase: FriendsUseCase,
    private val messagesUseCase: MessagesUseCase,
    private val imageLoader: ImageLoader,
    private val applicationContext: Context,
    private val getLocalUserByIdUseCase: GetLocalUserByIdUseCase,
    private val userSettings: UserSettings
) : ViewModel() {

    private val _screenState = MutableStateFlow(CreateChatScreenState.EMPTY)
    val screenState: StateFlow<CreateChatScreenState> = _screenState.asStateFlow()

    private val _baseError = MutableStateFlow<BaseError?>(null)
    val baseError: StateFlow<BaseError?> = _baseError.asStateFlow()

    private val currentOffset = MutableStateFlow(0)

    private val _canPaginate = MutableStateFlow(false)
    val canPaginate: StateFlow<Boolean> = _canPaginate.asStateFlow()

    private val _isChatCreated = MutableStateFlow<Long?>(null)
    val isChatCreated: StateFlow<Long?> = _isChatCreated.asStateFlow()

    private val _finalChatTitle = MutableStateFlow("")
    val finalChatTitle: StateFlow<String> = _finalChatTitle.asStateFlow()

    private val useContactNames: Boolean = userSettings.useContactNames.value

    private var accountUser: VkUser? = null

    init {
        fetchAccountUser()
        fetchUsers()
    }

    fun onPaginationConditionsMet() {
        currentOffset.update { screenState.value.friends.size }
        fetchUsers()
    }

    fun onRefresh() {
        onErrorConsumed()
        fetchUsers(offset = 0)
    }

    fun onErrorConsumed() {
        _baseError.setValue { null }
    }

    fun toggleFriendSelection(userId: Long) {
        val newSelectionList = screenState.value.selectedFriendsIds.toMutableList()

        if (newSelectionList.contains(userId)) {
            newSelectionList.remove(userId)
        } else {
            newSelectionList.add(userId)
        }

        _screenState.setValue { old ->
            old.copy(selectedFriendsIds = newSelectionList)
        }

        refreshFinalTitle()
    }

    fun onTitleTextInputChanged(newTitle: String) {
        _screenState.setValue { old -> old.copy(chatTitle = newTitle) }

        refreshFinalTitle()
    }

    fun onCreateChatButtonClicked() {
        _screenState.setValue { old -> old.copy(showConfirmDialog = true) }
    }

    fun onNavigatedBack() {
        viewModelScope.launch(Dispatchers.Main) {
            _isChatCreated.emit(null)
        }
    }

    fun onConfirmDialogDismissed() {
        _screenState.setValue { old -> old.copy(showConfirmDialog = false) }
    }

    fun onConfirmDialogConfirmed() {
        _screenState.setValue { old -> old.copy(showConfirmDialog = false) }
        createChat()
    }

    private fun fetchAccountUser() {
        viewModelScope.launch {
            accountUser = getLocalUserByIdUseCase.proceed(UserConfig.userId)
            if (accountUser != null) {
                _finalChatTitle.setValue { accountUser?.firstName.orEmpty() }
            }
        }
    }

    private fun refreshFinalTitle() {
        if (screenState.value.chatTitle.trim().isNotEmpty()) {
            _finalChatTitle.setValue { screenState.value.chatTitle.trim() }
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

            _finalChatTitle.setValue { finalTitle }
        }
    }

    private fun fetchUsers(
        offset: Int = currentOffset.value
    ) {
        friendsUseCase.getFriends(count = LOAD_COUNT, offset = offset)
            .listenValue(viewModelScope) { state ->
                state.processState(
                    error = ::handleError,
                    success = { response ->
                        val itemsCountSufficient = response.size == LOAD_COUNT
                        _canPaginate.setValue { itemsCountSufficient }

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
                            _screenState.setValue {
                                newState.copy(friends = loadedFriends)
                            }
                        } else {
                            _screenState.setValue {
                                newState.copy(
                                    friends = newState.friends.plus(loadedFriends)
                                )
                            }
                        }
                    }
                )

                _screenState.setValue { old ->
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
                title = finalChatTitle.value
            ).listenValue(viewModelScope) { state ->
                state.processState(
                    error = ::handleError,
                    success = { response ->
                        withContext(Dispatchers.Main) {
                            _isChatCreated.emit(2_000_000_000 + response)
                        }
                    }
                )
            }
        }
    }

    private fun handleError(error: State.Error) {
        when (error) {
            is State.Error.ApiError -> {
                when (error.errorCode) {
                    VkErrorCode.USER_AUTHORIZATION_FAILED -> {
                        _baseError.setValue { BaseError.SessionExpired }
                    }

                    else -> {
                        _baseError.setValue {
                            BaseError.SimpleError(message = error.errorMessage)
                        }
                    }
                }
            }

            State.Error.ConnectionError -> {
                _baseError.setValue {
                    BaseError.SimpleError(message = "Connection error")
                }
            }

            State.Error.InternalError -> {
                _baseError.setValue {
                    BaseError.SimpleError(message = "Internal error")
                }
            }

            State.Error.UnknownError -> {
                _baseError.setValue {
                    BaseError.SimpleError(message = "Unknown error")
                }
            }

            else -> Unit
        }
    }

    companion object {
        const val LOAD_COUNT = 30
    }
}
