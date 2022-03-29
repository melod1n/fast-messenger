package com.meloda.fast.util

import android.content.ClipData
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.net.NetworkCapabilities
import android.util.TypedValue
import androidx.annotation.AttrRes
import com.meloda.fast.common.AppGlobal


object AndroidUtils {

    fun isDarkTheme(): Boolean {
        return when (AppGlobal.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }
    }

    fun hasConnection(): Boolean {
        val network = AppGlobal.connectivityManager.activeNetwork ?: return false
        val activeNetwork =
            AppGlobal.connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }
    }

    fun getDisplayWidth(): Int {
        return Resources.getSystem().displayMetrics.widthPixels
    }

    fun getDisplayHeight(): Int {
        return Resources.getSystem().displayMetrics.heightPixels
    }

    fun copyText(label: String? = "", text: String) {
        AppGlobal.clipboardManager.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    fun getThemeAttrColor(context: Context, @AttrRes resId: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(resId, typedValue, true)
        val colorRes = typedValue.resourceId
        var color = -1
        try {
            color = context.resources.getColor(colorRes, context.theme)
        } catch (e: Exception) {

        }

        return color
    }

    fun bytesToHumanReadableSize(bytes: Double) = when {
        bytes >= 1 shl 30 -> "%.1f GB".format(bytes / (1 shl 30))
        bytes >= 1 shl 20 -> "%.1f MB".format(bytes / (1 shl 20))
        bytes >= 1 shl 10 -> "%.0f KB".format(bytes / (1 shl 10))
        else -> "$bytes B"
    }

}