package dev.meloda.fast.database.dao

import androidx.room.Dao
import androidx.room.Query
import dev.meloda.fast.model.database.VkMessageEntity

@Dao
abstract class MessageDao : EntityDao<VkMessageEntity> {

    @Query("SELECT * FROM messages WHERE isDeleted = 0 AND isSpam = 0")
    abstract suspend fun getAll(): List<VkMessageEntity>

    @Query("SELECT * FROM messages WHERE peerId IS (:convoId)")
    abstract suspend fun getAll(convoId: Long): List<VkMessageEntity>

    @Query("SELECT * FROM messages WHERE id IN (:ids)")
    abstract suspend fun getAllByIds(ids: List<Int>): List<VkMessageEntity>

    @Query("SELECT * FROM messages WHERE id IS (:messageId)")
    abstract suspend fun getById(messageId: Long): VkMessageEntity?

    @Query("DELETE FROM messages WHERE id IN (:ids)")
    abstract suspend fun deleteByIds(ids: List<Int>): Int

    @Query("UPDATE messages SET isDeleted = :isDeleted WHERE peerId = :convoId AND cmId = :cmId")
    abstract suspend fun markAsDeleted(convoId: Long, cmId: Long, isDeleted: Boolean): Int

    @Query("UPDATE messages SET isImportant = :isImportant WHERE peerId = :convoId AND cmId = :cmId")
    abstract suspend fun markAsImportant(convoId: Long, cmId: Long, isImportant: Boolean): Int

    @Query("UPDATE messages SET isSpam = :isSpam WHERE peerId = :convoId AND cmId = :cmId")
    abstract suspend fun markAsSpam(convoId: Long, cmId: Long, isSpam: Boolean): Int
}
