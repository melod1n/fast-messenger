package com.meloda.app.fast.data.db

import com.meloda.app.fast.model.database.AccountEntity

interface AccountsRepository {

    suspend fun getAccounts(): List<AccountEntity>

    suspend fun storeAccounts(accounts: List<AccountEntity>)
}
