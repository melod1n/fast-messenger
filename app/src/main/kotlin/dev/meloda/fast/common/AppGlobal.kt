package dev.meloda.fast.common

import android.app.Application
import androidx.preference.PreferenceManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.skydoves.compose.stability.runtime.ComposeStabilityAnalyzer
import dev.meloda.fast.auth.BuildConfig
import dev.meloda.fast.common.di.applicationModule
import dev.meloda.fast.datastore.AppSettings
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class AppGlobal : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        AppSettings.init(preferences)

        initKoin()

        ComposeStabilityAnalyzer.setEnabled(BuildConfig.DEBUG)
    }

    private fun initKoin() {
        startKoin {
            androidLogger()
            androidContext(this@AppGlobal)
            modules(applicationModule)
        }
    }

    override fun newImageLoader(): ImageLoader = get()
}
