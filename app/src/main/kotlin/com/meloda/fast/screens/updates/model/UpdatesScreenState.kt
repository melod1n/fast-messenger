package com.meloda.fast.screens.updates.model

import com.meloda.fast.model.UpdateItem

data class UpdatesScreenState(
    val updateItem: UpdateItem?,
    val updateState: UpdateState,
    val error: String?,
    val currentProgress: Float?,
    val isProgressIntermediate: Boolean,
) {

    companion object {
        val EMPTY = UpdatesScreenState(
            updateItem = null,
            updateState = UpdateState.NoUpdates,
            error = null,
            currentProgress = null,
            isProgressIntermediate = true
        )
    }
}
