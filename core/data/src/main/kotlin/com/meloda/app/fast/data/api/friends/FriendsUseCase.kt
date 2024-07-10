package com.meloda.app.fast.data.api.friends

import com.meloda.app.fast.data.State
import com.meloda.app.fast.model.FriendsInfo
import com.meloda.app.fast.model.api.domain.VkUser
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
