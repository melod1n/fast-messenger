package dev.meloda.fast.chatmaterials

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.meloda.fast.chatmaterials.model.ChatMaterialsScreenState
import dev.meloda.fast.chatmaterials.model.MaterialType
import dev.meloda.fast.chatmaterials.navigation.ChatMaterials
import dev.meloda.fast.chatmaterials.util.asPresentation
import dev.meloda.fast.common.extensions.listenValue
import dev.meloda.fast.common.extensions.setValue
import dev.meloda.fast.data.State
import dev.meloda.fast.data.processState
import dev.meloda.fast.domain.MessagesUseCase
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.model.api.domain.VkAttachmentHistoryMessage
import dev.meloda.fast.network.VkErrorCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

interface ChatMaterialsViewModel {
    val screenState: StateFlow<ChatMaterialsScreenState>
    val baseError: StateFlow<BaseError?>
    val imagesToPreload: StateFlow<List<String>>
    val currentOffset: StateFlow<Int>
    val canPaginate: StateFlow<Boolean>

    fun onPaginationConditionsMet()

    fun onRefresh()

    fun onErrorConsumed()

    fun onTypeChanged(newType: String)
}

class ChatMaterialsViewModelImpl(
    private val materialType: MaterialType,
    private val messagesUseCase: MessagesUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel(), ChatMaterialsViewModel {

    override val screenState = MutableStateFlow(ChatMaterialsScreenState.EMPTY)

    override val baseError = MutableStateFlow<BaseError?>(null)
    override val imagesToPreload = MutableStateFlow<List<String>>(emptyList())
    override val currentOffset = MutableStateFlow(0)
    override val canPaginate = MutableStateFlow(false)

    init {
        val arguments = ChatMaterials.from(savedStateHandle)

        screenState.setValue { old ->
            old.copy(
                peerId = arguments.peerId,
                conversationMessageId = arguments.conversationMessageId
            )
        }

        loadChatMaterials()
    }

    override fun onPaginationConditionsMet() {
        currentOffset.setValue { old -> old + LOAD_COUNT }
        loadChatMaterials()
    }

    override fun onRefresh() {
        loadChatMaterials(offset = 0)
    }

    override fun onErrorConsumed() {
        baseError.setValue { null }
    }

    override fun onTypeChanged(newType: String) {
        screenState.setValue { old -> old.copy(attachmentType = newType) }
        loadChatMaterials(0)
    }

    private fun loadChatMaterials(offset: Int = currentOffset.value) {
        messagesUseCase.getHistoryAttachments(
            peerId = screenState.value.peerId,
            count = LOAD_COUNT,
            offset = offset,
            attachmentTypes = listOf(materialType.toString()),
            conversationMessageId = screenState.value.conversationMessageId
        ).listenValue(viewModelScope) { state ->
            state.processState(
                error = ::handleError,
                success = { response ->
                    val itemsCountSufficient = response.size == LOAD_COUNT
                    canPaginate.setValue { itemsCountSufficient }

                    val paginationExhausted = !itemsCountSufficient
                            && screenState.value.materials.isNotEmpty()

                    val loadedMaterials = response.mapNotNull(VkAttachmentHistoryMessage::asPresentation)

                    val newState = screenState.value.copy(
                        isPaginationExhausted = paginationExhausted,
                        conversationMessageId = if (loadedMaterials.size + offset > 200) {
                            currentOffset.setValue { 0 }
                            loadedMaterials.lastOrNull()?.conversationMessageId ?: -1
                        } else {
                            screenState.value.conversationMessageId
                        }
                    )

                    if (offset == 0) {
                        screenState.setValue {
                            newState.copy(materials = loadedMaterials)
                        }
                    } else {
                        screenState.setValue {
                            newState.copy(
                                materials = newState.materials.plus(loadedMaterials)
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
        const val LOAD_COUNT = 200
    }
}
