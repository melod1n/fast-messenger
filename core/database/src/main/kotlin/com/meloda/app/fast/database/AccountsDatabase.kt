package com.meloda.app.fast.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.meloda.app.fast.database.dao.AccountDao
import com.meloda.app.fast.model.database.AccountEntity

@Database(
    entities = [AccountEntity::class],
    version = 2
)
abstract class AccountsDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
}


