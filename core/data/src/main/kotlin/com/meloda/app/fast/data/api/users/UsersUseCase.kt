package dev.meloda.fast.data.api.users

import dev.meloda.fast.data.State
import dev.meloda.fast.model.api.domain.VkUser
import kotlinx.coroutines.flow.Flow

interface UsersUseCase {

    fun get(
        userIds: List<Int>?,
        fields: String?,
        nomCase: String?
    ): Flow<State<List<VkUser>>>

    fun getLocalUser(userId: Int): Flow<State<VkUser?>>

    suspend fun storeUser(user: VkUser)
    suspend fun storeUsers(users: List<VkUser>)
}
