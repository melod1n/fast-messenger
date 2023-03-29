package com.meloda.fast.di

import androidx.room.Room
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.data.account.AccountsDao
import com.meloda.fast.data.conversations.ConversationsDao
import com.meloda.fast.data.groups.GroupsDao
import com.meloda.fast.data.messages.MessagesDao
import com.meloda.fast.data.users.UsersDao
import com.meloda.fast.database.AccountsDatabase
import com.meloda.fast.database.CacheDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import org.koin.dsl.module
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun provideCacheDatabase(): CacheDatabase =
        AppGlobal.cacheDatabase

    @Provides
    @Singleton
    fun provideAccountsDatabase(): AccountsDatabase =
        AppGlobal.accountsDatabase

    @Provides
    @Singleton
    fun provideConversationsDao(cacheDatabase: CacheDatabase): ConversationsDao =
        cacheDatabase.conversationsDao

    @Provides
    @Singleton
    fun provideMessagesDao(cacheDatabase: CacheDatabase): MessagesDao =
        cacheDatabase.messagesDao

    @Provides
    @Singleton
    fun provideUsersDao(cacheDatabase: CacheDatabase): UsersDao =
        cacheDatabase.usersDao

    @Provides
    @Singleton
    fun provideGroupsDao(cacheDatabase: CacheDatabase): GroupsDao =
        cacheDatabase.groupsDao

    @Provides
    @Singleton
    fun provideAccountsDao(accountsDatabase: AccountsDatabase): AccountsDao =
        accountsDatabase.accountsDao

}

val databaseModule = module {
    single {
        Room.databaseBuilder(get(), CacheDatabase::class.java, "cache")
            .fallbackToDestructiveMigration()
            .build()
    }
    single { Room.databaseBuilder(get(), AccountsDatabase::class.java, "accounts").build() }

    single { get<CacheDatabase>().conversationsDao }
    single { get<CacheDatabase>().messagesDao }
    single { get<CacheDatabase>().usersDao }
    single { get<CacheDatabase>().groupsDao }
    single { get<AccountsDatabase>().accountsDao }
}
