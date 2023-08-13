package com.meloda.fast.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import com.meloda.fast.data.account.AccountsDao
import com.meloda.fast.model.AppAccount

@Database(
    entities = [AppAccount::class],
    version = 2,
    exportSchema = false
)
abstract class AccountsDatabase : RoomDatabase() {
    abstract val accountsDao: AccountsDao

    // TODO: 09.08.2023, Danil Nikolaev: complete
    companion object {
        val migrationToTrustedHash = Migration(startVersion = 1, endVersion = 2) { db ->
        }
    }
}


