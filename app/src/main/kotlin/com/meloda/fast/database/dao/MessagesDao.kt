package com.meloda.fast.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meloda.fast.database.model.VkMessageDB

@Dao
interface MessagesDao {

    @Query("SELECT * FROM messages")
    suspend fun getAll(): List<VkMessageDB>

    @Query("SELECT * FROM messages WHERE id IN (:ids)")
    suspend fun getAllByIds(ids: List<Int>): List<VkMessageDB>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<VkMessageDB>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: VkMessageDB)

    @Delete
    suspend fun delete(item: VkMessageDB): Int

    @Delete
    suspend fun deleteAll(items: List<VkMessageDB>): Int

    @Query("DELETE FROM messages WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Int>): Int
}
