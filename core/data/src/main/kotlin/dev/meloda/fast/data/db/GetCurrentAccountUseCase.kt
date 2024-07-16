package dev.meloda.fast.data.db

import dev.meloda.fast.common.UserConfig
import dev.meloda.fast.model.database.AccountEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetCurrentAccountUseCase(private val accountsRepository: AccountsRepository) {

    suspend operator fun invoke(): AccountEntity? = withContext(Dispatchers.IO) {
        accountsRepository.getAccountById(UserConfig.currentUserId)
    }
}
