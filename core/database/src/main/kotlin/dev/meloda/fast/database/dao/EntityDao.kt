package dev.meloda.fast.database.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy

interface EntityDao<T> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(values: List<T>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(value: T)

    @Delete
    suspend fun delete(value: T): Int

    @Delete
    suspend fun deleteAll(values: List<T>): Int
}
