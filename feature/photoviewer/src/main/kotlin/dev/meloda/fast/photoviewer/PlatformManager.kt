package dev.meloda.fast.photoviewer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import dev.meloda.fast.logger.FastLogger
import java.io.File

interface PlatformManager {
    fun openBrowser(url: String): Boolean
    fun shareFile(filePath: String): Boolean
    fun copyFile(filePath: String, label: String = ""): Boolean
    fun copyString(string: String, label: String = ""): Boolean
}

class PlatformManagerImpl(
    private val context: Context,
    private val logger: FastLogger,
    private val clipboardManager: ClipboardManager
) : PlatformManager {

    override fun openBrowser(url: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.applicationContext.startActivity(intent)
            true
        } catch (e: Exception) {
            logger.error(PlatformManager::class, "Error opening browser for url: $url", e)
            false
        }
    }

    override fun shareFile(filePath: String): Boolean {
        return try {
            val imageFile = File(filePath)
            val applicationContext = context.applicationContext

            val uri = FileProvider.getUriForFile(
                applicationContext,
                "${applicationContext.packageName}.provider",
                imageFile
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = ClipData.newRawUri(null, uri)
            }

            val chooserIntent = Intent.createChooser(intent, "Share via...")
            chooserIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            applicationContext.startActivity(chooserIntent)
            true
        } catch (e: Exception) {
            logger.error(PlatformManager::class, "Error share file: $filePath", e)
            false
        }
    }

    override fun copyFile(filePath: String, label: String): Boolean {
        return try {
            val file = File(filePath)
            val applicationContext = context.applicationContext

            val uri = FileProvider.getUriForFile(
                applicationContext,
                "${applicationContext.packageName}.provider",
                file
            )

            val clip = ClipData.newUri(applicationContext.contentResolver, label, uri)
            clipboardManager.setPrimaryClip(clip)
            true
        } catch (e: Exception) {
            logger.error(PlatformManager::class, "Error copy file: $filePath", e)
            false
        }
    }

    override fun copyString(string: String, label: String): Boolean {
        return try {
            clipboardManager.setPrimaryClip(ClipData.newPlainText(label, string))
            true
        } catch (e: Exception) {
            logger.error(PlatformManager::class, "Error copy string: $string", e)
            false
        }
    }
}
