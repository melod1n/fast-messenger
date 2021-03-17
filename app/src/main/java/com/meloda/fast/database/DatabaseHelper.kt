package com.meloda.fast.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper constructor(context: Context) : SQLiteOpenHelper(
    context,
    DB_NAME,
    null,
    DB_VERSION
) {
    companion object {
        private const val DB_NAME = "cache.db"
        private const val DB_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(DatabaseUtils.createUsersTable())
        db.execSQL(DatabaseUtils.createGroupsTable())
        db.execSQL(DatabaseUtils.createFriendsTable())
        db.execSQL(DatabaseUtils.createMessagesTable())
        db.execSQL(DatabaseUtils.createChatsTable())
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }
}