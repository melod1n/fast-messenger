package com.meloda.app.fast.data.api.users

import com.meloda.app.fast.data.State
import com.meloda.app.fast.model.api.domain.VkUserDomain
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
