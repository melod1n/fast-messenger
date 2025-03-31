package dev.meloda.fast.database

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.meloda.fast.database.dao.AccountDao
import dev.meloda.fast.model.database.AccountEntity

@Database(
    entities = [AccountEntity::class],
    version = 3
)
abstract class AccountsDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
}


