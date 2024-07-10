package com.meloda.app.fast.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.meloda.app.fast.model.database.ConversationWithMessage
import com.meloda.app.fast.model.database.VkConversationEntity

@Dao
abstract class ConversationDao : EntityDao<VkConversationEntity> {

    @Query("SELECT * FROM conversations")
    abstract suspend fun getAll(): List<VkConversationEntity>

    @Query("SELECT * FROM conversations WHERE id IN (:ids)")
    abstract suspend fun getAllByIds(ids: List<Int>): List<VkConversationEntity>

    @Query("SELECT * FROM conversations WHERE id IS (:id)")
    abstract suspend fun getById(id: Int): VkConversationEntity?

    @Transaction
    @Query("SELECT * FROM conversations WHERE id IS (:id)")
    abstract suspend fun getByIdWithMessage(id: Int): ConversationWithMessage?

    @Query("DELETE FROM conversations WHERE rowid IN (:ids)")
    abstract suspend fun deleteByIds(ids: List<Int>): Int
}



