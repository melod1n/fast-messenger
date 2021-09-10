package com.meloda.fast.database.old

import android.content.ContentValues
import android.database.Cursor
import android.os.Bundle
import com.meloda.fast.common.AppGlobal.Companion.oldDatabase
import com.meloda.fast.database.old.DatabaseUtils.TABLE_CHATS
import com.meloda.fast.database.old.DatabaseUtils.TABLE_FRIENDS
import com.meloda.fast.database.old.DatabaseUtils.TABLE_MESSAGES
import com.meloda.fast.database.old.DatabaseUtils.TABLE_USERS
import com.meloda.fast.database.old.storage.ChatsStorage
import com.meloda.fast.database.old.storage.GroupsStorage
import com.meloda.fast.database.old.storage.MessagesStorage
import com.meloda.fast.database.old.storage.UsersStorage
import com.meloda.fast.api.model.old.oldVKConversation
import com.meloda.fast.api.model.old.oldVKMessage
import com.meloda.fast.api.model.old.oldVKUser
import java.util.*

object CacheStorage {

    val usersStorage = UsersStorage()
    val messagesStorage = MessagesStorage()
    val chatsStorage = ChatsStorage()
    val groupsStorage = GroupsStorage()

    fun selectCursor(tableName: String): Cursor {
        return QueryBuilder.query()
            .select("*").from(tableName)
            .asCursor(oldDatabase)
    }

    fun selectCursor(tableName: String, where: String): Cursor {
        return QueryBuilder.query()
            .select("*").from(tableName)
            .where(where)
            .asCursor(oldDatabase)
    }

    fun selectCursor(tableName: String, columnName: String, value: Any): Cursor {
        return QueryBuilder.query()
            .select("*").from(tableName)
            .where("$columnName=$value")
            .asCursor(oldDatabase)
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
        oldDatabase.beginTransaction()

        val contentValues = ContentValues()

        for (value in values) {
            when (tableName) {
                TABLE_USERS -> {
                    usersStorage.cacheValue(contentValues, value as oldVKUser)
                    break
                }
                TABLE_FRIENDS -> {
                    usersStorage.cacheValue(
                        contentValues,
                        value as oldVKUser,
                        Bundle().apply { putBoolean("toFriends", true) })
                    break
                }
                TABLE_MESSAGES -> {
                    messagesStorage.cacheValue(contentValues, value as oldVKMessage)
                    break
                }
                TABLE_CHATS -> {
                    chatsStorage.cacheValue(contentValues, value as oldVKConversation)
                    break
                }
            }

            oldDatabase.insert(tableName, null, contentValues)
            contentValues.clear()
        }

        oldDatabase.setTransactionSuccessful()
        oldDatabase.endTransaction()
    }

    fun delete(tableName: String, whereClause: String, vararg whereArgs: String) {
        oldDatabase.delete(tableName, whereClause, whereArgs)
    }

    fun delete(tableName: String) {
        oldDatabase.delete(tableName, null, null)
    }


}

