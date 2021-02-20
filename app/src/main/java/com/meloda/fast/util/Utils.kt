package com.meloda.fast.util

import android.content.Context
import android.content.res.Configuration
import com.meloda.fast.R
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.extensions.ArrayExtensions.isNullOrEmpty
import com.meloda.fast.io.BytesOutputStream
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

object Utils {

    fun getLocalizedThrowable(context: Context, t: Throwable): String {
        return context.getString(R.string.error, t.message.toString())
    }

    fun isDarkTheme(): Boolean {
        val currentNightMode =
            AppGlobal.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }
    }

    fun serialize(source: Any?): ByteArray? {
        try {
            val bos = BytesOutputStream()
            val out = ObjectOutputStream(bos)
            out.writeObject(source)
            out.close()
            return bos.byteArray
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun deserialize(source: ByteArray?): Any? {
        if (source.isNullOrEmpty()) {
            return null
        }

        try {
            val bis = ByteArrayInputStream(source)
            val `in` = ObjectInputStream(bis)
            val o = `in`.readObject()
            `in`.close()
            return o
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }


}