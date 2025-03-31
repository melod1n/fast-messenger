package dev.meloda.fast.domain

import dev.meloda.fast.data.State
import dev.meloda.fast.model.FriendsInfo
import dev.meloda.fast.model.api.domain.VkUser
import kotlinx.coroutines.flow.Flow

interface FriendsUseCase {

    fun getAllFriends(
        order: String = "hints",
        count: Int?,
        offset: Int?
    ): Flow<State<FriendsInfo>>

    fun getFriends(
        order: String = "hints",
        count: Int?,
        offset: Int?
    ): Flow<State<List<VkUser>>>

    fun getOnlineFriends(
        count: Int?,
        offset: Int?
    ): Flow<State<List<Long>>>

    suspend fun storeUsers(users: List<VkUser>)
}
