package ru.melod1n.project.vkm.database.dao

import androidx.room.*
import ru.melod1n.project.vkm.api.model.VKFriend

@Dao
interface FriendsDao {

    @Query("SELECT * FROM friends")
    fun getAll(): List<VKFriend>

    @Query("SELECT * FROM friends WHERE userId = :id")
    fun getById(id: Int): VKFriend?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: VKFriend)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(items: List<VKFriend>)

    @Update
    fun update(item: VKFriend)

    @Update
    fun update(items: List<VKFriend>)

    @Delete
    fun delete(item: VKFriend)

    @Delete
    fun delete(items: List<VKFriend>)

    @Query("DELETE FROM friends WHERE userId = :id")
    fun deleteById(id: Int)

    @Query("DELETE FROM friends")
    fun clear()

    @Query("SELECT * FROM friends WHERE userId = :id")
    fun getByUserId(id: Int): List<VKFriend>
}