package com.meloda.app.fast.model.api.domain

import com.meloda.app.fast.model.database.VkGroupEntity

data class VkGroupDomain(
    val id: Int,
    val name: String,
    val screenName: String,
    val photo50: String?,
    val photo100: String?,
    val photo200: String?,
    val membersCount: Int?
) {

    override fun toString() = name.trim()

    fun mapToDB(): VkGroupEntity = VkGroupEntity(
        id = id,
        name = name,
        screenName = screenName,
        photo50 = photo50,
        photo100 = photo100,
        photo200 = photo200,
        membersCount = membersCount
    )
}
