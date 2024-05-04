package com.meloda.fast.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.meloda.fast.database.dao.AccountsDao
import com.meloda.fast.model.AppAccount

@Database(
    entities = [AppAccount::class],
    version = 2
)
abstract class AccountsDatabase : RoomDatabase() {
    abstract fun accountsDao(): AccountsDao
}


