package dev.meloda.fast.profile

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel(
    private val getLocalUserByIdUseCase: GetLocalUserByIdUseCase,
    private val loadUserByIdUseCase: LoadUserByIdUseCase,
    private val logger: FastLogger
) : ViewModel() {

    private val screenState = MutableStateFlow(ProfileScreenState.EMPTY)
    val screenStateFlow get() = screenState.asStateFlow()

    init {
        getLocalAccountInfo()
    }

    private fun getLocalAccountInfo() {
        logger.debug(this@ProfileViewModel::class, "START")
        emit(screenState.value.copy(isLoading = true))

        getLocalUserByIdUseCase(UserConfig.userId).listenValue { state ->
            logger.debug(this@ProfileViewModel::class, "LOADED: $state")

            emit(screenState.value.copy(isLoading = false))

            state.processState(
                error = {
                    logger.debug(this@ProfileViewModel::class, "ERROR")
                    emit(screenState.value.copy(avatarUrl = null, fullName = null))
                },
                success = { user ->
                    logger.debug(this@ProfileViewModel::class, "SUCCESS")
                    emit(
                        screenState.value.copy(
                            avatarUrl = user?.photo200,
                            fullName = user?.fullName
                        )
                    )
                },
                any = ::loadAccountInfo
            )
        }
    }

    private fun emit(state: ProfileScreenState) {
        screenState.setValue { state }
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
