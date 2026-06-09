package dev.meloda.fast.common

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.preference.PreferenceManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.skydoves.compose.stability.runtime.ComposeStabilityAnalyzer
import dev.meloda.fast.auth.BuildConfig
import dev.meloda.fast.common.di.applicationModule
import dev.meloda.fast.datastore.AppSettings
import dev.meloda.fast.logger.FastLogLevel
import dev.meloda.fast.logger.FastLogger
import dev.meloda.fast.presentation.CrashActivity
import dev.meloda.fast.presentation.NetworkObserver
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import java.io.File
import java.io.FileOutputStream
import kotlin.system.exitProcess

class AppGlobal : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        AppSettings.init(preferences)
        ComposeStabilityAnalyzer.setEnabled(BuildConfig.DEBUG)

        initKoin()
        initCrashHandler()

        val logLevel =
            if (BuildConfig.DEBUG) FastLogLevel.DEBUG
            else FastLogLevel.ERROR

        get<FastLogger>()
            .apply { setLogLevel(logLevel) }
            .also { FastLogger.setInstance(it) }

        get<NetworkObserver>().start()
    }

    override fun newImageLoader(): ImageLoader = get()

    private fun initKoin() {
        startKoin {
            androidLogger()
            androidContext(this@AppGlobal)
            modules(applicationModule)
        }
    }

    private fun initCrashHandler() {
        val defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val crashLogsDirectory = File(filesDir.absolutePath + "/crashlogs")
            if (!crashLogsDirectory.exists()) {
                crashLogsDirectory.mkdirs()
            }

            val crashLogFileName = "crash_" + System.currentTimeMillis() + ".txt"
            val crashLogFile = File(crashLogsDirectory.absolutePath + "/" + crashLogFileName)

            FileOutputStream(crashLogFile).use { stream ->
                stream.write(throwable.stackTraceToString().toByteArray())
            }

            if (AppSettings.Debug.showAlertAfterCrash) {
                try {
                    val intent = Intent(this, CrashActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    intent.putExtra("CRASH_LOG_FILE_URI", Uri.fromFile(crashLogFile))
                    startActivity(intent)

                    exitProcess(0)
                } catch (e: Exception) {
                    if (e !is RuntimeException) {
                        defaultExceptionHandler?.uncaughtException(thread, throwable)
                    }
                }
            } else {
                defaultExceptionHandler?.uncaughtException(thread, throwable)
            }
        }
    }
}
