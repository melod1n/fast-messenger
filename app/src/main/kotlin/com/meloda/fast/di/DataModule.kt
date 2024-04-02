package com.meloda.fast.di

import com.meloda.fast.data.account.AccountsRepository
import com.meloda.fast.data.audios.AudiosRepository
import com.meloda.fast.data.auth.AuthRepository
import com.meloda.fast.data.files.FilesRepository
import com.meloda.fast.data.messages.data.repository.MessagesRepositoryImpl
import com.meloda.fast.data.photos.PhotosRepository
import com.meloda.fast.data.users.UsersRepository
import com.meloda.fast.data.videos.VideosRepository
import com.meloda.fast.screens.conversations.data.repository.ConversationsRepositoryImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

// TODO: 17.04.2023, Danil Nikolaev: use specific repositories in local DI modules
val dataModule = module {
    singleOf(::ConversationsRepositoryImpl)
    singleOf(::MessagesRepositoryImpl)
    singleOf(::UsersRepository)
    singleOf(::AuthRepository)
    singleOf(::AccountsRepository)
    singleOf(::PhotosRepository)
    singleOf(::VideosRepository)
    singleOf(::AudiosRepository)
    singleOf(::FilesRepository)
}
