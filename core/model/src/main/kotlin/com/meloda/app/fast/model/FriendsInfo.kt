package com.meloda.app.fast.model

import com.meloda.app.fast.model.api.domain.VkUser

data class FriendsInfo(
    val friends: List<VkUser>,
    val onlineFriends: List<VkUser>
)
