package com.meloda.fast.data.users

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meloda.fast.api.model.VkUser

@Dao
interface UsersDao {

    @Query("SELECT * FROM users")
    suspend fun getAll(): List<VkUser>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: Int): VkUser?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(values: List<VkUser>)

    suspend fun insert(values: Array<out VkUser>) = insert(values.toList())

}