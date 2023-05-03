package com.meloda.fast.data.account

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meloda.fast.model.AppAccount

@Dao
interface AccountsDao {

    @Query("SELECT * FROM accounts")
    suspend fun getAll(): List<AppAccount>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(values: List<AppAccount>)

    @Query("DELETE FROM accounts WHERE userId = :userId")
    suspend fun deleteById(userId: Int)

}
