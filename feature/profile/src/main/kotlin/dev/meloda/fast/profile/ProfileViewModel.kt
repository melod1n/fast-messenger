package dev.meloda.fast.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.meloda.fast.common.VkConstants
import dev.meloda.fast.common.extensions.listenValue
import dev.meloda.fast.common.extensions.setValue
import dev.meloda.fast.data.UserConfig
import dev.meloda.fast.data.processState
import dev.meloda.fast.domain.GetLocalUserByIdUseCase
import dev.meloda.fast.domain.LoadUserByIdUseCase
import dev.meloda.fast.logger.FastLogger
import dev.meloda.fast.profile.model.ProfileScreenState
import dev.meloda.fast.profile.navigation.Profile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel(
    private val getLocalUserByIdUseCase: GetLocalUserByIdUseCase,
    private val loadUserByIdUseCase: LoadUserByIdUseCase,
    private val logger: FastLogger,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val targetUserId: Long = runCatching {
        Profile.from(savedStateHandle).userId
    }.getOrNull() ?: UserConfig.userId

    val isCurrentUser: Boolean = (targetUserId == UserConfig.userId)

    private val screenState = MutableStateFlow(
        ProfileScreenState(isLoading = true, isCurrentUser = isCurrentUser)
    )
    val screenStateFlow get() = screenState.asStateFlow()

    init {
        loadProfile()
    }

    fun onRefresh() {
        fetchRemoteProfile()
    }

    private fun loadProfile() {
        logger.debug("ProfileViewModel", "loadProfile: targetUserId=$targetUserId, isCurrentUser=$isCurrentUser")
        emit(screenState.value.copy(isLoading = true, isCurrentUser = isCurrentUser, isError = false))

        getLocalUserByIdUseCase(targetUserId).listenValue { state ->
            logger.debug("ProfileViewModel", "Local profile loaded: $state")

            state.processState(
                error = { _ -> },
                success = { user ->
                    if (user != null) {
                        emit(
                            screenState.value.copy(
                                user = user,
                                isCurrentUser = isCurrentUser,
                                isLoading = false
                            )
                        )
                    }
                },
                any = ::fetchRemoteProfile
            )
        }
    }

    private fun emit(state: ProfileScreenState) {
        screenState.setValue { state }
    }

    private fun fetchRemoteProfile() {
        loadUserByIdUseCase(
            userId = if (isCurrentUser) null else targetUserId,
            fields = VkConstants.USER_FIELDS,
            nomCase = null
        ).listenValue(viewModelScope) { state ->
            state.processState(
                error = { err ->
                    logger.debug("ProfileViewModel", "fetchRemoteProfile error: $err")
                    if (screenState.value.user == null) {
                        screenState.setValue { it.copy(isLoading = false, isError = true) }
                    } else {
                        screenState.setValue { it.copy(isLoading = false) }
                    }
                },
                success = { response ->
                    val user = response
                    if (user != null) {
                        screenState.setValue { old ->
                            old.copy(
                                user = user,
                                isCurrentUser = isCurrentUser,
                                isLoading = false,
                                isError = false
                            )
                        }
                    }
                }
            )
        }
    }
}
