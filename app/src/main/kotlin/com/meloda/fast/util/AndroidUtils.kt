package com.meloda.fast.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.core.content.FileProvider
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import com.meloda.fast.BuildConfig
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.ext.isTrue
import java.io.File
import java.io.FileOutputStream


object AndroidUtils {

    fun getDisplayWidth(): Int {
        return Resources.getSystem().displayMetrics.widthPixels
    }

    fun getDisplayHeight(): Int {
        return Resources.getSystem().displayMetrics.heightPixels
    }

    fun copyText(
        label: String? = "",
        text: String,
        withToast: Boolean = false
    ) {
        val clipboardManager =
            AppGlobal.Instance.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        clipboardManager.setPrimaryClip(ClipData.newPlainText(label, text))

        if (withToast && Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            Toast.makeText(AppGlobal.Instance, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyImage(
        label: String? = "",
        imageUri: Uri,
        withToast: Boolean = false
    ) {
        val clipboardManager =
            AppGlobal.Instance.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        clipboardManager.setPrimaryClip(ClipData.newRawUri(label, imageUri))

        if (withToast && Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            Toast.makeText(AppGlobal.Instance, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }
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

    fun getImageToShare(context: Context, existingFile: File): Uri? {
        val imageFolder = File(context.cacheDir, "images")

        return try {
            imageFolder.mkdirs()

            val copyToFile = File(imageFolder, "shared_image.png")
            if (copyToFile.exists()) {
                copyToFile.delete()
            }

            val file = existingFile.copyTo(copyToFile)
            FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getImageToShare(context: Context, bitmap: Bitmap): Uri? {
        val imageFolder = File(context.cacheDir, "images")

        return try {
            imageFolder.mkdirs()

            val file = File(imageFolder, "shared_image.png")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun showShareSheet(context: Context, content: ShareContent) {
        val intent = Intent(Intent.ACTION_SEND).apply {

            type = when (content) {
                is ShareContent.Text -> {
                    putExtra(Intent.EXTRA_TEXT, content.text)
                    "text/plain"
                }

                is ShareContent.Image -> {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    putExtra(Intent.EXTRA_STREAM, content.uri)
                    "image/png"
                }

                is ShareContent.TextWithImage -> {
                    putExtra(Intent.EXTRA_TEXT, content.text)
                    putExtra(Intent.EXTRA_STREAM, content.imageUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    "image/png"
                }
            }
        }

        val contentType = when (content) {
            is ShareContent.Text -> "Text"
            is ShareContent.Image -> "Image"
            is ShareContent.TextWithImage -> "Text with image"
        }
        val chooserIntent = Intent.createChooser(intent, "Share $contentType")


        context.startActivity(chooserIntent)
    }
}

sealed class ShareContent {
    data class Text(val text: String) : ShareContent()

    data class Image(val uri: Uri) : ShareContent()

    data class TextWithImage(val text: String, val imageUri: Uri) : ShareContent()
}
