package com.meloda.fast.di

import androidx.room.Room
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.database.AccountsDatabase
import com.meloda.fast.database.CacheDatabase
import org.koin.core.scope.Scope
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(AppGlobal.Instance, CacheDatabase::class.java, "cache")
            .fallbackToDestructiveMigration()
            .build()
    }
    single {
        Room.databaseBuilder(AppGlobal.Instance, AccountsDatabase::class.java, "accounts")
            .build()
    }
    single { cache().conversationsDao }
    single { cache().messagesDao }
    single { cache().usersDao }
    single { cache().groupsDao }
    single { accounts().accountsDao }
}

private fun Scope.cache(): CacheDatabase = get()
private fun Scope.accounts(): AccountsDatabase = get()
