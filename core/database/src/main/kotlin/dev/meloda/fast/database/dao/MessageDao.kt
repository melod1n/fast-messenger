package dev.meloda.fast.database.dao

import androidx.room.Dao
import androidx.room.Query
import dev.meloda.fast.model.database.VkMessageEntity

@Dao
abstract class MessageDao : EntityDao<VkMessageEntity> {

    @Query("SELECT * FROM messages")
    abstract suspend fun getAll(): List<VkMessageEntity>

    @Query("SELECT * FROM messages WHERE peerId IS (:convoId)")
    abstract suspend fun getAll(convoId: Long): List<VkMessageEntity>

    @Query("SELECT * FROM messages WHERE id IN (:ids)")
    abstract suspend fun getAllByIds(ids: List<Int>): List<VkMessageEntity>

    @Query("SELECT * FROM messages WHERE id IS (:messageId)")
    abstract suspend fun getById(messageId: Long): VkMessageEntity?

    @Query("DELETE FROM messages WHERE id IN (:ids)")
    abstract suspend fun deleteByIds(ids: List<Int>): Int
}
