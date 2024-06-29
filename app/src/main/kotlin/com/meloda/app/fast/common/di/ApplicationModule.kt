package com.meloda.app.fast.common.di

import android.content.Context
import android.content.res.Resources
import android.os.PowerManager
import androidx.preference.PreferenceManager
import com.meloda.app.fast.MainViewModelImpl
import com.meloda.app.fast.auth.di.authModule
import com.meloda.app.fast.conversations.di.conversationsModule
import com.meloda.app.fast.data.di.dataModule
import com.meloda.app.fast.datastore.UserConfig
import com.meloda.app.fast.languagepicker.di.languagePickerModule
import com.meloda.app.fast.messageshistory.di.messagesHistoryModule
import com.meloda.app.fast.photoviewer.di.photoViewModule
import com.meloda.app.fast.service.longpolling.di.longPollModule
import com.meloda.app.fast.settings.di.settingsModule
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.core.qualifier.qualifier
import org.koin.dsl.module

val applicationModule = module {
    includes(dataModule)
    includes(
        authModule,
        conversationsModule,
        settingsModule,
        messagesHistoryModule,
        photoViewModule,
        languagePickerModule,
        longPollModule,
    )

    // TODO: 14/05/2024, Danil Nikolaev: research on memory leaks and potentials errors
    // TODO: 14/05/2024, Danil Nikolaev: extract all operations with preferences to standalone class
    singleOf(PreferenceManager::getDefaultSharedPreferences)
    single<Resources> { get<Context>().resources }
    factory<PowerManager> { get<Context>().getSystemService(Context.POWER_SERVICE) as PowerManager }

    factory<String>(named("token")) { UserConfig.accessToken }

    viewModelOf(::MainViewModelImpl) {
        qualifier = qualifier("main")
    }
}
