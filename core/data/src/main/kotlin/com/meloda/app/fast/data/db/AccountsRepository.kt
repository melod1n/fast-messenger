package dev.meloda.fast.data.db

import dev.meloda.fast.model.database.AccountEntity

interface AccountsRepository {

    suspend fun getAccounts(): List<AccountEntity>

    suspend fun getAccountById(userId: Int): AccountEntity?

    suspend fun storeAccounts(accounts: List<AccountEntity>)
}
