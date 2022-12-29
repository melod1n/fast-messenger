package com.meloda.fast.screens.updates

import com.meloda.fast.base.viewmodel.BaseViewModel
import com.meloda.fast.common.UpdateManager
import com.meloda.fast.data.ota.OtaApi
import com.meloda.fast.model.UpdateItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject


@HiltViewModel
class UpdatesViewModel @Inject constructor(
    private val updateManager: UpdateManager,
    private val otaApi: OtaApi,
) : BaseViewModel() {

    val updateState: MutableStateFlow<UpdateState> = MutableStateFlow(UpdateState.Loading)
    val currentError: MutableStateFlow<String?> = MutableStateFlow(null)
    val currentItem: MutableStateFlow<UpdateItem?> = MutableStateFlow(null)

    private var currentJob: Job? = null

    fun checkUpdates() {
        if (currentJob != null) {
            currentJob?.cancel()
            currentJob = null
        }
        updateState.update { UpdateState.Loading }

        currentJob = updateManager.checkUpdates { item, error ->
            when {
                item != null -> {
                    currentError.update { null }
                    updateState.update { UpdateState.NewUpdate }
                }
                error != null -> {
                    currentError.update { error.message ?: "" }
                    updateState.update { UpdateState.Error }
                }
                else -> {
                    currentError.update { null }
                    updateState.update { UpdateState.NoUpdates }
                }
            }
        }.apply { invokeOnCompletion { currentJob = null } }
    }
}
