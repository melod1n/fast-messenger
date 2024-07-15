package com.meloda.app.fast.profile

import androidx.lifecycle.ViewModel
import com.meloda.app.fast.common.UserConfig
import com.meloda.app.fast.common.VkConstants
import com.meloda.app.fast.common.extensions.listenValue
import com.meloda.app.fast.common.extensions.setValue
import com.meloda.app.fast.data.State
import com.meloda.app.fast.data.api.users.UsersUseCase
import com.meloda.app.fast.data.processState
import com.meloda.app.fast.model.BaseError
import com.meloda.app.fast.network.VkErrorCode
import com.meloda.app.fast.profile.model.ProfileScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface ProfileViewModel {
    val screenState: StateFlow<ProfileScreenState>
    val baseError: StateFlow<BaseError?>
}

class ProfileViewModelImpl(
    private val usersUseCase: UsersUseCase
) : ViewModel(), ProfileViewModel {

    override val screenState = MutableStateFlow(ProfileScreenState.EMPTY)
    override val baseError = MutableStateFlow<BaseError?>(null)

    init {
        getLocalAccountInfo()
    }

    private fun getLocalAccountInfo() {
        usersUseCase.getLocalUser(UserConfig.userId)
            .listenValue { state ->
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

                        screenState.setValue { old ->
                            old.copy(
                                avatarUrl = null,
                                fullName = null
                            )
                        }
                    },
                    success = { user ->
                        screenState.setValue { old ->
                            old.copy(
                                avatarUrl = user?.photo200,
                                fullName = user?.fullName
                            )
                        }
                    },
                    any = ::loadAccountInfo
                )
            }
    }

    private fun loadAccountInfo() {
        usersUseCase.get(
            userIds = null,
            fields = VkConstants.USER_FIELDS,
            nomCase = null
        ).listenValue { state ->
            state.processState(
                error = { error ->
                    // TODO: 12/07/2024, Danil Nikolaev: if local info is null then show error view
                },
                success = { response ->
                    val user = response.single()

                    screenState.setValue { old ->
                        old.copy(
                            avatarUrl = user.photo200,
                            fullName = user.fullName
                        )
                    }
                }
            )

            screenState.setValue { old -> old.copy(isLoading = state.isLoading()) }
        }
    }
}
