package com.meloda.fast.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import com.meloda.fast.database.model.VkConversationDB
import com.meloda.fast.database.model.VkMessageDB

@Dao
interface ConversationsDao {

    @Query("SELECT * FROM conversations")
    suspend fun getAll(): List<VkConversationDB>

    @Query("SELECT * FROM conversations WHERE id IN (:ids)")
    suspend fun getAllByIds(ids: List<Int>): List<VkConversationDB>

    @Query("SELECT * FROM conversations WHERE id IS (:id)")
    suspend fun getById(id: Int): VkConversationDB?

    @Transaction
    @Query("SELECT * FROM conversations WHERE id IS (:id)")
    suspend fun getByIdWithMessage(id: Int): ConversationWithMessage?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<VkConversationDB>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: VkConversationDB)

    @Delete
    suspend fun delete(item: VkConversationDB): Int

    @Delete
    suspend fun deleteAll(items: List<VkConversationDB>): Int

    @Query("DELETE FROM conversations WHERE rowid IN (:ids)")
    suspend fun deleteByIds(ids: List<Int>): Int
}


data class ConversationWithMessage(
    @Embedded val conversation: VkConversationDB,
    @Relation(
        parentColumn = "lastMessageId",
        entityColumn = "id"
    )
    val message: VkMessageDB?
)
