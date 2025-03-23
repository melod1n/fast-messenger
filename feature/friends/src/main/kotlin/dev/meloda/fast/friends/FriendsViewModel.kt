package dev.meloda.fast.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.meloda.fast.common.extensions.listenValue
import dev.meloda.fast.common.extensions.setValue
import dev.meloda.fast.data.State
import dev.meloda.fast.data.processState
import dev.meloda.fast.datastore.UserSettings
import dev.meloda.fast.domain.FriendsUseCase
import dev.meloda.fast.domain.LoadUsersByIdsUseCase
import dev.meloda.fast.domain.util.asPresentation
import dev.meloda.fast.friends.model.FriendsScreenState
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.model.api.domain.VkUser
import dev.meloda.fast.network.VkErrorCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

interface FriendsViewModel {

    val screenState: StateFlow<FriendsScreenState>
    val baseError: StateFlow<BaseError?>
    val imagesToPreload: StateFlow<List<String>>
    val currentOffset: StateFlow<Int>
    val canPaginate: StateFlow<Boolean>

    fun onPaginationConditionsMet()

    fun onRefresh()

    fun onErrorConsumed()

    fun setScrollIndex(index: Int)
    fun setScrollOffset(offset: Int)
}

abstract class BaseFriendsViewModelImpl : ViewModel(), FriendsViewModel {

    override val screenState = MutableStateFlow(FriendsScreenState.EMPTY)

    override val baseError = MutableStateFlow<BaseError?>(null)
    override val imagesToPreload = MutableStateFlow<List<String>>(emptyList())
    override val currentOffset = MutableStateFlow(0)
    override val canPaginate = MutableStateFlow(false)

    protected val friends = MutableStateFlow<List<VkUser>>(emptyList())

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

    override fun setScrollIndex(index: Int) {
        screenState.setValue { old -> old.copy(scrollIndex = index) }
    }

    override fun setScrollOffset(offset: Int) {
        screenState.setValue { old -> old.copy(scrollOffset = offset) }
    }

    abstract fun loadFriends(offset: Int = currentOffset.value)

    protected fun handleError(error: State.Error) {
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

    protected fun updateFriendsNames(useContactNames: Boolean) {
        val friends = friends.value
        if (friends.isEmpty()) return

        val uiFriends = friends.map { conversation ->
            conversation.asPresentation(useContactNames)
        }

        screenState.setValue { old ->
            old.copy(friends = uiFriends)
        }
    }

    companion object {
        const val LOAD_COUNT = 30
    }
}

class FriendsViewModelImpl(
    private val friendsUseCase: FriendsUseCase,
    private val userSettings: UserSettings
) : BaseFriendsViewModelImpl() {

    init {
        userSettings.useContactNames.listenValue(viewModelScope, ::updateFriendsNames)
        loadFriends()
    }

    override fun loadFriends(offset: Int) {
        friendsUseCase.getFriends(count = LOAD_COUNT, offset = offset)
            .listenValue(viewModelScope) { state ->
                state.processState(
                    error = ::handleError,
                    success = { response ->
                        val itemsCountSufficient = response.size == LOAD_COUNT
                        canPaginate.setValue { itemsCountSufficient }

                        val paginationExhausted = !itemsCountSufficient &&
                                screenState.value.friends.size >= LOAD_COUNT

                        imagesToPreload.setValue {
                            response.mapNotNull(VkUser::photo100)
                        }

                        friendsUseCase.storeUsers(response)

                        val loadedFriends = response.map {
                            it.asPresentation(userSettings.useContactNames.value)
                        }

                        val newState = screenState.value.copy(
                            isPaginationExhausted = paginationExhausted
                        )

                        if (offset == 0) {
                            friends.emit(response)
                            screenState.setValue {
                                newState.copy(friends = loadedFriends)
                            }
                        } else {
                            friends.emit(friends.value.plus(response))
                            screenState.setValue {
                                newState.copy(friends = newState.friends.plus(loadedFriends))
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
}

class OnlineFriendsViewModelImpl(
    private val friendsUseCase: FriendsUseCase,
    private val userSettings: UserSettings,
    private val loadUsersByIdsUseCase: LoadUsersByIdsUseCase
) : BaseFriendsViewModelImpl() {

    init {
        userSettings.useContactNames.listenValue(viewModelScope, ::updateFriendsNames)
        loadFriends()
    }

    override fun loadFriends(offset: Int) {
        friendsUseCase.getOnlineFriends(null, null)
            .listenValue(viewModelScope) { onlineState ->
                onlineState.processState(
                    error = ::handleError,
                    success = { userIds ->
                        loadUsersByIdsUseCase(userIds = userIds).listenValue(viewModelScope) { state ->
                            state.processState(
                                error = ::handleError,
                                success = { onlineFriends ->
                                    screenState.setValue { old ->
                                        old.copy(
                                            friends = onlineFriends.map {
                                                it.asPresentation(userSettings.useContactNames.value)
                                            }
                                        )
                                    }
                                }
                            )

                            screenState.setValue { old ->
                                old.copy(
                                    isLoading = offset == 0 && (onlineState.isLoading() || state.isLoading()),
                                    isPaginating = offset > 0 && (onlineState.isLoading() || state.isLoading())
                                )
                            }
                        }
                    }
                )
            }
    }
}
