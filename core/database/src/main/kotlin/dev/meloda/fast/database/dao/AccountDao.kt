package dev.meloda.fast.database.dao

import androidx.room.Dao
import androidx.room.Query
import dev.meloda.fast.model.database.AccountEntity

@Dao
abstract class AccountDao : EntityDao<AccountEntity> {

    @Query("SELECT * FROM accounts")
    abstract suspend fun getAll(): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE userId = :userId")
    abstract suspend fun getById(userId: Long): AccountEntity?

    @Query("DELETE FROM accounts WHERE userId = :userId")
    abstract suspend fun deleteById(userId: Long)
}
