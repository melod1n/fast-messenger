package dev.meloda.fast.database.dao

import androidx.room.Dao
import androidx.room.Query
import dev.meloda.fast.model.database.VkUserEntity

@Dao
abstract class UsersDao : EntityDao<VkUserEntity> {

    @Query("SELECT * FROM users")
    abstract suspend fun getAll(): List<VkUserEntity>

    @Query("SELECT * FROM users WHERE id IN (:ids)")
    abstract suspend fun getAllByIds(ids: List<Int>): List<VkUserEntity>

    @Query("DELETE FROM users WHERE id IN (:ids)")
    abstract suspend fun deleteByIds(ids: List<Int>): Int
}
