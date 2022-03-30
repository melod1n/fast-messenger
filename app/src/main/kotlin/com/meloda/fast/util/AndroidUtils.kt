package com.meloda.fast.util

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.core.content.FileProvider
import com.meloda.fast.BuildConfig
import com.meloda.fast.common.AppConstants
import com.meloda.fast.common.AppGlobal
import java.io.File


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

    @Suppress("DEPRECATION")
    fun isCanInstallUnknownApps(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AppGlobal.packageManager.canRequestPackageInstalls()
        } else {
            Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.INSTALL_NON_MARKET_APPS
            ) == 1
        }
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

    fun getInstallPackageIntent(
        context: Context,
        providerPath: String,
        fileToRead: File
    ): Intent {
        val intent = Intent(Intent.ACTION_VIEW)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            intent.data = FileProvider.getUriForFile(
                context,
                BuildConfig.APPLICATION_ID + providerPath,
                fileToRead
            )
        } else {
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.setDataAndType(Uri.fromFile(fileToRead), AppConstants.INSTALL_APP_MIME_TYPE)
        }

        return intent
    }
}