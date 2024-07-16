package dev.meloda.fast.data.api.friends

import dev.meloda.fast.data.State
import dev.meloda.fast.model.FriendsInfo
import dev.meloda.fast.model.api.domain.VkUser
import kotlinx.coroutines.flow.Flow

interface FriendsUseCase {

    fun getAllFriends(
        count: Int?,
        offset: Int?
    ): Flow<State<FriendsInfo>>

    fun getFriends(
        count: Int?,
        offset: Int?
    ): Flow<State<List<VkUser>>>

    fun getOnlineFriends(
        count: Int?,
        offset: Int?
    ): Flow<State<List<Int>>>

    suspend fun storeUsers(users: List<VkUser>)
}
