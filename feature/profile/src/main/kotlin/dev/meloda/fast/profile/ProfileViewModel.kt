package dev.meloda.fast.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.meloda.fast.common.VkConstants
import dev.meloda.fast.common.extensions.listenValue
import dev.meloda.fast.common.extensions.setValue
import dev.meloda.fast.data.State
import dev.meloda.fast.data.UserConfig
import dev.meloda.fast.data.processState
import dev.meloda.fast.domain.GetLocalUserByIdUseCase
import dev.meloda.fast.domain.LoadUserByIdUseCase
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.network.VkErrorCode
import dev.meloda.fast.profile.model.ProfileScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface ProfileViewModel {
    val screenState: StateFlow<ProfileScreenState>
    val baseError: StateFlow<BaseError?>
}

class ProfileViewModelImpl(
    private val getLocalUserByIdUseCase: GetLocalUserByIdUseCase,
    private val loadUserByIdUseCase: LoadUserByIdUseCase
) : ViewModel(), ProfileViewModel {

    override val screenState = MutableStateFlow(ProfileScreenState.EMPTY)
    override val baseError = MutableStateFlow<BaseError?>(null)

    init {
        getLocalAccountInfo()
    }

    private fun getLocalAccountInfo() {
        getLocalUserByIdUseCase(UserConfig.userId)
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
        loadUserByIdUseCase(
            userId = null,
            fields = VkConstants.USER_FIELDS,
            nomCase = null
        ).listenValue(viewModelScope) { state ->
            state.processState(
                error = { error ->
                    // TODO: 12/07/2024, Danil Nikolaev: if local info is null then show error view
                },
                success = { response ->
                    val user = requireNotNull(response)

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
