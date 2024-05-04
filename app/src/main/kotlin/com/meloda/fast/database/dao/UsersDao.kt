package com.meloda.fast.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.meloda.fast.database.model.VkUserDB

@Dao
abstract class UsersDao : BaseDao<VkUserDB> {

    @Query("SELECT * FROM users")
    abstract suspend fun getAll(): List<VkUserDB>

    @Query("SELECT * FROM users WHERE id IN (:ids)")
    abstract suspend fun getAllByIds(ids: List<Int>): List<VkUserDB>

    @Query("DELETE FROM users WHERE id IN (:ids)")
    abstract suspend fun deleteByIds(ids: List<Int>): Int
}
