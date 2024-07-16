package dev.meloda.fast.data.api.account

import android.os.Build
import dev.meloda.fast.model.api.asInt
import dev.meloda.fast.network.RestApiErrorDomain
import dev.meloda.fast.network.mapApiDefault
import dev.meloda.fast.network.service.account.AccountService
import com.slack.eithernet.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AccountRepositoryImpl(
    private val service: AccountService
) : AccountRepository {

    override suspend fun setOnline(
        accessToken: String?,
        voip: Boolean
    ): ApiResult<Int, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        service.setOnline(
            mutableMapOf(
                "voip" to voip.asInt().toString()
            ).apply {
                accessToken?.let { this["access_token"] = it }
            }
        ).mapApiDefault()
    }

    override suspend fun setOffline(
        accessToken: String?
    ): ApiResult<Int, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        service.setOffline(
            accessToken?.let { mapOf("access_token" to it) } ?: emptyMap()
        ).mapApiDefault()
    }

    override suspend fun registerDevice(
        token: String,
        deviceId: String
    ): ApiResult<Int, RestApiErrorDomain> = withContext(Dispatchers.IO) {
        service.registerDevice(
            mapOf(
                "token" to token,
                "pushes_granted" to "1",
                "app_version" to "15271",
                "push_provider" to "fcm",
                "companion_apps" to "vk_client",
                "type" to "4",
                "device_id" to deviceId,
                "system_version" to Build.VERSION.RELEASE
            )
        ).mapApiDefault()
    }
}
