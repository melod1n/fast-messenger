package dev.meloda.fast.model

import dev.meloda.fast.model.database.AccountEntity

data class AccountDto(
    val userId: Long,
    val accessToken: String,
    val fastToken: String?,
    val trustedHash: String?,
    val exchangeToken: String?
) {

    fun mapToEntity(): AccountEntity = AccountEntity(
        userId = userId,
        accessToken = accessToken,
        fastToken = fastToken,
        trustedHash = trustedHash,
        exchangeToken = exchangeToken
    )

    override fun toString(): String {
        return super.toString()
    }
}
