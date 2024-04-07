package com.meloda.fast.api

import com.meloda.fast.api.model.domain.VkAttachment
import com.meloda.fast.api.model.domain.VkAudioMessageDomain
import com.meloda.fast.api.model.domain.VkCallDomain
import com.meloda.fast.api.model.domain.VkCuratorDomain
import com.meloda.fast.api.model.domain.VkEventDomain
import com.meloda.fast.api.model.domain.VkGiftDomain
import com.meloda.fast.api.model.domain.VkGraffitiDomain
import com.meloda.fast.api.model.domain.VkGroupCallDomain
import com.meloda.fast.api.model.domain.VkStoryDomain
import com.meloda.fast.api.model.domain.VkWidgetDomain

@Suppress("RemoveExplicitTypeArguments")
object VKConstants {

    const val GROUP_FIELDS = "description,members_count,counters,status,verified"

    const val USER_FIELDS =
        "photo_50,photo_100,photo_200,status,screen_name,online,online_mobile,last_seen,verified,sex,online_info,bdate"

    const val ALL_FIELDS = "$USER_FIELDS,$GROUP_FIELDS"

    // TODO: 17/12/2023, Danil Nikolaev: up api version and handle changes
    const val API_VERSION = "5.173"
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

    val restrictedToEditAttachments = listOf<Class<out VkAttachment>>(
        VkCallDomain::class.java,
        VkCuratorDomain::class.java,
        VkEventDomain::class.java,
        VkGiftDomain::class.java,
        VkGraffitiDomain::class.java,
        VkGroupCallDomain::class.java,
        VkStoryDomain::class.java,
        VkAudioMessageDomain::class.java,
        VkWidgetDomain::class.java
    )
}
