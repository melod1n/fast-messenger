package com.meloda.fast.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.core.content.FileProvider
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import com.meloda.fast.BuildConfig
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.ext.isTrue
import java.io.File


object AndroidUtils {

    fun getDisplayWidth(): Int {
        return Resources.getSystem().displayMetrics.widthPixels
    }

    fun getDisplayHeight(): Int {
        return Resources.getSystem().displayMetrics.heightPixels
    }

    fun copyText(label: String? = "", text: String) {
        val clipboardManager =
            AppGlobal.Instance.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        clipboardManager.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    fun getThemeAttrColor(context: Context, @AttrRes resId: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(resId, typedValue, true)
        val colorRes = typedValue.resourceId
        var color = -1
        try {
            color = context.resources.getColor(colorRes, context.theme)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return color
    }

    fun bytesToMegabytes(bytes: Double): Double {
        return bytes / 1024 / 1024
    }

    fun bytesToHumanReadableSize(bytes: Double): String = when {
        bytes >= 1 shl 30 -> "%.1f GB".format(bytes / (1 shl 30))
        bytes >= 1 shl 20 -> "%.1f MB".format(bytes / (1 shl 20))
        bytes >= 1 shl 10 -> "%.1f KB".format(bytes / (1 shl 10))
        else -> "$bytes B"
    }

    @Suppress("DEPRECATION")
    fun isCanInstallUnknownApps(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Settings.Secure.getInt(
                AppGlobal.Instance.contentResolver,
                Settings.Secure.INSTALL_NON_MARKET_APPS
            ) == 1
        } else {
            AppGlobal.packageManager.canRequestPackageInstalls()
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
        fileToRead: File,
    ): Intent {
        val intent = Intent(Intent.ACTION_VIEW)

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
        intent.data = FileProvider.getUriForFile(
            context,
            BuildConfig.APPLICATION_ID + providerPath,
            fileToRead
        )

        return intent
    }

    fun getStatusBarInsets(insets: WindowInsetsCompat): Insets {
        return insets.getInsets(WindowInsetsCompat.Type.statusBars())
    }

    fun getNavBarInsets(insets: WindowInsetsCompat): Insets {
        return insets.getInsets(WindowInsetsCompat.Type.navigationBars())
    }

    fun getImeInsets(insets: WindowInsetsCompat): Insets {
        return insets.getInsets(WindowInsetsCompat.Type.ime())
    }

    fun isBatterySaverOn(): Boolean {
        return (AppGlobal.Instance.getSystemService(Context.POWER_SERVICE) as? PowerManager)?.isPowerSaveMode.isTrue
    }
}
