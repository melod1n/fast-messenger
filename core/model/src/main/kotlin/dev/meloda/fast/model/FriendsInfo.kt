package dev.meloda.fast.model

import dev.meloda.fast.model.api.domain.VkUser

data class FriendsInfo(
    val friends: List<VkUser>,
    val onlineFriendsIds: List<Int>
)
