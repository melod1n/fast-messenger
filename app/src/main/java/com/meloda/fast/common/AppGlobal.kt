package com.meloda.fast.common

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.database.sqlite.SQLiteDatabase
import android.os.Handler
import android.view.inputmethod.InputMethodManager
import androidx.core.content.pm.PackageInfoCompat
import androidx.preference.PreferenceManager
import com.meloda.fast.BuildConfig
import com.meloda.fast.R
import com.meloda.fast.database.DatabaseHelper
import com.meloda.fast.util.AndroidUtils
import dagger.hilt.android.HiltAndroidApp
import org.acra.ACRA
import org.acra.ReportingInteractionMode
import org.acra.annotation.ReportsCrashes
import java.util.*

@SuppressLint("NonConstantResourceId")
@ReportsCrashes(
    mailTo = "lischenkodev@gmail.com",
    mode = ReportingInteractionMode.DIALOG,
    resDialogTitle = R.string.app_has_been_crashed,
    resDialogText = R.string.empty,
    resDialogTheme = R.style.AppTheme_Dialog,
    resDialogPositiveButtonText = R.string.send_crash_report,
    resDialogNegativeButtonText = R.string.ok
)
@HiltAndroidApp
class AppGlobal : Application() {

    companion object {

        lateinit var inputMethodManager: InputMethodManager

        lateinit var preferences: SharedPreferences
        lateinit var locale: Locale
        lateinit var handler: Handler
        lateinit var resources: Resources
        lateinit var packageName: String
        lateinit var instance: AppGlobal

        lateinit var dbHelper: DatabaseHelper
        lateinit var database: SQLiteDatabase

        lateinit var packageManager: PackageManager

        var versionName = ""
        var versionCode = 0L

        var screenWidth = 0
        var screenHeight = 0

        fun post(runnable: Runnable) {
            handler.post(runnable)
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        if (!BuildConfig.DEBUG) {
            ACRA.init(this)
        }

        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        handler = Handler(mainLooper)
        locale = Locale.getDefault()

        dbHelper = DatabaseHelper(this)
        database = dbHelper.writableDatabase

        val info = packageManager.getPackageInfo(this.packageName, PackageManager.GET_ACTIVITIES)
        versionName = info.versionName
        versionCode = PackageInfoCompat.getLongVersionCode(info)

        Companion.resources = resources
        Companion.packageName = packageName
        Companion.packageManager = packageManager

        screenWidth = AndroidUtils.getDisplayWidth()
        screenHeight = AndroidUtils.getDisplayHeight()

        inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

}