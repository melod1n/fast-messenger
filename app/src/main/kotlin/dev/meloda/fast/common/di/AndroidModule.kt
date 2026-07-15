package dev.meloda.fast.common.di

import android.content.ClipboardManager
import android.content.Context
import android.content.res.Resources
import android.net.ConnectivityManager
import android.os.PowerManager
import androidx.preference.PreferenceManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val androidModule = module {
    // TODO: 14/05/2024, Danil Nikolaev: extract all operations with preferences to standalone class
    factoryOf(PreferenceManager::getDefaultSharedPreferences)
    factory<Resources> { androidContext().resources }
    factory<PowerManager> { androidContext().getSystemService(Context.POWER_SERVICE) as PowerManager }
    factory<ConnectivityManager> { androidContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
    factory<ClipboardManager> { androidContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
}
