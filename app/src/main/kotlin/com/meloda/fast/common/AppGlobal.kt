package com.meloda.fast.common

import android.app.Application
import android.app.DownloadManager
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.ConnectivityManager
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.pm.PackageInfoCompat
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.google.android.material.color.DynamicColors
import com.meloda.fast.database.AccountsDatabase
import com.meloda.fast.database.CacheDatabase
import com.meloda.fast.screens.settings.SettingsFragment
import dagger.hilt.android.HiltAndroidApp
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.properties.Delegates

@HiltAndroidApp
class AppGlobal : Application() {

    companion object {

        lateinit var inputMethodManager: InputMethodManager
        lateinit var connectivityManager: ConnectivityManager
        lateinit var clipboardManager: ClipboardManager
        lateinit var downloadManager: DownloadManager

        var preferences: SharedPreferences by Delegates.notNull()
        lateinit var resources: Resources
        lateinit var packageName: String
        private lateinit var instance: AppGlobal

        lateinit var cacheDatabase: CacheDatabase
        lateinit var accountsDatabase: AccountsDatabase

        lateinit var packageManager: PackageManager

        var versionName = ""
        var versionCode = 0

        var screenWidth = 0
        var screenHeight = 0

        var screenWidth80 = 0

        val Instance get() = instance
    }

    override fun onCreate() {
        super.onCreate()

        instance = this

        cacheDatabase = Room.databaseBuilder(this, CacheDatabase::class.java, "cache")
            .fallbackToDestructiveMigration()
            .build()

        accountsDatabase = Room.databaseBuilder(this, AccountsDatabase::class.java, "accounts")
            .build()

        preferences = PreferenceManager.getDefaultSharedPreferences(this)

        if (preferences.getBoolean(
                SettingsFragment.KEY_DEBUG_TEST_THEME,
                SettingsFragment.DEFAULT_VALUE_DEBUG_TEST_THEME
            )
        ) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }

        val info = packageManager.getPackageInfo(this.packageName, PackageManager.GET_ACTIVITIES)
        versionName = info.versionName
        versionCode = PackageInfoCompat.getLongVersionCode(info).toInt()

        Companion.resources = resources
        Companion.packageName = packageName
        Companion.packageManager = packageManager

        screenWidth = resources.displayMetrics.widthPixels
        screenHeight = resources.displayMetrics.heightPixels

        screenWidth80 = (screenWidth * 0.8).roundToInt()

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
            "width: $screenWidth; 70% width: $screenWidth80; height: $screenHeight; density: $density; diagonal: $diagonal; dpiDensity: $densityDpi; scaledDensity: $densityScaled; xDpi: $xDpi; yDpi: $yDpi"
        )

        inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        applyDarkTheme()
    }

    private fun applyDarkTheme() {
        val nightMode = preferences.getInt(
            SettingsFragment.KEY_APPEARANCE_DARK_THEME,
            SettingsFragment.DEFAULT_VALUE_APPEARANCE_DARK_THEME
        )
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}
