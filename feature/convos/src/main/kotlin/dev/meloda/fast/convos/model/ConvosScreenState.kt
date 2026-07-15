package dev.meloda.fast.convos.model

import androidx.compose.runtime.Immutable
import dev.meloda.fast.model.BaseError
import dev.meloda.fast.ui.model.vk.UiConvo
import dev.meloda.fast.common.ImmutableList
import dev.meloda.fast.common.emptyImmutableList

@Immutable
data class ConvosScreenState(
    val isLoading: Boolean,
    val isPaginating: Boolean,
    val isPaginationExhausted: Boolean,
    val profileImageUrl: String?,
    val scrollIndex: Int,
    val scrollOffset: Int,
    val canPaginate: Boolean,
    val expandedConvoId: Long?,
    val convos: ImmutableList<UiConvo>,
    val dialog: ConvoDialog?,

    // TODO: 30.05.2026, Danil Nikolaev: remove
    val error: BaseError?
) {

    companion object {
        val EMPTY: ConvosScreenState = ConvosScreenState(
            isLoading = true,
            isPaginating = false,
            isPaginationExhausted = false,
            profileImageUrl = null,
            scrollIndex = 0,
            scrollOffset = 0,
            canPaginate = false,
            expandedConvoId = null,
            convos = emptyImmutableList(),
            dialog = null,
            error = null
        )
    }
}
