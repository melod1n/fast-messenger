package ru.melod1n.project.vkm.database.dao

import androidx.room.*
import ru.melod1n.project.vkm.api.model.VKUser

@Dao
interface UsersDao {

    @Query("SELECT * FROM users")
    fun getAll(): List<VKUser>

    @Query("SELECT * FROM users WHERE userId = :id")
    fun getById(id: Int): VKUser?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: VKUser)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(items: List<VKUser>)

    @Update
    fun update(item: VKUser)

    @Update
    fun update(items: List<VKUser>)

    @Delete
    fun delete(item: VKUser)

    @Delete
    fun delete(items: List<VKUser>)

    @Query("DELETE FROM users WHERE userId = :id")
    fun deleteById(id: Int)

    @Query("DELETE FROM users")
    fun clear()
}