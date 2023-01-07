package com.meloda.fast.data.conversations

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meloda.fast.api.model.domain.VkConversationDomain

@Dao
interface ConversationsDao {

    @Query("SELECT * FROM conversations")
    suspend fun getAll(): List<VkConversationDomain>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(values: List<VkConversationDomain>)

}
