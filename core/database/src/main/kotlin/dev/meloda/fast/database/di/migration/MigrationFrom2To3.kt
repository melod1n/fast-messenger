package dev.meloda.fast.database.di.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val migrationFrom2To3 = object : Migration(
    startVersion = 2,
    endVersion = 3
) {

    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE accounts ADD COLUMN exchangeToken TEXT DEFAULT null")
    }
}
