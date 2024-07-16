package dev.meloda.fast.common.di

import android.content.Context
import android.content.res.Resources
import android.os.PowerManager
import androidx.preference.PreferenceManager
import dev.meloda.fast.MainViewModelImpl
import dev.meloda.fast.auth.authModule
import dev.meloda.fast.chatmaterials.di.chatMaterialsModule
import dev.meloda.fast.common.provider.Provider
import dev.meloda.fast.conversations.di.conversationsModule
import dev.meloda.fast.data.di.dataModule
import dev.meloda.fast.friends.di.friendsModule
import dev.meloda.fast.languagepicker.di.languagePickerModule
import dev.meloda.fast.messageshistory.di.messagesHistoryModule
import dev.meloda.fast.photoviewer.di.photoViewModule
import dev.meloda.fast.profile.di.profileModule
import dev.meloda.fast.provider.ApiLanguageProvider
import dev.meloda.fast.service.longpolling.di.longPollModule
import dev.meloda.fast.settings.di.settingsModule
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.qualifier
import org.koin.dsl.bind
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
        friendsModule,
        profileModule,
        chatMaterialsModule
    )

    // TODO: 14/05/2024, Danil Nikolaev: research on memory leaks and potentials errors
    // TODO: 14/05/2024, Danil Nikolaev: extract all operations with preferences to standalone class
    singleOf(PreferenceManager::getDefaultSharedPreferences)
    single<Resources> { androidContext().resources }
    factory<PowerManager> { androidContext().getSystemService(Context.POWER_SERVICE) as PowerManager }

    singleOf(::ApiLanguageProvider) bind Provider::class

    viewModelOf(::MainViewModelImpl) {
        qualifier = qualifier("main")
    }
}
