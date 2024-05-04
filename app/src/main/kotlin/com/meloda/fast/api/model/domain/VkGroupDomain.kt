package com.meloda.fast.api.model.domain

import android.os.Parcelable
import com.meloda.fast.database.model.VkGroupDB
import kotlinx.parcelize.Parcelize

@Parcelize
data class VkGroupDomain(
    val id: Int,
    val name: String,
    val screenName: String,
    val photo50: String?,
    val photo100: String?,
    val photo200: String?,
    val membersCount: Int?
) : Parcelable {

    override fun toString() = name.trim()

    fun mapToDB(): VkGroupDB = VkGroupDB(
        id = id,
        name = name,
        screenName = screenName,
        photo50 = photo50,
        photo100 = photo100,
        photo200 = photo200,
        membersCount = membersCount
    )
}
