package com.meloda.fast.api.model.base

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.meloda.fast.api.model.VkGroup
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkGroup(
    val id: Int,
    val name: String,
    @SerializedName("screen_name")
    val screenName: String,
    @SerializedName("is_closed")
    val isClosed: Int,
    val type: String,
    @SerializedName("is_admin")
    val isAdmin: Int,
    @SerializedName("is_member")
    val isMember: Int,
    @SerializedName("is_advertiser")
    val isAdvertiser: Int,
    @SerializedName("photo_50")
    val photo50: String?,
    @SerializedName("photo_100")
    val photo100: String?,
    @SerializedName("photo_200")
    val photo200: String?,
    @SerializedName("members_count")
    val membersCount: Int?
) : Parcelable {

    fun asVkGroup() = VkGroup(
        id = -id,
        name = name,
        screenName = screenName,
        photo200 = photo200,
        membersCount = membersCount
    )

}
