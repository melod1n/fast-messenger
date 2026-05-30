package dev.meloda.fast.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import dev.meloda.fast.model.database.ConvoWithMessage
import dev.meloda.fast.model.database.VkConvoEntity

@Dao
abstract class ConvoDao : EntityDao<VkConvoEntity> {

    @Query("SELECT * FROM convos")
    abstract suspend fun getAll(): List<VkConvoEntity>

    @Query("SELECT * FROM convos WHERE id IN (:ids)")
    abstract suspend fun getAllByIds(ids: List<Long>): List<VkConvoEntity>

    @Query("SELECT * FROM convos WHERE id IS (:id)")
    abstract suspend fun getById(id: Long): VkConvoEntity?

    @Transaction
    @Query("SELECT * FROM convos WHERE id IS (:id)")
    abstract suspend fun getByIdWithMessage(id: Long): ConvoWithMessage?

    @Query("DELETE FROM convos WHERE rowid IN (:ids)")
    abstract suspend fun deleteByIds(ids: List<Long>): Int

    @Query("UPDATE convos SET inReadCmId = :cmId, unreadCount = :unreadCount WHERE id = :convoId")
    abstract suspend fun updateReadIncoming(convoId: Long, cmId: Long, unreadCount: Int): Int

    @Query("UPDATE convos SET outReadCmId = :cmId, unreadCount = :unreadCount WHERE id = :convoId")
    abstract suspend fun updateReadOutgoing(convoId: Long, cmId: Long, unreadCount: Int): Int

    @Query("UPDATE convos SET isArchived = :isArchived WHERE id = :convoId")
    abstract suspend fun updateIsArchived(convoId: Long, isArchived: Boolean): Int

    @Query("UPDATE convos SET majorId = :majorId WHERE id = :convoId")
    abstract suspend fun updateMajorId(convoId: Long, majorId: Int): Int

    @Query("UPDATE convos SET minorId = :minorId WHERE id = :convoId")
    abstract suspend fun updateMinorId(convoId: Long, minorId: Int): Int

    @Query("UPDATE convos SET lastCmId = :cmId WHERE id = :convoId")
    abstract suspend fun updateLastCmId(convoId: Long, cmId: Long): Int
}
