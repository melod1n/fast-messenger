package ru.melod1n.project.vkm.database.dao

import androidx.room.*
import ru.melod1n.project.vkm.api.model.VKConversation

@Dao
interface ConversationsDao {

    @Query("SELECT * FROM conversations")
    fun getAll(): List<VKConversation>

    @Query("SELECT * FROM conversations WHERE conversationId = :id")
    fun getById(id: Int): VKConversation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: VKConversation)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(items: List<VKConversation>)

    @Update
    fun update(item: VKConversation)

    @Update
    fun update(items: List<VKConversation>)

    @Delete
    fun delete(item: VKConversation)

    @Delete
    fun delete(items: List<VKConversation>)

    @Query("DELETE FROM conversations WHERE conversationId = :id")
    fun deleteById(id: Int)

    @Query("DELETE FROM conversations")
    fun clear()
}