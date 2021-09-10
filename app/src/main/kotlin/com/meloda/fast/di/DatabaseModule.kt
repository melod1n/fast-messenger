package com.meloda.fast.di

import com.meloda.fast.common.AppGlobal
import com.meloda.fast.database.AppDatabase
import com.meloda.fast.database.dao.ConversationsDao
import com.meloda.fast.database.dao.MessagesDao
import com.meloda.fast.database.dao.UsersDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(): AppDatabase =
        AppGlobal.appDatabase

    @Provides
    @Singleton
    fun provideUsersDao(appDatabase: AppDatabase): UsersDao =
        appDatabase.usersDao()

    @Provides
    @Singleton
    fun provideConversationsDao(appDatabase: AppDatabase): ConversationsDao =
        appDatabase.conversationsDao()

    @Provides
    @Singleton
    fun provideMessagesDao(appDatabase: AppDatabase): MessagesDao =
        appDatabase.messagesDao()

}