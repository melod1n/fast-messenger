package com.meloda.fast.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.meloda.fast.database.model.VkGroupDB

@Dao
abstract class GroupsDao : BaseDao<VkGroupDB> {

    @Query("SELECT * FROM groups")
    abstract suspend fun getAll(): List<VkGroupDB>

    @Query("SELECT * FROM groups WHERE id IN (:ids)")
    abstract suspend fun getAllByIds(ids: List<Int>): List<VkGroupDB>

    @Query("DELETE FROM groups WHERE id IN (:ids)")
    abstract suspend fun deleteByIds(ids: List<Int>): Int
}
