package com.meloda.fast.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.meloda.fast.data.account.AccountsDao
import com.meloda.fast.model.AppAccount

@Database(
    entities = [AppAccount::class],
    version = 1,
    exportSchema = false
)
abstract class AccountsDatabase : RoomDatabase() {
    abstract val accountsDao: AccountsDao
}