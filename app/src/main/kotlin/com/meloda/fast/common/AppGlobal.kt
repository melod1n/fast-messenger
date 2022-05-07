package com.meloda.fast.common

import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.ConnectivityManager
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.core.content.pm.PackageInfoCompat
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.meloda.fast.BuildConfig
import com.meloda.fast.R
import com.meloda.fast.database.AppDatabase
import dagger.hilt.android.HiltAndroidApp
import org.acra.ACRA
import org.acra.config.dialog
import org.acra.config.mailSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import kotlin.math.sqrt

@HiltAndroidApp
class AppGlobal : Application() {

    companion object {

        lateinit var inputMethodManager: InputMethodManager
        lateinit var connectivityManager: ConnectivityManager
        lateinit var clipboardManager: ClipboardManager

        lateinit var preferences: SharedPreferences
        lateinit var resources: Resources
        lateinit var packageName: String
        private lateinit var instance: AppGlobal

        lateinit var appDatabase: AppDatabase

        lateinit var packageManager: PackageManager

        var versionName = ""
        var versionCode = 0L

        var screenWidth = 0
        var screenHeight = 0

        val Instance get() = instance
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        initAcra {
            //core configuration:
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.JSON

            alsoReportToAndroidFramework = true

            mailSender {
                mailTo = "lischenkodev@gmail.com"

                reportFileName = "stacktrace.json"
            }

            dialog {
                resIcon = 0
                resTheme = R.style.AppTheme_Alert

                title = getString(R.string.warning)
                text = getString(R.string.app_crash_occurred)
                commentPrompt = getString(R.string.app_crash_comment_prompt)

                positiveButtonText = getString(R.string.app_crash_report)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        if (!BuildConfig.DEBUG) {
            ACRA.init(this)
        }

        appDatabase = Room.databaseBuilder(this, AppDatabase::class.java, "cache")
            .fallbackToDestructiveMigration()
            .build()

        preferences = PreferenceManager.getDefaultSharedPreferences(this)

        val info = packageManager.getPackageInfo(this.packageName, PackageManager.GET_ACTIVITIES)
        versionName = info.versionName
        versionCode = PackageInfoCompat.getLongVersionCode(info)

        Companion.resources = resources
        Companion.packageName = packageName
        Companion.packageManager = packageManager

        screenWidth = resources.displayMetrics.widthPixels
        screenHeight = resources.displayMetrics.heightPixels

        val density = resources.displayMetrics.density
        val densityDpi = resources.displayMetrics.densityDpi
        val densityScaled = resources.displayMetrics.scaledDensity
        val xDpi = resources.displayMetrics.xdpi
        val yDpi = resources.displayMetrics.ydpi

        val diagonal = sqrt(
            (screenWidth * screenWidth - screenHeight * screenHeight).toFloat()
        )

        Log.i(
            "Fast::DeviceInfo",
            "width: $screenWidth; height: $screenHeight; density: $density; diagonal: $diagonal; dpiDensity: $densityDpi; scaledDensity: $densityScaled; xDpi: $xDpi; yDpi: $yDpi"
        )

        inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }
}