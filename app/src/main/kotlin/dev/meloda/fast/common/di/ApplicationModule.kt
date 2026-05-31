package dev.meloda.fast.common.di

import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import dev.meloda.fast.MainViewModel
import dev.meloda.fast.auth.authModule
import dev.meloda.fast.chatmaterials.di.chatMaterialsModule
import dev.meloda.fast.common.LongPollController
import dev.meloda.fast.common.LongPollControllerImpl
import dev.meloda.fast.common.NetworkStateListener
import dev.meloda.fast.common.provider.Provider
import dev.meloda.fast.common.provider.ResourceProvider
import dev.meloda.fast.common.provider.ResourceProviderImpl
import dev.meloda.fast.convos.di.convosModule
import dev.meloda.fast.convos.di.createChatModule
import dev.meloda.fast.domain.di.domainModule
import dev.meloda.fast.friends.di.friendsModule
import dev.meloda.fast.languagepicker.di.languagePickerModule
import dev.meloda.fast.logger.loggerModule
import dev.meloda.fast.messageshistory.di.messagesHistoryModule
import dev.meloda.fast.photoviewer.di.photoViewModule
import dev.meloda.fast.presentation.NetworkObserver
import dev.meloda.fast.profile.di.profileModule
import dev.meloda.fast.provider.ApiLanguageProvider
import dev.meloda.fast.service.longpolling.di.longPollModule
import dev.meloda.fast.settings.di.settingsModule
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.qualifier
import org.koin.dsl.bind
import org.koin.dsl.module

@OptIn(ExperimentalCoilApi::class)
val applicationModule = module {
    includes(domainModule)
    includes(
        authModule,
        convosModule,
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

    includes(loggerModule)
    includes(androidModule)

    factoryOf(::ApiLanguageProvider) bind Provider::class

    viewModelOf(::MainViewModel) { qualifier = qualifier("main") }

    single<ImageLoader> {
        ImageLoader.Builder(get())
            .crossfade(true)
            .build()
            .also {
                it.diskCache?.directory?.toFile()?.listFiles()
            }
    }

    singleOf(::LongPollControllerImpl) bind LongPollController::class
    singleOf(::ResourceProviderImpl) bind ResourceProvider::class

    singleOf(::NetworkStateListener)
    singleOf(::NetworkObserver) { qualifier = qualifier("main") }
}
