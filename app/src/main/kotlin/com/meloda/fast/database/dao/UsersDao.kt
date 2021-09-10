package com.meloda.fast.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meloda.fast.api.model.VkUser

@Dao
interface UsersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(values: List<VkUser>)

    @Query("SELECT * FROM users")
    suspend fun getAll(): List<VkUser>

    suspend fun insert(values: Array<out VkUser>) = insert(values.toList())

}