package com.meloda.fast.common

import android.app.Application
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.pm.PackageInfoCompat
import androidx.preference.PreferenceManager
import com.meloda.fast.BuildConfig
import com.meloda.fast.common.di.applicationModule
import com.meloda.fast.screens.settings.SettingsKeys
import com.shakebugs.shake.Shake
import com.vk.recompose.highlighter.RecomposeHighlighterConfig
import com.vk.recompose.logger.RecomposeLoggerConfig
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class AppGlobal : Application() {

    override fun onCreate() {
        super.onCreate()

        instance = this

        initVkomposePlugins()

        val info = packageManager.getPackageInfo(this.packageName, PackageManager.GET_ACTIVITIES)
        versionName = info.versionName
        versionCode = PackageInfoCompat.getLongVersionCode(info).toInt()

        applyDarkTheme()

        initKoin()
        initShake()
    }

    private fun initVkomposePlugins() {
        RecomposeLoggerConfig.isEnabled = false
        RecomposeHighlighterConfig.isEnabled = false
    }

    private fun applyDarkTheme() {
        val nightMode = preferences.getInt(
            SettingsKeys.KEY_APPEARANCE_DARK_THEME,
            SettingsKeys.DEFAULT_VALUE_APPEARANCE_DARK_THEME
        )
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    private fun initKoin() {
        startKoin {
            androidLogger()
            androidContext(this@AppGlobal)
            modules(applicationModule)
        }
    }

    private fun initShake() {
        Shake.setAskForCrashDescription(true)
        Shake.setCrashReportingEnabled(true)
        Shake.start(
            this,
            BuildConfig.shakeClientId,
            BuildConfig.shakeClientSecret
        )
    }

    companion object {
        private lateinit var instance: AppGlobal

        val preferences: SharedPreferences by lazy {
            PreferenceManager.getDefaultSharedPreferences(instance)
        }

        var versionName = ""
        var versionCode = 0

        val Instance: AppGlobal get() = instance
        val resources: Resources get() = Instance.resources
        val packageManager: PackageManager get() = Instance.packageManager
    }
}
