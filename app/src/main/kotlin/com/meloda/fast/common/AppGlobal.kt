package com.meloda.fast.common

import android.annotation.SuppressLint
import android.app.Application
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.database.sqlite.SQLiteDatabase
import android.net.ConnectivityManager
import android.os.Handler
import android.view.inputmethod.InputMethodManager
import androidx.core.content.pm.PackageInfoCompat
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.meloda.fast.BuildConfig
import com.meloda.fast.R
import com.meloda.fast.database.AppDatabase
import com.meloda.fast.database.old.DatabaseHelper
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
        lateinit var connectivityManager: ConnectivityManager
        lateinit var clipboardManager: ClipboardManager

        lateinit var preferences: SharedPreferences
        lateinit var locale: Locale
        lateinit var handler: Handler
        lateinit var resources: Resources
        lateinit var packageName: String
        lateinit var instance: AppGlobal

        lateinit var appDatabase: AppDatabase

        lateinit var dbHelper: DatabaseHelper
        lateinit var oldDatabase: SQLiteDatabase

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

        appDatabase = Room.databaseBuilder(
            this, AppDatabase::class.java, "cache"
        )
            .fallbackToDestructiveMigration()
            .build()

        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        handler = Handler(mainLooper)
        locale = Locale.getDefault()

        dbHelper = DatabaseHelper(this)
        oldDatabase = dbHelper.writableDatabase

        val info = packageManager.getPackageInfo(this.packageName, PackageManager.GET_ACTIVITIES)
        versionName = info.versionName
        versionCode = PackageInfoCompat.getLongVersionCode(info)

        Companion.resources = resources
        Companion.packageName = packageName
        Companion.packageManager = packageManager

        screenWidth = AndroidUtils.getDisplayWidth()
        screenHeight = AndroidUtils.getDisplayHeight()

        inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

}