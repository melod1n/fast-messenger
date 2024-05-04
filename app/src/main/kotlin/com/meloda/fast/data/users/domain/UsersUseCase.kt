package com.meloda.fast.data.users.domain

import com.meloda.fast.api.model.domain.VkUserDomain
import com.meloda.fast.base.State
import kotlinx.coroutines.flow.Flow

interface UsersUseCase {

    fun getUserById(
        userId: Int,
        fields: String?,
        nomCase: String?
    ): Flow<State<VkUserDomain?>>

    fun getUsersByIds(
        userIds: List<Int>,
        fields: String?,
        nomCase: String?
    ): Flow<State<List<VkUserDomain>>>

    suspend fun storeUser(user: VkUserDomain)
    suspend fun storeUsers(users: List<VkUserDomain>)
}
