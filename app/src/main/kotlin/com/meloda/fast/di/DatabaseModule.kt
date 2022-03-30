package com.meloda.fast.di

import com.meloda.fast.common.AppGlobal
import com.meloda.fast.database.AppDatabase
import com.meloda.fast.database.dao.*
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
    fun provideAccountsDao(appDatabase: AppDatabase): AccountsDao =
        appDatabase.accountsDao

    @Provides
    @Singleton
    fun provideConversationsDao(appDatabase: AppDatabase): ConversationsDao =
        appDatabase.conversationsDao()

    @Provides
    @Singleton
    fun provideMessagesDao(appDatabase: AppDatabase): MessagesDao =
        appDatabase.messagesDao()

    @Provides
    @Singleton
    fun provideUsersDao(appDatabase: AppDatabase): UsersDao =
        appDatabase.usersDao()

    @Provides
    @Singleton
    fun provideGroupsDao(appDatabase: AppDatabase): GroupsDao =
        appDatabase.groupsDao()

}