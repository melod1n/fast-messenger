package dev.meloda.fast.model.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.meloda.fast.model.api.domain.VkGroupDomain

@Entity(tableName = "groups")
data class VkGroupEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val screenName: String,
    val photo50: String?,
    val photo100: String?,
    val photo200: String?,
    val membersCount: Int?
)

fun VkGroupEntity.asDomain(): VkGroupDomain = VkGroupDomain(
    id = id,
    name = name,
    screenName = screenName,
    photo50 = photo50,
    photo100 = photo100,
    photo200 = photo200,
    membersCount = membersCount
)
