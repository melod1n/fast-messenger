package com.meloda.app.fast.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.meloda.app.fast.model.database.VkGroupEntity

@Dao
abstract class GroupDao : EntityDao<VkGroupEntity> {

    @Query("SELECT * FROM groups")
    abstract suspend fun getAll(): List<VkGroupEntity>

    @Query("SELECT * FROM groups WHERE id IN (:ids)")
    abstract suspend fun getAllByIds(ids: List<Int>): List<VkGroupEntity>

    @Query("DELETE FROM groups WHERE id IN (:ids)")
    abstract suspend fun deleteByIds(ids: List<Int>): Int
}
