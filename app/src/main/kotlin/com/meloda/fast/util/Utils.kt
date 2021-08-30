package com.meloda.fast.util

import android.content.Context
import com.meloda.fast.util.ArrayUtils.isNullOrEmpty
import com.meloda.fast.R
import com.meloda.fast.io.BytesOutputStream
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

object Utils {

    fun getLocalizedThrowable(context: Context, t: Throwable): String {
        return context.getString(R.string.error, t.message.toString())
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