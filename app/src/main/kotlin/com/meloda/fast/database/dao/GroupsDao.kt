package com.meloda.fast.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meloda.fast.database.model.VkGroupDB

@Dao
interface GroupsDao {

    @Query("SELECT * FROM groups")
    suspend fun getAll(): List<VkGroupDB>

    @Query("SELECT * FROM groups WHERE id IN (:ids)")
    suspend fun getAllByIds(ids: List<Int>): List<VkGroupDB>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<VkGroupDB>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: VkGroupDB)

    @Delete
    suspend fun delete(item: VkGroupDB): Int

    @Delete
    suspend fun deleteAll(items: List<VkGroupDB>): Int

    @Query("DELETE FROM groups WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Int>): Int
}
