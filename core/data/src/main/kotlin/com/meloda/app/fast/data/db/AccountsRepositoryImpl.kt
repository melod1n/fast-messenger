package com.meloda.app.fast.data.db

import com.meloda.app.fast.database.dao.AccountDao
import com.meloda.app.fast.model.database.AccountEntity

class AccountsRepositoryImpl(
    private val accountDao: AccountDao
) : AccountsRepository {

    override suspend fun getAccounts(): List<AccountEntity> = accountDao.getAll()

    override suspend fun storeAccounts(
        accounts: List<AccountEntity>
    ) = accountDao.insertAll(accounts)
}
