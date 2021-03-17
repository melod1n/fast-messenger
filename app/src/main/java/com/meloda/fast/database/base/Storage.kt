package com.meloda.fast.database.base

import android.content.ContentValues
import android.database.Cursor
import android.os.Bundle
import androidx.annotation.WorkerThread
import com.meloda.fast.common.AppGlobal

abstract class Storage<T> {

    abstract val tag: String

    protected var database = AppGlobal.database

    @WorkerThread
    abstract fun getAllValues(): ArrayList<T>

    @WorkerThread
    abstract fun insertValues(values: ArrayList<T>, params: Bundle? = null)

    @WorkerThread
    fun insertValue(value: T, params: Bundle? = null) {
        insertValues(arrayListOf(value), params)
    }

    @WorkerThread
    abstract fun cacheValue(values: ContentValues, value: T, params: Bundle? = null)

    @WorkerThread
    abstract fun parseValue(cursor: Cursor): T

}