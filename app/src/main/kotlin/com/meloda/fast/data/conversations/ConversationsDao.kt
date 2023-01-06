package com.meloda.fast.data.conversations

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meloda.fast.api.model.data.VkConversation

@Dao
interface ConversationsDao {

    @Query("SELECT * FROM conversations")
    suspend fun getAll(): List<VkConversation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(values: List<VkConversation>)

}
