package dev.meloda.fast.common

import android.app.Application
import androidx.preference.PreferenceManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.skydoves.compose.stability.runtime.ComposeStabilityAnalyzer
import dev.meloda.fast.auth.BuildConfig
import dev.meloda.fast.common.di.applicationModule
import dev.meloda.fast.datastore.AppSettings
import org.acra.config.dialog
import org.acra.config.mailSender
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class AppGlobal : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        AppSettings.init(preferences)
        ComposeStabilityAnalyzer.setEnabled(BuildConfig.DEBUG)

        initKoin()
        initAcra()
    }

    override fun newImageLoader(): ImageLoader = get()

    private fun initKoin() {
        startKoin {
            androidLogger()
            androidContext(this@AppGlobal)
            modules(applicationModule)
        }
    }

    private fun initAcra() {
        initAcra {
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.JSON

            mailSender {
                mailTo = "lischenkodev@gmail.com"
                reportAsFile = true
                reportFileName = "Crash.txt"
            }

            dialog {
                text = "App crashed"
                enabled = true
            }
        }
    }
}
