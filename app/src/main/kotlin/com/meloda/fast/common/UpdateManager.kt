package com.meloda.fast.common

import com.meloda.fast.BuildConfig
import com.meloda.fast.api.base.ApiResponse
import com.meloda.fast.api.network.ApiAnswer
import com.meloda.fast.api.network.ota.OtaGetLatestReleaseResponse
import com.meloda.fast.data.ota.OtaApi
import com.meloda.fast.model.UpdateActualUrl
import com.meloda.fast.model.UpdateItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLEncoder
import kotlin.coroutines.CoroutineContext

interface UpdateManager {
    val stateFlow: Flow<UpdateManagerState>

    fun checkUpdates(): Job
}

class UpdateManagerImpl(private val repo: OtaApi) : UpdateManager {

    private val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    private val coroutineScope = CoroutineScope(coroutineContext)

    private var otaBaseUrl: String? = null

    override val stateFlow = MutableStateFlow(UpdateManagerState.EMPTY)

    override fun checkUpdates() = coroutineScope.launch {
        val job: suspend () -> ApiAnswer<UpdateActualUrl> = { repo.getActualUrl() }

        when (val jobResponse = job()) {
            is ApiAnswer.Success -> {
                val item = jobResponse.data
                otaBaseUrl = item.url

                getLatestRelease()
            }
            is ApiAnswer.Error -> {
                otaBaseUrl = null
                val throwable = jobResponse.error.throwable

                val newForm = stateFlow.value.copy(
                    updateItem = null,
                    throwable = throwable
                )
                stateFlow.update { newForm }
            }
        }
    }

    private fun getLatestRelease() = coroutineScope.launch {
        val url = "$otaBaseUrl/releases-latest"

        val job: suspend () -> ApiAnswer<ApiResponse<OtaGetLatestReleaseResponse>> = {
            repo.getLatestRelease(url = url, secretCode = getOtaSecret())
        }

        when (val jobResponse = job()) {
            is ApiAnswer.Success -> {
                val response = jobResponse.data.response ?: return@launch
                val latestRelease = response.release

                val updateItem = if (latestRelease != null &&
                    (AppGlobal.versionName
                        .split("_")
                        .getOrNull(1) != latestRelease.versionName ||
                            AppGlobal.versionCode < latestRelease.versionCode)
                ) {
                    latestRelease
                } else {
                    null
                }

                val newForm = stateFlow.value.copy(
                    updateItem = updateItem,
                    throwable = null
                )

                stateFlow.update { newForm }
            }

            is ApiAnswer.Error -> {
                val throwable = jobResponse.error.throwable

                val newForm = stateFlow.value.copy(
                    updateItem = null,
                    throwable = throwable
                )
                stateFlow.update { newForm }
            }
        }
    }

    private fun getOtaSecret(): String {
        return URLEncoder.encode(BuildConfig.otaSecretCode, "utf-8")
    }
}

data class UpdateManagerState(
    val updateItem: UpdateItem?,
    val throwable: Throwable?,
) {
    companion object {
        val EMPTY = UpdateManagerState(
            updateItem = null, throwable = null
        )
    }
}
