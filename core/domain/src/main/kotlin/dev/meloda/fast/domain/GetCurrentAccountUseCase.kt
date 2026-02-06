package dev.meloda.fast.domain

import dev.meloda.fast.data.UserConfig
import dev.meloda.fast.data.db.AccountsRepository
import dev.meloda.fast.model.database.AccountEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetCurrentAccountUseCase(private val accountsRepository: AccountsRepository) {
    suspend operator fun invoke(): AccountEntity? = withContext(Dispatchers.IO) {
        accountsRepository.getAccountById(UserConfig.currentUserId)
    }
}
