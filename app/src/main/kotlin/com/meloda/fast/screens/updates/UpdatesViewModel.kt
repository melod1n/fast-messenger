package com.meloda.fast.screens.updates

import androidx.lifecycle.MutableLiveData
import com.meloda.fast.data.ota.OtaApi
import com.meloda.fast.base.viewmodel.BaseViewModel
import com.meloda.fast.common.UpdateManager
import com.meloda.fast.ext.setIfNotEquals
import com.meloda.fast.model.UpdateItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import javax.inject.Inject


@HiltViewModel
class UpdatesViewModel @Inject constructor(
    private val updateManager: UpdateManager,
    private val otaApi: OtaApi
) : BaseViewModel() {

    val updateState = MutableLiveData(UpdateState.Loading)
    val currentError = MutableLiveData<String?>(null)
    val currentItem = MutableLiveData<UpdateItem?>(null)

    private var currentJob: Job? = null

    fun checkUpdates() {
        if (currentJob != null) {
            currentJob?.cancel()
            currentJob = null
        }
        updateState.setIfNotEquals(UpdateState.Loading)

        currentJob = updateManager.checkUpdates { item, error ->
            when {
                item != null -> {
                    currentError.setIfNotEquals(null)
                    updateState.setIfNotEquals(UpdateState.NewUpdate)
                }
                error != null -> {
                    currentError.setIfNotEquals(error.message ?: "")
                    updateState.setIfNotEquals(UpdateState.Error)
                }
                else -> {
                    currentError.setIfNotEquals(null)
                    updateState.setIfNotEquals(UpdateState.NoUpdates)
                }
            }
        }.apply { invokeOnCompletion { currentJob = null } }
    }
}