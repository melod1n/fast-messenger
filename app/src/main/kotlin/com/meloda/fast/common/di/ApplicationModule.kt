package com.meloda.fast.common.di

import com.meloda.fast.data.account.di.accountModule
import com.meloda.fast.di.databaseModule
import com.meloda.fast.di.networkModule
import com.meloda.fast.di.utilModule
import com.meloda.fast.modules.api.di.apiModule
import com.meloda.fast.modules.auth.di.authModule
import com.meloda.fast.screens.conversations.di.conversationsModule
import com.meloda.fast.screens.languagepicker.di.languagePickerModule
import com.meloda.fast.screens.main.di.mainModule
import com.meloda.fast.screens.messages.di.messagesHistoryModule
import com.meloda.fast.screens.photos.di.photoViewModule
import com.meloda.fast.screens.settings.di.settingsModule
import com.meloda.fast.service.longpolling.di.longPollModule
import org.koin.dsl.module

val applicationModule = module {
    includes(
        databaseModule,
        networkModule,
        apiModule,
        utilModule,
        conversationsModule,
        settingsModule,
        messagesHistoryModule,
        photoViewModule,
        mainModule,
        languagePickerModule,
        longPollModule,
        accountModule,
        authModule
    )
}
