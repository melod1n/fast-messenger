package com.meloda.fast.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meloda.fast.database.model.VkUserDB

@Dao
interface UsersDao {

    @Query("SELECT * FROM users")
    suspend fun getAll(): List<VkUserDB>

    @Query("SELECT * FROM users WHERE id IN (:ids)")
    suspend fun getAllByIds(ids: List<Int>): List<VkUserDB>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<VkUserDB>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: VkUserDB)

    @Delete
    suspend fun delete(item: VkUserDB): Int

    @Delete
    suspend fun deleteAll(items: List<VkUserDB>): Int

    @Query("DELETE FROM users WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Int>): Int
}
