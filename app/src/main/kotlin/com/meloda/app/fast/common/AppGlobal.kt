package com.meloda.app.fast.common

import android.app.Application
import androidx.preference.PreferenceManager
import com.meloda.app.fast.common.di.applicationModule
import com.meloda.app.fast.datastore.SettingsController
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class AppGlobal : Application() {

    override fun onCreate() {
        super.onCreate()

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        SettingsController.init(preferences)
        UserConfig.init(preferences)

        initKoin()
    }

    private fun initKoin() {
        startKoin {
            androidLogger()
            androidContext(this@AppGlobal)
            modules(applicationModule)
        }
    }
}
