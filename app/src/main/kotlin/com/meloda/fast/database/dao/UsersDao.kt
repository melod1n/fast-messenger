package com.meloda.fast.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meloda.fast.api.model.VkUser

@Dao
interface UsersDao : KindaDao<VkUser> {

//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insert(values: List<VkUser>)

//    override suspend fun insert(values: List<VkUser>) {
//        TODO("Not yet implemented")
//    }


    @Query("SELECT * FROM users")
    suspend fun getAll(): List<VkUser>

    suspend fun insert(values: Array<out VkUser>) = insert(values.toList())

}

interface KindaDao<T> {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(values: List<T>)

}