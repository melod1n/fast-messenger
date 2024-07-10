package com.meloda.app.fast.friends

import androidx.lifecycle.ViewModel
import com.meloda.app.fast.common.extensions.listenValue
import com.meloda.app.fast.common.extensions.setValue
import com.meloda.app.fast.data.State
import com.meloda.app.fast.data.api.friends.FriendsUseCase
import com.meloda.app.fast.data.processState
import com.meloda.app.fast.datastore.UserSettings
import com.meloda.app.fast.friends.model.FriendsScreenState
import com.meloda.app.fast.friends.util.asPresentation
import com.meloda.app.fast.model.BaseError
import com.meloda.app.fast.model.api.domain.VkUser
import com.meloda.app.fast.network.VkErrorCodes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

interface FriendsViewModel {

    val screenState: StateFlow<FriendsScreenState>
    val baseError: StateFlow<BaseError?>
    val imagesToPreload: StateFlow<List<String>>
    val currentOffset: StateFlow<Int>
    val canPaginate: StateFlow<Boolean>

    fun onMetPaginationCondition()

    fun onRefresh()

    fun onErrorConsumed()
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
        loadFriends()
    }

    private fun loadFriends(offset: Int = currentOffset.value) {
        friendsUseCase.getFriends(count = 30, offset = offset).listenValue { state ->
            state.processState(
                error = { error ->
                    when (error) {
                        is State.Error.ApiError -> {
                            val (code, message) = error

                            when (code) {
                                VkErrorCodes.UserAuthorizationFailed -> {
                                    baseError.setValue { BaseError.SessionExpired }
                                }

                                else -> {
                                    Unit
                                }
                            }
                        }

                        State.Error.ConnectionError -> TODO()
                        State.Error.InternalError -> TODO()
                        is State.Error.OAuthError -> TODO()
                        State.Error.Unknown -> TODO()
                    }
                },
                success = { response ->
                    val itemsCountSufficient = response.size == 30
                    canPaginate.setValue { itemsCountSufficient }

                    val paginationExhausted = !itemsCountSufficient &&
                            screenState.value.friends.size >= 30

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

    override fun onMetPaginationCondition() {
        currentOffset.update { screenState.value.friends.size }
        loadFriends()
    }

    override fun onRefresh() {
        loadFriends(offset = 0)
    }

    override fun onErrorConsumed() {
        baseError.setValue { null }
    }
}
