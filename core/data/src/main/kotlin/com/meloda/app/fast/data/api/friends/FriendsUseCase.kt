package com.meloda.app.fast.data.api.friends

import com.meloda.app.fast.data.State
import com.meloda.app.fast.model.api.domain.VkUser
import kotlinx.coroutines.flow.Flow

interface FriendsUseCase {

    fun getFriends(
        count: Int?,
        offset: Int?
    ): Flow<State<List<VkUser>>>

    suspend fun storeUsers(users: List<VkUser>)
}
