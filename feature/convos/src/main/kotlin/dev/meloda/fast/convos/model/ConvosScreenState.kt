package dev.meloda.fast.convos.model

import androidx.compose.runtime.Immutable

@Immutable
data class ConvosScreenState(
    val isLoading: Boolean,
    val isPaginating: Boolean,
    val isPaginationExhausted: Boolean,
    val profileImageUrl: String?,
    val scrollIndex: Int,
    val scrollOffset: Int,
    val isArchive: Boolean
) {

    companion object {
        val EMPTY: ConvosScreenState = ConvosScreenState(
            isLoading = true,
            isPaginating = false,
            isPaginationExhausted = false,
            profileImageUrl = null,
            scrollIndex = 0,
            scrollOffset = 0,
            isArchive = false
        )
    }
}
