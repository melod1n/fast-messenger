package dev.meloda.fast.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.meloda.fast.common.extensions.listenValue
import dev.meloda.fast.common.extensions.setValue
import dev.meloda.fast.data.State
import dev.meloda.fast.data.processState
import dev.meloda.fast.datastore.UserSettings
import dev.meloda.fast.domain.FriendsUseCase
import dev.meloda.fast.friends.model.FriendsScreenState
import dev.meloda.fast.friends.util.asPresentation
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.model.api.domain.VkUser
import dev.meloda.fast.network.VkErrorCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

// TODO: 13/07/2024, Danil Nikolaev: separate two lists and their pagination
interface FriendsViewModel {

    val screenState: StateFlow<FriendsScreenState>
    val baseError: StateFlow<BaseError?>
    val imagesToPreload: StateFlow<List<String>>
    val currentOffset: StateFlow<Int>
    val canPaginate: StateFlow<Boolean>

    fun onPaginationConditionsMet()

    fun onRefresh()

    fun onErrorConsumed()

    fun onTabSelected(tabIndex: Int)

    fun setScrollIndex(index: Int)
    fun setScrollOffset(offset: Int)
    fun setScrollIndexOnline(index: Int)
    fun setScrollOffsetOnline(offset: Int)
}

class FriendsViewModelImpl(
    private val friendsUseCase: FriendsUseCase,
    private val userSettings: UserSettings
) : ViewModel(), FriendsViewModel {

    override val screenState = MutableStateFlow(FriendsScreenState.EMPTY)

    override val baseError = MutableStateFlow<BaseError?>(null)
    override val imagesToPreload = MutableStateFlow<List<String>>(emptyList())
    override val currentOffset = MutableStateFlow(0)
    override val canPaginate = MutableStateFlow(false)

    private val friends = MutableStateFlow<List<VkUser>>(emptyList())

    init {
        userSettings.useContactNames.listenValue(viewModelScope, ::updateFriendsNames)

        loadFriends()
    }

    override fun onPaginationConditionsMet() {
        currentOffset.update { screenState.value.friends.size }
        loadFriends()
    }

    override fun onRefresh() {
        loadFriends(offset = 0)
    }

    override fun onErrorConsumed() {
        baseError.setValue { null }
    }

    override fun onTabSelected(tabIndex: Int) {
        screenState.setValue { old -> old.copy(selectedTabIndex = tabIndex) }
    }

    override fun setScrollIndex(index: Int) {
        screenState.setValue { old -> old.copy(scrollIndex = index) }
    }

    override fun setScrollOffset(offset: Int) {
        screenState.setValue { old -> old.copy(scrollOffset = offset) }
    }

    override fun setScrollIndexOnline(index: Int) {
        screenState.setValue { old -> old.copy(scrollIndexOnline = index) }
    }

    override fun setScrollOffsetOnline(offset: Int) {
        screenState.setValue { old -> old.copy(scrollOffsetOnline = offset) }
    }

    private fun loadFriends(offset: Int = currentOffset.value) {
        friendsUseCase.getFriends(count = LOAD_COUNT, offset = offset)
            .listenValue(viewModelScope) { state ->
                state.processState(
                    error = { error ->
                        if (error is State.Error.ApiError) {
                            when (error.errorCode) {
                                VkErrorCode.USER_AUTHORIZATION_FAILED -> {
                                    baseError.setValue { BaseError.SessionExpired }
                                }

                                else -> Unit
                            }
                        }
                    },
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

                        val loadedOnlineFriends = loadedFriends.filter {
                            it.onlineStatus.isOnline()
                        }

                        val newState = screenState.value.copy(
                            isPaginationExhausted = paginationExhausted
                        )

                        if (offset == 0) {
                            friends.emit(response)
                            screenState.setValue {
                                newState.copy(
                                    friends = loadedFriends,
                                    onlineFriends = loadedOnlineFriends
                                )
                            }
                        } else {
                            friends.emit(friends.value.plus(response))
                            screenState.setValue {
                                newState.copy(
                                    friends = newState.friends.plus(loadedFriends),
                                    onlineFriends = newState.onlineFriends.plus(loadedOnlineFriends)
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

    private fun updateFriendsNames(useContactNames: Boolean) {
        val friends = friends.value
        if (friends.isEmpty()) return

        val uiFriends = friends.map { conversation ->
            conversation.asPresentation(useContactNames)
        }

        val onlineUiFriends = screenState.value.onlineFriends.mapNotNull { friend ->
            uiFriends.find { it.userId == friend.userId }
        }

        screenState.setValue { old ->
            old.copy(
                friends = uiFriends,
                onlineFriends = onlineUiFriends
            )
        }
    }

    companion object {
        const val LOAD_COUNT = 15
    }
}
