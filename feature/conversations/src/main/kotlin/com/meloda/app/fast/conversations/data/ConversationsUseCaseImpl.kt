package com.meloda.app.fast.conversations.data

import com.meloda.app.fast.conversations.domain.ConversationsUseCase
import com.meloda.app.fast.data.State
import com.meloda.app.fast.data.api.conversations.ConversationsRepository
import com.meloda.app.fast.data.toStateApiError
import com.meloda.app.fast.model.api.domain.VkConversation
import com.slack.eithernet.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class ConversationsUseCaseImpl(
    private val conversationsRepository: ConversationsRepository,
) : ConversationsUseCase {

    //    override fun getConversations(
//        count: Int?,
//        offset: Int?,
//        fields: String,
//        filter: String,
//        extended: Boolean?,
//        startMessageId: Int?
//    ): Flow<com.meloda.app.fast.network.State<ConversationsResponseDomain>> = flow {
//        emit(com.meloda.app.fast.network.State.Loading)
//
//        val newState = conversationsRepository.getConversations(
//            params = ConversationsGetRequest(
//                count = count,
//                offset = offset,
//                fields = fields,
//                filter = filter,
//                extended = extended,
//                startMessageId = startMessageId
//            )
//        ).fold(
//            onSuccess = { response -> com.meloda.app.fast.network.State.Success(response.toDomain()) },
//            onNetworkFailure = { com.meloda.app.fast.network.State.Error.ConnectionError },
//            onUnknownFailure = { com.meloda.app.fast.network.State.UNKNOWN_ERROR },
//            onHttpFailure = { result -> result.error.toStateApiError() },
//            onApiFailure = { result -> result.error.toStateApiError() }
//        )
//        emit(newState)
//    }
//
//    override fun delete(peerId: Int): Flow<com.meloda.app.fast.network.State<Unit>> = flow {
//        emit(com.meloda.app.fast.network.State.Loading)
//
//        val newState = conversationsRepository.delete(
//            ConversationsDeleteRequest(peerId = peerId)
//        ).fold(
//            onSuccess = { com.meloda.app.fast.network.State.Success(Unit) },
//            onNetworkFailure = { com.meloda.app.fast.network.State.Error.ConnectionError },
//            onUnknownFailure = { com.meloda.app.fast.network.State.UNKNOWN_ERROR },
//            onHttpFailure = { result -> result.error.toStateApiError() },
//            onApiFailure = { result -> result.error.toStateApiError() }
//        )
//        emit(newState)
//    }
//
//    override fun pin(peerId: Int): Flow<com.meloda.app.fast.network.State<Unit>> = flow {
//        emit(com.meloda.app.fast.network.State.Loading)
//
//        val newState = conversationsRepository.pin(
//            ConversationsPinRequest(peerId = peerId)
//        ).fold(
//            onSuccess = { com.meloda.app.fast.network.State.Success(Unit) },
//            onNetworkFailure = { com.meloda.app.fast.network.State.Error.ConnectionError },
//            onUnknownFailure = { com.meloda.app.fast.network.State.UNKNOWN_ERROR },
//            onHttpFailure = { result -> result.error.toStateApiError() },
//            onApiFailure = { result -> result.error.toStateApiError() }
//        )
//        emit(newState)
//    }
//
//    override fun unpin(peerId: Int): Flow<com.meloda.app.fast.network.State<Unit>> = flow {
//        emit(com.meloda.app.fast.network.State.Loading)
//
//        val newState = conversationsRepository.unpin(
//            ConversationsUnpinRequest(peerId = peerId)
//        ).fold(
//            onSuccess = { com.meloda.app.fast.network.State.Success(Unit) },
//            onNetworkFailure = { com.meloda.app.fast.network.State.Error.ConnectionError },
//            onUnknownFailure = { com.meloda.app.fast.network.State.UNKNOWN_ERROR },
//            onHttpFailure = { result -> result.error.toStateApiError() },
//            onApiFailure = { result -> result.error.toStateApiError() }
//        )
//        emit(newState)
//    }
//
//    override suspend fun storeConversations(conversations: List<VkConversationDomain>) {
//        conversationsDao.insertAll(conversations.map(VkConversationDomain::mapToDb))
//    }
//
//    override suspend fun storeGroups(groups: List<VkGroupDomain>) {
//        groupsDao.insertAll(groups.map(VkGroupDomain::mapToDB))
//    }
    override fun getConversations(
        count: Int?,
        offset: Int?
    ): Flow<State<List<VkConversation>>> = flow {
        emit(State.Loading)

        val newState = when (
            val result = conversationsRepository.getConversations(
                count = count,
                offset = offset
            )
        ) {
            is ApiResult.Success -> State.Success(result.value)

            is ApiResult.Failure.NetworkFailure -> State.Error.ConnectionError
            is ApiResult.Failure.UnknownFailure -> State.UNKNOWN_ERROR
            is ApiResult.Failure.HttpFailure -> result.error.toStateApiError()
            is ApiResult.Failure.ApiFailure -> result.error.toStateApiError()
        }
        emit(newState)
    }

    override suspend fun storeConversations(
        conversations: List<VkConversation>
    ) = withContext(Dispatchers.IO) {
        conversationsRepository.storeConversations(conversations)
    }
}
