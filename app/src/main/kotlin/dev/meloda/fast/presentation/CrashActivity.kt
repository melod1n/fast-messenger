package dev.meloda.fast.presentation

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import dev.meloda.fast.datastore.UserSettings
import dev.meloda.fast.ui.theme.AppTheme
import dev.meloda.fast.ui.util.isNeedToEnableDarkMode
import org.koin.compose.koinInject
import java.io.File

class CrashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val crashLogFileUri = intent.getParcelableExtra<Uri>("CRASH_LOG_FILE_URI") ?: run {
            finish()
            return
        }
        val crashLogFile = crashLogFileUri.toFile().takeIf(File::exists) ?: run {
            finish()
            return
        }

        val stacktrace = crashLogFile.bufferedReader().readText()

        setContent {
            val userSettings: UserSettings = koinInject()

            AppTheme(
                useDarkTheme = isNeedToEnableDarkMode(darkMode = userSettings.darkMode.collectAsState().value),
                useDynamicColors = userSettings.enableDynamicColors.collectAsState().value,
                selectedColorScheme = 0,
                useAmoledBackground = userSettings.enableAmoledDark.collectAsState().value,
                useSystemFont = userSettings.useSystemFont.collectAsState().value
            ) {
                AppCrashedDialog(
                    stacktrace = stacktrace,
                    onDismiss = { finish() },
                    onShare = {
                        val uri = FileProvider.getUriForFile(
                            this,
                            "$packageName.provider",
                            crashLogFile
                        )

                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "text/plain"
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            putExtra(Intent.EXTRA_STREAM, uri)
                            clipData = ClipData.newRawUri(null, uri)
                        }

                        val chooserIntent = Intent.createChooser(sendIntent, null)
                        chooserIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        startActivity(chooserIntent)
                    }
                )
            }
        }
    }
}
