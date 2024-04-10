package com.meloda.fast.database.account

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meloda.fast.model.AppAccount
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountsDao {

    @Query("SELECT * FROM accounts")
    suspend fun getAll(): List<AppAccount>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(values: List<AppAccount>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(value: AppAccount)

    @Query("DELETE FROM accounts WHERE userId = :userId")
    suspend fun deleteById(userId: Int)

}
