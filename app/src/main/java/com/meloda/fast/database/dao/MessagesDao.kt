package com.meloda.fast.database.dao

import androidx.room.*
import com.meloda.fast.api.model.VKMessage

@Dao
interface MessagesDao {

    @Query("SELECT * FROM messages")
    fun getAll(): List<VKMessage>

    @Query("SELECT * FROM messages WHERE messageId = :id")
    fun getById(id: Int): VKMessage?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: VKMessage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(items: List<VKMessage>)

    @Update
    fun update(item: VKMessage)

    @Update
    fun update(items: List<VKMessage>)

    @Delete
    fun delete(item: VKMessage)

    @Delete
    fun delete(items: List<VKMessage>)

    @Query("DELETE FROM messages WHERE messageId = :id")
    fun deleteById(id: Int)

    @Query("DELETE FROM messages")
    fun clear()

    @Query("SELECT * FROM messages WHERE peerId = :peerId")
    fun getByPeerId(peerId: Int): List<VKMessage>
}