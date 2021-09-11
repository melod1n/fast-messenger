package com.meloda.fast.database.old.storage

import android.content.ContentValues
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import androidx.annotation.WorkerThread
import com.meloda.fast.database.old.CacheStorage
import com.meloda.fast.database.old.CacheStorage.getInt
import com.meloda.fast.database.old.CacheStorage.getString
import com.meloda.fast.database.old.DatabaseKeys.DEACTIVATED
import com.meloda.fast.database.old.DatabaseKeys.GROUP_ID
import com.meloda.fast.database.old.DatabaseKeys.IS_CLOSED
import com.meloda.fast.database.old.DatabaseKeys.NAME
import com.meloda.fast.database.old.DatabaseKeys.PHOTOS
import com.meloda.fast.database.old.DatabaseKeys.SCREEN_NAME
import com.meloda.fast.database.old.DatabaseKeys.TYPE
import com.meloda.fast.database.old.DatabaseUtils.TABLE_GROUPS
import com.meloda.fast.database.old.base.Storage
import com.meloda.fast.api.model.old.oldVKGroup
import com.meloda.fast.api.oldVKUtil
import org.json.JSONObject

class GroupsStorage : Storage<oldVKGroup>() {

    override val tag = "GroupsStorage"

    @WorkerThread
    fun getGroups(ids: IntArray): ArrayList<oldVKGroup> {
        val cursor = CacheStorage.selectCursor(TABLE_GROUPS, GROUP_ID, ids)

        val groups = ArrayList<oldVKGroup>(cursor.count)
        while (cursor.moveToNext()) groups.add(parseValue(cursor))

        cursor.close()
        return groups
    }

    @WorkerThread
    fun getGroup(userId: Int): oldVKGroup? {
        val group = getGroups(intArrayOf(userId))

        return if (group.isNotEmpty()) group[0] else null
    }

    override fun getAllValues(): ArrayList<oldVKGroup> {
        val cursor = CacheStorage.selectCursor(TABLE_GROUPS)
        val groups = ArrayList<oldVKGroup>()

        while (cursor.moveToNext()) groups.add(parseValue(cursor))

        cursor.close()

        return groups
    }

    override fun insertValues(values: ArrayList<oldVKGroup>, params: Bundle?) {
        if (values.isEmpty()) return

        database.beginTransaction()

        val contentValues = ContentValues()

        for (value in values) {
            cacheValue(contentValues, value, params)

            database.insert(TABLE_GROUPS, null, contentValues)

            contentValues.clear()
        }

        database.setTransactionSuccessful()
        database.endTransaction()

        Log.d(tag, "Successful cached groups")
    }

    override fun cacheValue(values: ContentValues, value: oldVKGroup, params: Bundle?) {
        values.put(GROUP_ID, value.id)
        values.put(NAME, value.name)
        values.put(SCREEN_NAME, value.screenName)
        values.put(IS_CLOSED, value.isClosed)
        values.put(DEACTIVATED, value.deactivated)
        values.put(TYPE, value.type.value)

        val photos =
            oldVKUtil.putPhotosToJson(value.photo50, value.photo100, value.photo200).toString()

        values.put(PHOTOS, photos)
    }

    override fun parseValue(cursor: Cursor): oldVKGroup {
        val group = oldVKGroup()

        group.id = getInt(cursor, GROUP_ID)
        group.name = getString(cursor, NAME)
        group.screenName = getString(cursor, SCREEN_NAME)
        group.isClosed = getInt(cursor, IS_CLOSED) == 1
        group.deactivated = getString(cursor, DEACTIVATED)
        group.type = oldVKGroup.Type.fromString(getString(cursor, TYPE))

        val photos = oldVKUtil.parseJsonPhotos(JSONObject(getString(cursor, PHOTOS)))

        group.photo50 = photos[0]
        group.photo100 = photos[1]
        group.photo200 = photos[2]

        return group
    }

}