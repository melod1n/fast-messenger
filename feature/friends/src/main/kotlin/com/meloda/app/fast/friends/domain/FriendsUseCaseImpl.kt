package com.meloda.app.fast.friends.domain

import com.meloda.app.fast.data.State
import com.meloda.app.fast.data.api.friends.FriendsRepository
import com.meloda.app.fast.data.api.friends.FriendsUseCase
import com.meloda.app.fast.data.mapToState
import com.meloda.app.fast.model.api.domain.VkUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FriendsUseCaseImpl(private val repository: FriendsRepository) : FriendsUseCase {

    override fun getFriends(
        count: Int?, offset: Int?
    ): Flow<State<List<VkUser>>> = flow {
        emit(State.Loading)

        val newState = repository.getFriends(count, offset).mapToState()
        emit(newState)
    }

    override suspend fun storeUsers(users: List<VkUser>) {
        repository.storeUsers(users)
    }
}
