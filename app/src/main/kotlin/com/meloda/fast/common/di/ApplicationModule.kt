package com.meloda.fast.common.di

import com.meloda.fast.di.apiModule
import com.meloda.fast.di.dataModule
import com.meloda.fast.di.databaseModule
import com.meloda.fast.di.navigationModule
import com.meloda.fast.di.networkModule
import com.meloda.fast.di.otaModule
import com.meloda.fast.screens.captcha.di.captchaModule
import com.meloda.fast.screens.conversations.di.conversationsModule
import com.meloda.fast.screens.login.di.loginModule
import com.meloda.fast.screens.main.di.mainModule
import com.meloda.fast.screens.messages.di.messagesHistoryModule
import com.meloda.fast.screens.photos.di.photoViewModule
import com.meloda.fast.screens.settings.UserSettings
import com.meloda.fast.screens.settings.UserSettingsImpl
import com.meloda.fast.screens.settings.di.settingsModule
import com.meloda.fast.screens.twofa.di.twoFaModule
import com.meloda.fast.screens.updates.di.updatesModule
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val applicationModule = module {
    includes(
        navigationModule,
        databaseModule,
        dataModule,
        otaModule,
        networkModule,
        apiModule,
        loginModule,
        twoFaModule,
        captchaModule,
        conversationsModule,
        settingsModule,
        updatesModule,
        messagesHistoryModule,
        photoViewModule,
        mainModule
    )

    singleOf(::UserSettingsImpl) bind UserSettings::class
}
