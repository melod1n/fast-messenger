package com.meloda.app.fast.database.di

import androidx.room.Room
import com.meloda.app.fast.database.AccountsDatabase
import org.koin.core.scope.Scope
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(get(), AccountsDatabase::class.java, "accounts").build()
    }
    single { get<AccountsDatabase>().accountDao() }

    single {
        Room.databaseBuilder(get(), com.meloda.app.fast.database.CacheDatabase::class.java, "cache")
            .fallbackToDestructiveMigration()
            .build()
    }
    single { cacheDB().userDao() }
    single { cacheDB().groupDao() }
    single { cacheDB().messageDao() }
    single { cacheDB().conversationDao() }
}

private fun Scope.cacheDB(): com.meloda.app.fast.database.CacheDatabase = get()
