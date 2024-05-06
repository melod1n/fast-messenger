package com.meloda.app.fast.common.di

import com.meloda.app.fast.MainViewModelImpl
import com.meloda.app.fast.auth.di.authModule
import com.meloda.app.fast.conversations.di.conversationsModule
import com.meloda.app.fast.data.di.dataModule
import com.meloda.app.fast.languagepicker.di.languagePickerModule
import com.meloda.app.fast.messageshistory.di.messagesHistoryModule
import com.meloda.app.fast.photoviewer.di.photoViewModule
import com.meloda.app.fast.service.longpolling.di.longPollModule
import com.meloda.app.fast.settings.di.settingsModule
import org.koin.androidx.viewmodel.dsl.viewModelOf
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

    viewModelOf(::MainViewModelImpl) {
        qualifier = qualifier("main")
    }
}
