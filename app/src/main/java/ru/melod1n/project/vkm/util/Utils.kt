package ru.melod1n.project.vkm.util

import android.content.Context
import android.content.res.Configuration
import ru.melod1n.project.vkm.R
import ru.melod1n.project.vkm.common.AppGlobal
import ru.melod1n.project.vkm.extensions.ArrayExtensions.isNullOrEmpty
import ru.melod1n.project.vkm.io.BytesOutputStream
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