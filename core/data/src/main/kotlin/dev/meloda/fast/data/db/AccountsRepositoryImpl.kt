package dev.meloda.fast.data.db

import dev.meloda.fast.database.dao.AccountDao
import dev.meloda.fast.model.database.AccountEntity

class AccountsRepositoryImpl(
    private val accountDao: AccountDao
) : AccountsRepository {

    override suspend fun getAccounts(): List<AccountEntity> = accountDao.getAll()

    override suspend fun getAccountById(userId: Long): AccountEntity? =
        accountDao.getById(userId)

    override suspend fun storeAccounts(
        accounts: List<AccountEntity>
    ) = accountDao.insertAll(accounts)
}
