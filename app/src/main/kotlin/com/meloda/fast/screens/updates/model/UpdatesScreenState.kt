package com.meloda.fast.screens.updates.model

import com.meloda.fast.model.UpdateItem
import com.meloda.fast.model.base.UiText

data class UpdatesScreenState(
    val title: UiText?,
    val subtitle: UiText?,
    val actionButtonText: UiText?,
    val actionButtonIcon: Int?,
    val updateItem: UpdateItem?,
    val updateState: UpdateState,
    val error: String?,
    val currentProgress: Float?,
    val isProgressIntermediate: Boolean,
    val currentDownloadProgress: Int
) {

    companion object {
        val EMPTY = UpdatesScreenState(
            title = null,
            subtitle = null,
            actionButtonText = null,
            actionButtonIcon = null,
            updateItem = null,
            updateState = UpdateState.Loading,
            error = null,
            currentProgress = null,
            isProgressIntermediate = true,
            currentDownloadProgress = 0,
        )
    }
}
