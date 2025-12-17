package dev.meloda.fast.data.api.convos

import com.slack.eithernet.ApiResult
import dev.meloda.fast.model.ConvosFilter
import dev.meloda.fast.model.api.domain.VkConvo
import dev.meloda.fast.network.RestApiErrorDomain

interface ConvosRepository {

    suspend fun storeConvos(convos: List<VkConvo>)

    suspend fun getConvos(
        count: Int?,
        offset: Int?,
        filter: ConvosFilter
    ): ApiResult<List<VkConvo>, RestApiErrorDomain>

    suspend fun getConvosById(
        peerIds: List<Long>,
        extended: Boolean? = null,
        fields: String? = null
    ): ApiResult<List<VkConvo>, RestApiErrorDomain>

    suspend fun delete(peerId: Long): ApiResult<Long, RestApiErrorDomain>
    suspend fun pin(peerId: Long): ApiResult<Int, RestApiErrorDomain>
    suspend fun unpin(peerId: Long): ApiResult<Int, RestApiErrorDomain>
    suspend fun reorderPinned(peerIds: List<Long>): ApiResult<Int, RestApiErrorDomain>
    suspend fun archive(peerId: Long): ApiResult<Int, RestApiErrorDomain>
    suspend fun unarchive(peerId: Long): ApiResult<Int, RestApiErrorDomain>
}
