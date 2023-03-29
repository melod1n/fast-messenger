package com.meloda.fast.common

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.pm.PackageInfoCompat
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.google.android.material.color.DynamicColors
import com.meloda.fast.database.AccountsDatabase
import com.meloda.fast.database.CacheDatabase
import com.meloda.fast.di.*
import com.meloda.fast.screens.captcha.di.captchaModule
import com.meloda.fast.screens.login.di.loginModule
import com.meloda.fast.screens.settings.SettingsFragment
import com.meloda.fast.screens.twofa.di.twoFaModule
import com.meloda.fast.util.AndroidUtils
import dagger.hilt.android.HiltAndroidApp
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindInstance
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import kotlin.math.roundToInt
import kotlin.properties.Delegates

@HiltAndroidApp
class AppGlobal : Application(), DIAware {

    override val di = appModule

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

        screenWidth80 = (AndroidUtils.getDisplayWidth() * 0.8).roundToInt()

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
            modules(
                databaseModule,
                dataModule,
                navigationModule,
                networkModule,
                otaModule,
                loginModule
            )
        }
    }

    companion object {
        private lateinit var instance: AppGlobal

        val appModule = DI.lazy {
            bindInstance<Context> { instance }
            bindInstance { preferences }
        }

        var preferences: SharedPreferences by Delegates.notNull()

        var cacheDatabase: CacheDatabase by Delegates.notNull()
        var accountsDatabase: AccountsDatabase by Delegates.notNull()

        var versionName = ""
        var versionCode = 0
        var screenWidth80 = 0

        val Instance get() = instance
        val resources get() = Instance.resources
        val packageManager get() = Instance.packageManager
    }
}
