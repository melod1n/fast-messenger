package com.meloda.fast.database.old.storage

import android.content.ContentValues
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import androidx.annotation.WorkerThread
import com.meloda.fast.api.UserConfig
import com.meloda.fast.api.VKUtil
import com.meloda.fast.api.model.old.oldVKUser
import com.meloda.fast.database.old.CacheStorage
import com.meloda.fast.database.old.DatabaseKeys.DEACTIVATED
import com.meloda.fast.database.old.DatabaseKeys.FIRST_NAME
import com.meloda.fast.database.old.DatabaseKeys.FRIEND_ID
import com.meloda.fast.database.old.DatabaseKeys.GENDER
import com.meloda.fast.database.old.DatabaseKeys.IS_ONLINE
import com.meloda.fast.database.old.DatabaseKeys.IS_ONLINE_MOBILE
import com.meloda.fast.database.old.DatabaseKeys.LAST_NAME
import com.meloda.fast.database.old.DatabaseKeys.LAST_SEEN
import com.meloda.fast.database.old.DatabaseKeys.PHOTOS
import com.meloda.fast.database.old.DatabaseKeys.SCREEN_NAME
import com.meloda.fast.database.old.DatabaseKeys.SORT_ID
import com.meloda.fast.database.old.DatabaseKeys.STATUS
import com.meloda.fast.database.old.DatabaseKeys.USER_ID
import com.meloda.fast.database.old.DatabaseUtils.TABLE_FRIENDS
import com.meloda.fast.database.old.DatabaseUtils.TABLE_USERS
import com.meloda.fast.database.old.QueryBuilder
import com.meloda.fast.database.old.base.Storage
import org.json.JSONObject

@WorkerThread
class UsersStorage : Storage<oldVKUser>() {

    override val tag = "UsersStorage"

    @WorkerThread
    fun getUsers(ids: IntArray): ArrayList<oldVKUser> {
        val cursor = CacheStorage.selectCursor(TABLE_USERS, USER_ID, ids)

        val users = ArrayList<oldVKUser>(cursor.count)
        while (cursor.moveToNext()) users.add(parseValue(cursor))

        cursor.close()
        return users
    }

    @WorkerThread
    fun getUser(userId: Int): oldVKUser? {
        val user = getUsers(intArrayOf(userId))

        return if (user.isNotEmpty()) user[0] else null
    }

    @WorkerThread
    fun getFriends(userId: Int, onlyOnline: Boolean = false): ArrayList<oldVKUser> {
        val cursor = QueryBuilder.query()
            .select("*")
            .from(TABLE_FRIENDS)
            .leftJoin(TABLE_USERS)
            .on("friends.${FRIEND_ID} = users.$USER_ID")
            .where("friends.${USER_ID} = $userId")
            .asCursor(database)

        val users = ArrayList<oldVKUser>(cursor.count)

        while (cursor.moveToNext()) {
            val userOnline = CacheStorage.getInt(cursor, IS_ONLINE) == 1
            if (onlyOnline && !userOnline) continue

            val user = parseValue(cursor)
            users.add(user)
        }

        cursor.close()

        return users
    }

    override fun getAllValues(): ArrayList<oldVKUser> {
        val cursor = CacheStorage.selectCursor(TABLE_USERS)
        val users = ArrayList<oldVKUser>()

        while (cursor.moveToNext()) users.add(parseValue(cursor))

        cursor.close()

        return users
    }

    @WorkerThread
    override fun insertValues(values: ArrayList<oldVKUser>, params: Bundle?) {
        if (values.isEmpty()) return

        val toFriends = params?.getBoolean("toFriends") ?: false

        database.beginTransaction()

        val contentValues = ContentValues()

        for (user in values) {
            cacheValue(contentValues, user, params)

            database.insert(if (toFriends) TABLE_FRIENDS else TABLE_USERS, null, contentValues)

            contentValues.clear()
        }

        database.setTransactionSuccessful()
        database.endTransaction()

        Log.d(tag, "Successful cached users. toFriends: $toFriends")
    }

    @WorkerThread
    override fun cacheValue(values: ContentValues, value: oldVKUser, params: Bundle?) {
        val toFriends = params?.getBoolean("toFriends") ?: false

        if (toFriends) {
            values.put(USER_ID, UserConfig.userId)
            values.put(FRIEND_ID, value.userId)
            values.put(SORT_ID, value.sortId)
            return
        }

        values.put(USER_ID, value.userId)
        values.put(FIRST_NAME, value.firstName)
        values.put(LAST_NAME, value.lastName)
        values.put(DEACTIVATED, value.deactivated)
        values.put(GENDER, value.sex)
        values.put(SCREEN_NAME, value.screenName)
        values.put(IS_ONLINE, value.isOnline)
        values.put(IS_ONLINE_MOBILE, value.isOnlineMobile)
        values.put(STATUS, value.status)
        values.put(LAST_SEEN, value.lastSeen)

        values.put(
            PHOTOS,
            VKUtil.putPhotosToJson(
                value.photo50,
                value.photo100,
                value.photo200
            ).toString()
        )
    }

    @WorkerThread
    override fun parseValue(cursor: Cursor): oldVKUser {
        val user = oldVKUser()

        user.userId = CacheStorage.getInt(cursor, USER_ID)
        user.firstName = CacheStorage.getString(cursor, FIRST_NAME)
        user.lastName = CacheStorage.getString(cursor, LAST_NAME)
        user.deactivated = CacheStorage.getString(cursor, DEACTIVATED)
        user.sex = CacheStorage.getInt(cursor, GENDER)
        user.screenName = CacheStorage.getString(cursor, SCREEN_NAME)
        user.isOnline = CacheStorage.getInt(cursor, IS_ONLINE) == 1
        user.isOnlineMobile = CacheStorage.getInt(cursor, IS_ONLINE_MOBILE) == 1
        user.status = CacheStorage.getString(cursor, STATUS)
        user.lastSeen = CacheStorage.getInt(cursor, LAST_SEEN)

        val photos =
            VKUtil.parseJsonPhotos(JSONObject(CacheStorage.getString(cursor, PHOTOS)))

        user.photo50 = photos[0]
        user.photo100 = photos[1]
        user.photo200 = photos[2]

        return user
    }

}