package com.meloda.app.fast.data.api.users

import com.meloda.app.fast.data.State
import com.meloda.app.fast.model.api.domain.VkUser
import kotlinx.coroutines.flow.Flow

interface UsersUseCase {

    fun getUserById(
        userId: Int,
        fields: String?,
        nomCase: String?
    ): Flow<State<VkUser?>>

    fun getUsersByIds(
        userIds: List<Int>,
        fields: String?,
        nomCase: String?
    ): Flow<State<List<VkUser>>>

    suspend fun storeUser(user: VkUser)
    suspend fun storeUsers(users: List<VkUser>)
}
