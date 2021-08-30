package com.meloda.fast.database

import android.content.ContentValues
import android.database.Cursor
import android.os.Bundle
import com.meloda.fast.common.AppGlobal.Companion.database
import com.meloda.fast.database.DatabaseUtils.TABLE_CHATS
import com.meloda.fast.database.DatabaseUtils.TABLE_FRIENDS
import com.meloda.fast.database.DatabaseUtils.TABLE_MESSAGES
import com.meloda.fast.database.DatabaseUtils.TABLE_USERS
import com.meloda.fast.database.storage.ChatsStorage
import com.meloda.fast.database.storage.GroupsStorage
import com.meloda.fast.database.storage.MessagesStorage
import com.meloda.fast.database.storage.UsersStorage
import com.meloda.fast.api.model.VKConversation
import com.meloda.fast.api.model.VKMessage
import com.meloda.fast.api.model.VKUser
import java.util.*

object CacheStorage {

    val usersStorage = UsersStorage()
    val messagesStorage = MessagesStorage()
    val chatsStorage = ChatsStorage()
    val groupsStorage = GroupsStorage()

    fun selectCursor(tableName: String): Cursor {
        return QueryBuilder.query()
            .select("*").from(tableName)
            .asCursor(database)
    }

    fun selectCursor(tableName: String, where: String): Cursor {
        return QueryBuilder.query()
            .select("*").from(tableName)
            .where(where)
            .asCursor(database)
    }

    fun selectCursor(tableName: String, columnName: String, value: Any): Cursor {
        return QueryBuilder.query()
            .select("*").from(tableName)
            .where("$columnName=$value")
            .asCursor(database)
    }

    fun selectCursor(tableName: String, columnName: String, ids: IntArray): Cursor {
        val where = StringBuilder(5 * ids.size)

        where.append("$columnName=${ids[0]}")

        for (i in 1 until ids.size) {
            where.append(" OR ")
            where.append("$columnName=${ids[i]}")
        }

        return selectCursor(tableName, where.toString())
    }

    fun getInt(cursor: Cursor, columnName: String) =
        cursor.getInt(cursor.getColumnIndexOrThrow(columnName))

    fun getString(cursor: Cursor, columnName: String) =
        cursor.getString(cursor.getColumnIndexOrThrow(columnName))

    fun getBlob(cursor: Cursor, columnName: String) =
        cursor.getBlob(cursor.getColumnIndexOrThrow(columnName))

    fun <T> insert(tableName: String, values: ArrayList<T>) {
        database.beginTransaction()

        val contentValues = ContentValues()

        for (value in values) {
            when (tableName) {
                TABLE_USERS -> {
                    usersStorage.cacheValue(contentValues, value as VKUser)
                    break
                }
                TABLE_FRIENDS -> {
                    usersStorage.cacheValue(
                        contentValues,
                        value as VKUser,
                        Bundle().apply { putBoolean("toFriends", true) })
                    break
                }
                TABLE_MESSAGES -> {
                    messagesStorage.cacheValue(contentValues, value as VKMessage)
                    break
                }
                TABLE_CHATS -> {
                    chatsStorage.cacheValue(contentValues, value as VKConversation)
                    break
                }
            }

            database.insert(tableName, null, contentValues)
            contentValues.clear()
        }

        database.setTransactionSuccessful()
        database.endTransaction()
    }

    fun delete(tableName: String, whereClause: String, vararg whereArgs: String) {
        database.delete(tableName, whereClause, whereArgs)
    }

    fun delete(tableName: String) {
        database.delete(tableName, null, null)
    }


}

