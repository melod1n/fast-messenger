package com.meloda.fast.data.messages

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meloda.fast.api.model.VkMessage

@Dao
interface MessagesDao {

    @Query("SELECT * FROM messages")
    suspend fun getAll(): List<VkMessage>

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getById(id: Int): VkMessage?

    @Query("SELECT * FROM messages WHERE peerId = :peerId")
    suspend fun getByPeerId(peerId: Int): List<VkMessage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(values: List<VkMessage>)

    suspend fun insert(values: Array<out VkMessage>) = insert(values.toList())

}