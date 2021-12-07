package com.meloda.fast.api

import com.meloda.fast.api.model.attachments.*

object VKConstants {

    const val GROUP_FIELDS = "description,members_count,counters,status,verified"

    const val USER_FIELDS =
        "photo_50,photo_100,photo_200,status,screen_name,online,online_mobile,last_seen,verified,sex,online_info"

    const val ALL_FIELDS = "$USER_FIELDS,$GROUP_FIELDS"

    const val API_VERSION = "5.132"
    const val LP_VERSION = 10

    const val VK_APP_ID = "2274003"
    const val VK_SECRET = "hHbZxrka2uZ6jB1inYsH"

    const val FAST_GROUP_ID = -119516304
    const val FAST_APP_ID = "6964679"

    object Auth {
        const val SCOPE = "notify," +
                "friends," +
                "photos," +
                "audio," +
                "video," +
                "docs," +
                "status," +
                "notes," +
                "pages," +
                "wall," +
                "groups," +
                "messages," +
                "offline," +
                "notifications"

        object GrantType {
            const val PASSWORD = "password"
        }
    }

    val restrictedToEditAttachments = listOf(
        VkCall::class.java,
        VkCurator::class.java,
        VkEvent::class.java,
        VkGift::class.java,
        VkGraffiti::class.java,
        VkGroupCall::class.java,
        VkStory::class.java,
        VkVoiceMessage::class.java
    )
}