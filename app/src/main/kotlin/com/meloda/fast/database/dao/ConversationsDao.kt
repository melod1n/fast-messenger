package com.meloda.fast.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meloda.fast.api.model.VkConversation

@Dao
interface ConversationsDao {

    @Query("SELECT * FROM conversations")
    suspend fun getAll(): List<VkConversation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(values: List<VkConversation>)

    suspend fun insert(values: Array<out VkConversation>) = insert(values.toList())

}