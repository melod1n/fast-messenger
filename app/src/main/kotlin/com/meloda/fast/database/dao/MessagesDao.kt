package com.meloda.fast.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.meloda.fast.database.model.VkMessageDB

@Dao
abstract class MessagesDao : BaseDao<VkMessageDB> {

    @Query("SELECT * FROM messages")
    abstract suspend fun getAll(): List<VkMessageDB>

    @Query("SELECT * FROM messages WHERE id IN (:ids)")
    abstract suspend fun getAllByIds(ids: List<Int>): List<VkMessageDB>

    @Query("DELETE FROM messages WHERE id IN (:ids)")
    abstract suspend fun deleteByIds(ids: List<Int>): Int
}
