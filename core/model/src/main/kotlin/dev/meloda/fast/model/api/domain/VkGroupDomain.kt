package dev.meloda.fast.model.api.domain

import dev.meloda.fast.model.database.VkGroupEntity

data class VkGroupDomain(
    val id: Long,
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
