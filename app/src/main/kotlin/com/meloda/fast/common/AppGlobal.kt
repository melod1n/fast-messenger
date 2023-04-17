package com.meloda.fast.common

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.media.AudioManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.pm.PackageInfoCompat
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import com.meloda.fast.common.di.applicationModule
import com.meloda.fast.screens.settings.SettingsFragment
import com.meloda.fast.util.AndroidUtils
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import kotlin.math.roundToInt
import kotlin.properties.Delegates

class AppGlobal : Application() {

    override fun onCreate() {
        super.onCreate()

        instance = this

        preferences = PreferenceManager.getDefaultSharedPreferences(this)

        if (preferences.getBoolean(
                SettingsFragment.KEY_USE_DYNAMIC_COLORS,
                SettingsFragment.DEFAULT_VALUE_USE_DYNAMIC_COLORS
            )
        ) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }

        val info = packageManager.getPackageInfo(this.packageName, PackageManager.GET_ACTIVITIES)
        versionName = info.versionName
        versionCode = PackageInfoCompat.getLongVersionCode(info).toInt()

        screenWidth80 = (AndroidUtils.getDisplayWidth() * 0.8).roundToInt()

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        applyDarkTheme()

        initKoin()
    }

    private fun applyDarkTheme() {
        val nightMode = preferences.getInt(
            SettingsFragment.KEY_APPEARANCE_DARK_THEME,
            SettingsFragment.DEFAULT_VALUE_APPEARANCE_DARK_THEME
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

    companion object {
        private lateinit var instance: AppGlobal

        var preferences: SharedPreferences by Delegates.notNull()

        var versionName = ""
        var versionCode = 0
        var screenWidth80 = 0

        val Instance: AppGlobal get() = instance
        val resources: Resources get() = Instance.resources
        val packageManager: PackageManager get() = Instance.packageManager

        var audioManager: AudioManager by Delegates.notNull()
    }
}
