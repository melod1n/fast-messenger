package dev.meloda.fast.convos.model

import androidx.compose.runtime.Immutable
import dev.meloda.fast.common.ImmutableList
import dev.meloda.fast.common.emptyImmutableList
import dev.meloda.fast.model.BaseError

@Immutable
data class CreateChatScreenState(
    val isLoading: Boolean,
    val isPaginating: Boolean,
    val isPaginationExhausted: Boolean,
    val friends: ImmutableList<SelectableUiFriend>,
    val chatTitle: String,
    val finalChatTitle: String,
    val showConfirmDialog: Boolean,
    val error: BaseError?,
    val canPaginate: Boolean
) {
    companion object {
        val EMPTY: CreateChatScreenState = CreateChatScreenState(
            isLoading = true,
            isPaginating = false,
            isPaginationExhausted = false,
            friends = emptyImmutableList(),
            chatTitle = "",
            finalChatTitle = "",
            showConfirmDialog = false,
            error = null,
            canPaginate = false
        )
    }
}
