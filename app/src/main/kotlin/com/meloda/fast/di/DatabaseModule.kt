package com.meloda.fast.di

import androidx.room.Room
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.database.AccountsDatabase
import org.koin.core.scope.Scope
import org.koin.dsl.module

val databaseModule = module {
    single {
        // TODO: 09.08.2023, Danil Nikolaev: write migration to trusted_hash
        Room.databaseBuilder(AppGlobal.Instance, AccountsDatabase::class.java, "accounts")
            .fallbackToDestructiveMigration()
            .build()
    }
    single { accounts().accountsDao }
}
private fun Scope.accounts(): AccountsDatabase = get()
