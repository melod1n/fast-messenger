package com.meloda.fast.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.meloda.fast.model.AppAccount

@Dao
abstract class AccountsDao : BaseDao<AppAccount> {

    @Query("SELECT * FROM accounts")
    abstract suspend fun getAll(): List<AppAccount>

    @Query("DELETE FROM accounts WHERE userId = :userId")
    abstract suspend fun deleteById(userId: Int)
}
