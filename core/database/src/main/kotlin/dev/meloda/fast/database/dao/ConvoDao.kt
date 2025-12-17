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
    abstract suspend fun getAllByIds(ids: List<Int>): List<VkConvoEntity>

    @Query("SELECT * FROM convos WHERE id IS (:id)")
    abstract suspend fun getById(id: Long): VkConvoEntity?

    @Transaction
    @Query("SELECT * FROM convos WHERE id IS (:id)")
    abstract suspend fun getByIdWithMessage(id: Long): ConvoWithMessage?

    @Query("DELETE FROM convos WHERE rowid IN (:ids)")
    abstract suspend fun deleteByIds(ids: List<Int>): Int
}



