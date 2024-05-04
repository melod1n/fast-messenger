package com.meloda.fast.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.meloda.fast.database.model.ConversationWithMessage
import com.meloda.fast.database.model.VkConversationDB

@Dao
abstract class ConversationsDao : BaseDao<VkConversationDB> {

    @Query("SELECT * FROM conversations")
    abstract suspend fun getAll(): List<VkConversationDB>

    @Query("SELECT * FROM conversations WHERE id IN (:ids)")
    abstract suspend fun getAllByIds(ids: List<Int>): List<VkConversationDB>

    @Query("SELECT * FROM conversations WHERE id IS (:id)")
    abstract suspend fun getById(id: Int): VkConversationDB?

    @Transaction
    @Query("SELECT * FROM conversations WHERE id IS (:id)")
    abstract suspend fun getByIdWithMessage(id: Int): ConversationWithMessage?

    @Query("DELETE FROM conversations WHERE rowid IN (:ids)")
    abstract suspend fun deleteByIds(ids: List<Int>): Int
}



