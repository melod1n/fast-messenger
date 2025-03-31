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
import dev.meloda.fast.network.VkErrorCode
import dev.meloda.fast.ui.model.api.UiFriend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface CreateChatViewModel {

    val screenState: StateFlow<CreateChatScreenState>
    val baseError: StateFlow<BaseError?>
    val currentOffset: StateFlow<Int>
    val canPaginate: StateFlow<Boolean>

    val isChatCreated: StateFlow<Long?>

    fun onPaginationConditionsMet()
    fun onRefresh()
    fun onErrorConsumed()

    fun toggleFriendSelection(userId: Long)

    fun onTitleTextInputChanged(newTitle: String)

    fun onCreateChatButtonClicked()

    fun onNavigatedBack()
}

class CreateChatViewModelImpl(
    private val friendsUseCase: FriendsUseCase,
    private val messagesUseCase: MessagesUseCase,
    private val imageLoader: ImageLoader,
    private val applicationContext: Context,
    private val getLocalUserByIdUseCase: GetLocalUserByIdUseCase,
    private val userSettings: UserSettings
) : CreateChatViewModel, ViewModel() {

    override val screenState = MutableStateFlow(CreateChatScreenState.EMPTY)
    override val baseError = MutableStateFlow<BaseError?>(null)
    override val currentOffset = MutableStateFlow(0)
    override val canPaginate = MutableStateFlow(false)

    override val isChatCreated = MutableStateFlow<Long?>(null)

    private val useContactNames: Boolean = userSettings.useContactNames.value

    init {
        loadFriends()
    }

    override fun onPaginationConditionsMet() {
        currentOffset.update { screenState.value.friends.size }
        loadFriends()
    }

    override fun onRefresh() {
        onErrorConsumed()
        loadFriends(offset = 0)
    }

    override fun onErrorConsumed() {
        baseError.setValue { null }
    }

    override fun toggleFriendSelection(userId: Long) {
        val newSelectionList = screenState.value.selectedFriendsIds.toMutableList()

        if (newSelectionList.contains(userId)) {
            newSelectionList.remove(userId)
        } else {
            newSelectionList.add(userId)
        }

        screenState.setValue { old ->
            old.copy(selectedFriendsIds = newSelectionList)
        }
    }

    override fun onTitleTextInputChanged(newTitle: String) {
        screenState.setValue { old -> old.copy(chatTitle = newTitle) }
    }

    override fun onCreateChatButtonClicked() {
        createChat()
    }

    override fun onNavigatedBack() {
        viewModelScope.launch(Dispatchers.Main) {
            isChatCreated.emit(null)
        }
    }

    private fun loadFriends(
        offset: Int = currentOffset.value
    ) {
        friendsUseCase.getFriends(count = LOAD_COUNT, offset = offset)
            .listenValue(viewModelScope) { state ->
                state.processState(
                    error = ::handleError,
                    success = { response ->
                        val itemsCountSufficient = response.size == LOAD_COUNT
                        canPaginate.setValue { itemsCountSufficient }

                        val paginationExhausted = !itemsCountSufficient &&
                                screenState.value.friends.isNotEmpty()

                        val imagesToPreload =
                            response.mapNotNull { it.photo100.takeIf { !it.isNullOrEmpty() } }

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
            val title = screenState.value.chatTitle.takeUnless(String::isBlank)

            val accountAsFriend =
                getLocalUserByIdUseCase.proceed(UserConfig.userId)?.asPresentation(useContactNames)

            val accountList = accountAsFriend?.let(::listOf) ?: emptyList()

            val selectedFriends = screenState.value.selectedFriendsIds
                .takeIf { it.isNotEmpty() }
                ?.mapNotNull { userId -> screenState.value.friends.find { it.userId == userId } }

            messagesUseCase.createChat(
                userIds = selectedFriends?.map { it.userId },
                title = title
                    ?: (accountList + selectedFriends.orEmpty()).joinToString(transform = UiFriend::firstName)
            ).listenValue(viewModelScope) { state ->
                state.processState(
                    error = ::handleError,
                    success = { response ->
                        withContext(Dispatchers.Main) {
                            isChatCreated.emit(2_000_000_000 + response)
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
                        baseError.setValue { BaseError.SessionExpired }
                    }

                    else -> {
                        baseError.setValue {
                            BaseError.SimpleError(message = error.errorMessage)
                        }
                    }
                }
            }

            State.Error.ConnectionError -> {
                baseError.setValue {
                    BaseError.SimpleError(message = "Connection error")
                }
            }

            State.Error.InternalError -> {
                baseError.setValue {
                    BaseError.SimpleError(message = "Internal error")
                }
            }

            State.Error.UnknownError -> {
                baseError.setValue {
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
