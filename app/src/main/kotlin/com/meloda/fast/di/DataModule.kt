package com.meloda.fast.di

import com.meloda.fast.data.account.AccountApi
import com.meloda.fast.data.account.AccountsDao
import com.meloda.fast.data.account.AccountsRepository
import com.meloda.fast.data.audios.AudiosApi
import com.meloda.fast.data.audios.AudiosRepository
import com.meloda.fast.data.auth.AuthApi
import com.meloda.fast.data.auth.AuthRepository
import com.meloda.fast.data.conversations.ConversationsApi
import com.meloda.fast.data.conversations.ConversationsDao
import com.meloda.fast.data.conversations.ConversationsRepository
import com.meloda.fast.data.files.FilesApi
import com.meloda.fast.data.files.FilesRepository
import com.meloda.fast.data.groups.GroupsDao
import com.meloda.fast.data.groups.GroupsRepository
import com.meloda.fast.data.longpoll.LongPollApi
import com.meloda.fast.data.messages.MessagesApi
import com.meloda.fast.data.messages.MessagesDao
import com.meloda.fast.data.messages.MessagesRepository
import com.meloda.fast.data.photos.PhotosApi
import com.meloda.fast.data.photos.PhotosRepository
import com.meloda.fast.data.users.UsersApi
import com.meloda.fast.data.users.UsersDao
import com.meloda.fast.data.users.UsersRepository
import com.meloda.fast.data.videos.VideosApi
import com.meloda.fast.data.videos.VideosRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.koin.dsl.module
import javax.inject.Singleton

@Deprecated("use specific repositories in local DI modules")
@InstallIn(SingletonComponent::class)
@Module
object DataModule {

    @Singleton
    @Provides
    fun provideConversationsRepository(
        conversationsApi: ConversationsApi,
        conversationsDao: ConversationsDao
    ): ConversationsRepository = ConversationsRepository(conversationsApi, conversationsDao)

    @Singleton
    @Provides
    fun provideMessagesRepository(
        messagesApi: MessagesApi,
        messagesDao: MessagesDao,
        longPollApi: LongPollApi
    ): MessagesRepository = MessagesRepository(messagesApi, messagesDao, longPollApi)

    @Singleton
    @Provides
    fun provideUsersRepository(
        usersApi: UsersApi,
        usersDao: UsersDao
    ): UsersRepository = UsersRepository(usersApi, usersDao)

    @Singleton
    @Provides
    fun provideGroupsRepository(
        groupsDao: GroupsDao
    ): GroupsRepository = GroupsRepository(groupsDao)

    @Singleton
    @Provides
    fun provideAuthRepository(
        authApi: AuthApi
    ): AuthRepository = AuthRepository(authApi)

    @Singleton
    @Provides
    fun provideAccountsRepository(
        accountApi: AccountApi,
        accountsDao: AccountsDao
    ): AccountsRepository = AccountsRepository(accountApi, accountsDao)

    @Singleton
    @Provides
    fun providePhotosRepository(
        photosApi: PhotosApi
    ): PhotosRepository = PhotosRepository(photosApi)

    @Singleton
    @Provides
    fun provideVideosRepository(
        videosApi: VideosApi
    ): VideosRepository = VideosRepository(videosApi)

    @Singleton
    @Provides
    fun provideAudiosRepository(
        audiosApi: AudiosApi
    ): AudiosRepository = AudiosRepository(audiosApi)

    @Singleton
    @Provides
    fun provideFilesRepository(
        filesApi: FilesApi
    ): FilesRepository = FilesRepository(filesApi)
}

val dataModule = module {
    single { AuthRepository(get()) }
}
