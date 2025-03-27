package dev.meloda.fast.database.di

import androidx.room.Room
import dev.meloda.fast.database.AccountsDatabase
import dev.meloda.fast.database.CacheDatabase
import org.koin.core.scope.Scope
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(get(), AccountsDatabase::class.java, "accounts").build()
    }
    single { get<AccountsDatabase>().accountDao() }

    single {
        Room.databaseBuilder(get(), CacheDatabase::class.java, "cache")
            .fallbackToDestructiveMigration()
            .build()
    }
    single { cacheDB().userDao() }
    single { cacheDB().groupDao() }
    single { cacheDB().messageDao() }
    single { cacheDB().conversationDao() }
}

private fun Scope.cacheDB(): CacheDatabase = get()
