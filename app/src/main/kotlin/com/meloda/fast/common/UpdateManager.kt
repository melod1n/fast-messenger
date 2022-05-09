package com.meloda.fast.common

import androidx.lifecycle.MutableLiveData
import com.meloda.fast.BuildConfig
import com.meloda.fast.api.base.ApiResponse
import com.meloda.fast.api.network.Answer
import com.meloda.fast.api.network.ota.OtaGetLatestReleaseResponse
import com.meloda.fast.api.network.ota.OtaRepo
import com.meloda.fast.extensions.setIfNotEquals
import com.meloda.fast.model.UpdateActualUrl
import com.meloda.fast.model.UpdateItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import kotlin.coroutines.CoroutineContext

class UpdateManager(private val repo: OtaRepo) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    companion object {
        val newUpdate = MutableLiveData<UpdateItem?>(null)
        val updateError = MutableLiveData<Throwable?>(null)

        var otaBaseUrl: String? = null
            private set
    }

    private var listener: ((item: UpdateItem?, error: Throwable?) -> Unit)? = null

    private fun getActualUrl() = launch {
        val job: suspend () -> Answer<UpdateActualUrl> = { repo.getActualUrl() }

        when (val jobResponse = job()) {
            is Answer.Success -> {
                val item = jobResponse.data
                otaBaseUrl = item.url

                getLatestRelease()
            }
            is Answer.Error -> {
                otaBaseUrl = null
                val throwable = jobResponse.throwable
                listener?.invoke(null, throwable)

                withContext(Dispatchers.Main) {
                    updateError.setIfNotEquals(throwable)
                }
            }
        }
    }

    private fun getLatestRelease() = launch {
        val url = "$otaBaseUrl/releases-latest"

        val job: suspend () -> Answer<ApiResponse<OtaGetLatestReleaseResponse>> = {
            repo.getLatestRelease(url = url, secretCode = getOtaSecret())
        }

        withContext(Dispatchers.Main) {
            when (val jobResponse = job()) {
                is Answer.Success -> {
                    val response = jobResponse.data.response ?: return@withContext
                    val latestRelease = response.release

                    if (latestRelease != null &&
                        (AppGlobal.versionName
                            .split("_")
                            .getOrNull(1) != latestRelease.versionName ||
                                AppGlobal.versionCode < latestRelease.versionCode)
                    ) {
                        newUpdate.setIfNotEquals(latestRelease)
                        listener?.invoke(latestRelease, null)
                    } else {
                        newUpdate.setIfNotEquals(null)
                        listener?.invoke(null, null)
                    }
                }

                is Answer.Error -> {
                    val throwable = jobResponse.throwable
                    updateError.setIfNotEquals(throwable)
                    listener?.invoke(null, throwable)
                }
            }
        }
    }

    private fun getOtaSecret(): String {
        return URLEncoder.encode(BuildConfig.otaSecretCode, "utf-8")
    }

    fun checkUpdates(block: ((item: UpdateItem?, error: Throwable?) -> Unit)? = null) = launch {
        this@UpdateManager.listener = block
        getActualUrl()
    }
}