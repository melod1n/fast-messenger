package com.meloda.fast.util

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.annotation.AttrRes
import com.meloda.fast.BuildConfig
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

    fun isDeveloperSettingsEnabled(context: Context) = Settings.Secure.getInt(
        context.contentResolver,
        Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
        0
    ) == 1

    @Suppress("DEPRECATION")
    fun isCanInstallUnknownApps(context: Context) =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.INSTALL_NON_MARKET_APPS
            ) == 1
        } else {
            AppGlobal.packageManager.canRequestPackageInstalls()
        }

    fun openInstallUnknownAppsScreen(context: Context) {
        context.startActivity(Intent().apply {
            action = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                Settings.ACTION_SECURITY_SETTINGS
            } else {
                data = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES
            }
        })
    }

    fun copyText(label: String? = "", text: String) {
        AppGlobal.clipboardManager.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    fun getThemeAttrColor(context: Context, @AttrRes resId: Int): Int {
//        val typedValue = TypedValue()
//
//        context.theme.resolveAttribute(resId, typedValue, true)
//
//        return typedValue.data

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

    /*
    TypedValue typedValue = new TypedValue();
    context.getTheme().resolveAttribute(attributeId, typedValue, true);
    int colorRes = typedValue.resourceId;
    int color = -1;
    try {
        color = context.getResources().getColor(colorRes);
    } catch (Resources.NotFoundException e) {
        Log.w(TAG, "Not found color resource by id: " + colorRes);
    }
    return color;
     */

}