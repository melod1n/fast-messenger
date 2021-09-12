package com.meloda.fast.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meloda.fast.api.model.VkGroup

@Dao
interface GroupsDao {

    @Query("SELECT * FROM groups")
    suspend fun getAll(): List<VkGroup>

    @Query("SELECT * FROM groups WHERE id = :id")
    suspend fun getById(id: Int): VkGroup?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(values: List<VkGroup>)

    suspend fun insert(values: Array<out VkGroup>) = insert(values.toList())

}