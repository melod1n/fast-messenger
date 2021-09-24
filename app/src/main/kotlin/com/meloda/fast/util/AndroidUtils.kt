package com.meloda.fast.util

import android.content.ClipData
import android.content.Context
import android.content.res.Configuration
import android.net.NetworkCapabilities
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.annotation.AttrRes
import com.meloda.fast.common.AppGlobal


object AndroidUtils {

    fun px(dp: Float): Float {
        return dp * (AppGlobal.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun px(dp: Int) = px(dp.toFloat())

    fun dp(px: Float): Float {
        return px / (AppGlobal.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun dp(px: Int) = dp(px.toFloat())

    fun isDarkTheme(): Boolean {
        val currentNightMode =
            AppGlobal.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return when (currentNightMode) {
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
        return AppGlobal.resources.displayMetrics.widthPixels
    }

    fun getDisplayHeight(): Int {
        return AppGlobal.resources.displayMetrics.heightPixels
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