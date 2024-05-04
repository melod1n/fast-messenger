package com.meloda.fast.di

import androidx.room.Room
import com.meloda.fast.database.AccountsDatabase
import com.meloda.fast.database.CacheDatabase
import org.koin.core.scope.Scope
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(get(), AccountsDatabase::class.java, "accounts").build()
    }
    single { get<AccountsDatabase>().accountsDao() }

    single {
        Room.databaseBuilder(get(), CacheDatabase::class.java, "cache")
            .fallbackToDestructiveMigration()
            .build()
    }
    single { cacheDB().usersDao() }
    single { cacheDB().groupsDao() }
    single { cacheDB().messagesDao() }
    single { cacheDB().conversationsDao() }
}

private fun Scope.cacheDB(): CacheDatabase = get()
