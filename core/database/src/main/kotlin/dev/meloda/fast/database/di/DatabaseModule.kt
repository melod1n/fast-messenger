package dev.meloda.fast.database.di

import androidx.room.Room
import dev.meloda.fast.database.AccountsDatabase
import dev.meloda.fast.database.CacheDatabase
import dev.meloda.fast.database.di.migration.migrationFrom2To3
import org.koin.core.scope.Scope
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(get(), AccountsDatabase::class.java, "accounts")
            .addMigrations(migrationFrom2To3)
            .build()
    }
    single { get<AccountsDatabase>().accountDao() }

    single {
        Room.databaseBuilder(get(), CacheDatabase::class.java, "cache")
            .fallbackToDestructiveMigration(true)
            .build()
    }
    single { cacheDB().userDao() }
    single { cacheDB().groupDao() }
    single { cacheDB().messageDao() }
    single { cacheDB().conversationDao() }
}

private fun Scope.cacheDB(): CacheDatabase = get()
