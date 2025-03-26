package dev.meloda.fast.common.di

import android.content.Context
import android.content.res.Resources
import android.os.PowerManager
import androidx.preference.PreferenceManager
import coil.ImageLoader
import dev.meloda.fast.MainViewModelImpl
import dev.meloda.fast.auth.captcha.di.captchaModule
import dev.meloda.fast.auth.login.di.loginModule
import dev.meloda.fast.auth.validation.di.validationModule
import dev.meloda.fast.chatmaterials.di.chatMaterialsModule
import dev.meloda.fast.common.LongPollController
import dev.meloda.fast.common.LongPollControllerImpl
import dev.meloda.fast.common.provider.Provider
import dev.meloda.fast.common.provider.ResourceProvider
import dev.meloda.fast.common.provider.ResourceProviderImpl
import dev.meloda.fast.conversations.di.conversationsModule
import dev.meloda.fast.conversations.di.createChatModule
import dev.meloda.fast.domain.di.domainModule
import dev.meloda.fast.friends.di.friendsModule
import dev.meloda.fast.languagepicker.di.languagePickerModule
import dev.meloda.fast.messageshistory.di.messagesHistoryModule
import dev.meloda.fast.photoviewer.di.photoViewModule
import dev.meloda.fast.profile.di.profileModule
import dev.meloda.fast.provider.ApiLanguageProvider
import dev.meloda.fast.service.longpolling.di.longPollModule
import dev.meloda.fast.settings.di.settingsModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.qualifier
import org.koin.dsl.bind
import org.koin.dsl.module

val applicationModule = module {
    includes(domainModule)
    includes(
        loginModule,
        validationModule,
        captchaModule,
        conversationsModule,
        settingsModule,
        messagesHistoryModule,
        photoViewModule,
        languagePickerModule,
        longPollModule,
        friendsModule,
        profileModule,
        chatMaterialsModule,
        createChatModule
    )

    // TODO: 14/05/2024, Danil Nikolaev: extract all operations with preferences to standalone class
    singleOf(PreferenceManager::getDefaultSharedPreferences)
    single<Resources> { androidContext().resources }
    factory<PowerManager> { androidContext().getSystemService(Context.POWER_SERVICE) as PowerManager }

    singleOf(::ApiLanguageProvider) bind Provider::class

    viewModelOf(::MainViewModelImpl) {
        qualifier = qualifier("main")
    }

    single<ImageLoader> {
        ImageLoader.Builder(get())
            .crossfade(true)
            .build()
    }

    singleOf(::LongPollControllerImpl) bind LongPollController::class
    singleOf(::ResourceProviderImpl) bind ResourceProvider::class
}
