package com.meloda.fast.screens.updates.model

import com.meloda.fast.model.UpdateItem
import com.meloda.fast.screens.updates.UpdateState

data class UpdatesScreenState(
    val updateItem: UpdateItem?,
    val updateState: UpdateState,
    val error: String?,
) {

    companion object {
        val EMPTY = UpdatesScreenState(
            updateItem = null,
            updateState = UpdateState.NoUpdates,
            error = null
        )
    }
}
