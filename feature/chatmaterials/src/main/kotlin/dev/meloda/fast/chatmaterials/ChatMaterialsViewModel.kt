package dev.meloda.fast.chatmaterials

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.meloda.fast.chatmaterials.model.ChatMaterialsScreenState
import dev.meloda.fast.chatmaterials.navigation.ChatMaterials
import dev.meloda.fast.chatmaterials.util.asPresentation
import dev.meloda.fast.common.extensions.listenValue
import dev.meloda.fast.common.extensions.setValue
import dev.meloda.fast.data.processState
import dev.meloda.fast.domain.MessagesUseCase
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.model.api.domain.VkAttachmentHistoryMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

interface ChatMaterialsViewModel {
    val screenState: StateFlow<ChatMaterialsScreenState>
    val baseError: StateFlow<BaseError?>
    val imagesToPreload: StateFlow<List<String>>
    val currentOffset: StateFlow<Int>
    val canPaginate: StateFlow<Boolean>

    fun onMetPaginationCondition()

    fun onRefresh()

    fun onErrorConsumed()

    fun onTypeChanged(newType: String)
}

class ChatMaterialsViewModelImpl(
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

    override fun onMetPaginationCondition() {
        currentOffset.update { screenState.value.materials.size }
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

    private fun loadChatMaterials(
        offset: Int = currentOffset.value
    ) {
        messagesUseCase.getHistoryAttachments(
            peerId = screenState.value.peerId,
            count = LOAD_COUNT,
            offset = offset,
            attachmentTypes = listOf(screenState.value.attachmentType),
            conversationMessageId = screenState.value.conversationMessageId
        ).listenValue(viewModelScope) { state ->
            state.processState(
                error = { error ->

                },
                success = { response ->
                    val itemsCountSufficient = response.size == LOAD_COUNT
                    canPaginate.setValue { itemsCountSufficient }

                    val paginationExhausted = !itemsCountSufficient &&
                            screenState.value.materials.size >= LOAD_COUNT

                    val loadedMaterials = response.map(VkAttachmentHistoryMessage::asPresentation)

                    val newState = screenState.value.copy(
                        isPaginationExhausted = paginationExhausted
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

    companion object {
        const val LOAD_COUNT = 100
    }
}
