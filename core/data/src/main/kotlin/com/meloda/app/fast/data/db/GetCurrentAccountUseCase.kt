package com.meloda.app.fast.data.db

import com.meloda.app.fast.common.UserConfig
import com.meloda.app.fast.model.database.AccountEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetCurrentAccountUseCase(private val accountsRepository: AccountsRepository) {

    suspend operator fun invoke(): AccountEntity? = withContext(Dispatchers.IO) {
        accountsRepository.getAccountById(UserConfig.currentUserId)
    }
}
