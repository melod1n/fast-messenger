package dev.meloda.fast.domain

import dev.meloda.fast.data.State
import dev.meloda.fast.data.api.conversations.ConversationsRepository
import dev.meloda.fast.data.mapToState
import dev.meloda.fast.model.api.domain.VkConversation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class ConversationsUseCaseImpl(
    private val repository: ConversationsRepository,
) : ConversationsUseCase {

    //    override fun getConversations(
//        count: Int?,
//        offset: Int?,
//        fields: String,
//        filter: String,
//        extended: Boolean?,
//        startMessageId: Int?
//    ): Flow<dev.meloda.fast.network.State<ConversationsResponseDomain>> = flow {
//        emit(dev.meloda.fast.network.State.Loading)
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
//            onSuccess = { response -> dev.meloda.fast.network.State.Success(response.toDomain()) },
//            onNetworkFailure = { dev.meloda.fast.network.State.Error.ConnectionError },
//            onUnknownFailure = { dev.meloda.fast.network.State.UNKNOWN_ERROR },
//            onHttpFailure = { result -> result.error.toStateApiError() },
//            onApiFailure = { result -> result.error.toStateApiError() }
//        )
//        emit(newState)
//    }
//

    //
//    override fun pin(peerId: Int): Flow<dev.meloda.fast.network.State<Unit>> = flow {
//        emit(dev.meloda.fast.network.State.Loading)
//
//        val newState = conversationsRepository.pin(
//            ConversationsPinRequest(peerId = peerId)
//        ).fold(
//            onSuccess = { dev.meloda.fast.network.State.Success(Unit) },
//            onNetworkFailure = { dev.meloda.fast.network.State.Error.ConnectionError },
//            onUnknownFailure = { dev.meloda.fast.network.State.UNKNOWN_ERROR },
//            onHttpFailure = { result -> result.error.toStateApiError() },
//            onApiFailure = { result -> result.error.toStateApiError() }
//        )
//        emit(newState)
//    }
//
//    override fun unpin(peerId: Int): Flow<dev.meloda.fast.network.State<Unit>> = flow {
//        emit(dev.meloda.fast.network.State.Loading)
//
//        val newState = conversationsRepository.unpin(
//            ConversationsUnpinRequest(peerId = peerId)
//        ).fold(
//            onSuccess = { dev.meloda.fast.network.State.Success(Unit) },
//            onNetworkFailure = { dev.meloda.fast.network.State.Error.ConnectionError },
//            onUnknownFailure = { dev.meloda.fast.network.State.UNKNOWN_ERROR },
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

        val newState = repository.getConversations(count, offset).mapToState()
        emit(newState)
    }

    override suspend fun storeConversations(
        conversations: List<VkConversation>
    ) = withContext(Dispatchers.IO) {
        repository.storeConversations(conversations)
    }

    override fun delete(peerId: Int): Flow<State<Int>> = flow {
        emit(State.Loading)

        val newState = repository.delete(peerId = peerId).mapToState()
        emit(newState)
    }

    override fun changePinState(peerId: Int, pin: Boolean): Flow<State<Int>> = flow {
        emit(State.Loading)

        val newState = if (pin) {
            repository.pin(peerId)
        } else {
            repository.unpin(peerId)
        }.mapToState()

        emit(newState)
    }
}
