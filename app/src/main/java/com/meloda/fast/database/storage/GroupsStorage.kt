package com.meloda.fast.database.storage

import android.content.ContentValues
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import androidx.annotation.WorkerThread
import com.meloda.fast.database.CacheStorage
import com.meloda.fast.database.CacheStorage.getInt
import com.meloda.fast.database.CacheStorage.getString
import com.meloda.fast.database.DatabaseKeys.DEACTIVATED
import com.meloda.fast.database.DatabaseKeys.GROUP_ID
import com.meloda.fast.database.DatabaseKeys.IS_CLOSED
import com.meloda.fast.database.DatabaseKeys.NAME
import com.meloda.fast.database.DatabaseKeys.PHOTOS
import com.meloda.fast.database.DatabaseKeys.SCREEN_NAME
import com.meloda.fast.database.DatabaseKeys.TYPE
import com.meloda.fast.database.DatabaseUtils.TABLE_GROUPS
import com.meloda.fast.database.base.Storage
import com.meloda.vksdk.model.VKGroup
import com.meloda.vksdk.util.VKUtil
import org.json.JSONObject

class GroupsStorage : Storage<VKGroup>() {

    override val tag = "GroupsStorage"

    @WorkerThread
    fun getGroups(ids: IntArray): ArrayList<VKGroup> {
        val cursor = CacheStorage.selectCursor(TABLE_GROUPS, GROUP_ID, ids)

        val groups = ArrayList<VKGroup>(cursor.count)
        while (cursor.moveToNext()) groups.add(parseValue(cursor))

        cursor.close()
        return groups
    }

    @WorkerThread
    fun getGroup(userId: Int): VKGroup? {
        val group = getGroups(intArrayOf(userId))

        return if (group.isNotEmpty()) group[0] else null
    }

    override fun getAllValues(): ArrayList<VKGroup> {
        val cursor = CacheStorage.selectCursor(TABLE_GROUPS)
        val groups = ArrayList<VKGroup>()

        while (cursor.moveToNext()) groups.add(parseValue(cursor))

        cursor.close()

        return groups
    }

    override fun insertValues(values: ArrayList<VKGroup>, params: Bundle?) {
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

    override fun cacheValue(values: ContentValues, value: VKGroup, params: Bundle?) {
        values.put(GROUP_ID, value.id)
        values.put(NAME, value.name)
        values.put(SCREEN_NAME, value.screenName)
        values.put(IS_CLOSED, value.isClosed)
        values.put(DEACTIVATED, value.deactivated)
        values.put(TYPE, value.type.value)

        val photos =
            VKUtil.putPhotosToJson(value.photo50, value.photo100, value.photo200).toString()

        values.put(PHOTOS, photos)
    }

    override fun parseValue(cursor: Cursor): VKGroup {
        val group = VKGroup()

        group.id = getInt(cursor, GROUP_ID)
        group.name = getString(cursor, NAME)
        group.screenName = getString(cursor, SCREEN_NAME)
        group.isClosed = getInt(cursor, IS_CLOSED) == 1
        group.deactivated = getString(cursor, DEACTIVATED)
        group.type = VKGroup.Type.fromString(getString(cursor, TYPE))

        val photos = VKUtil.parseJsonPhotos(JSONObject(getString(cursor, PHOTOS)))

        group.photo50 = photos[0]
        group.photo100 = photos[1]
        group.photo200 = photos[2]

        return group
    }

}