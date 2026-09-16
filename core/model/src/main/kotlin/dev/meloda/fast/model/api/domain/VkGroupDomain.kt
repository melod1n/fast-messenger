package dev.meloda.fast.model.api.domain

import dev.meloda.fast.model.database.VkGroupEntity

data class VkGroupDomain(
    val id: Long,
    val name: String,
    val screenName: String,
    val photo50: String?,
    val photo100: String?,
    val photo200: String?,
    val photoBase: String? = null,
    val membersCount: Int?
) {

    override fun toString() = name.trim()
}

fun VkGroupDomain.asEntity(): VkGroupEntity = VkGroupEntity(
    id = id,
    name = name,
    screenName = screenName,
    photo50 = photo50,
    photo100 = photo100,
    photo200 = photo200,
    photoBase = photoBase,
    membersCount = membersCount
)

fun VkGroupDomain.photoUrl(size: Int = 200): String? = resolvePhotoUrl(
    photoBase = photoBase,
    size = size,
    preferred = when {
        size <= 50 -> photo50
        size <= 100 -> photo100
        else -> photo200
    },
    photo200, photo100, photo50
)

fun VkGroupDomain.photo(size: Int = 200): String? = photoUrl(size)
