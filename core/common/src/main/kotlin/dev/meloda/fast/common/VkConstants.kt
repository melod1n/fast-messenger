package dev.meloda.fast.common

object VkConstants {

    const val GROUP_FIELDS = "description,members_count,counters,status,verified"

    const val USER_FIELDS =
        "photo_50,photo_100,photo_200,photo_400_orig,status,screen_name,online_info,last_seen,verified,sex,bdate"

    const val ALL_FIELDS =
        "$USER_FIELDS,$GROUP_FIELDS"

    const val LP_VERSION = 19

    const val VK_APP_ID = "2274003"
    const val VK_SECRET = "hHbZxrka2uZ6jB1inYsH"

    const val FAST_GROUP_ID = -119516304
    const val FAST_APP_ID = "6964679"

    const val MESSENGER_APP_ID = 51453752
    const val MESSENGER_APP_SECRET = "4UyuCUsdK8pVCNoeQuGi"

    const val MESSENGER_APP_SCOPE = 1454174

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

//    val restrictedToEditAttachments = listOf<Class<out VkAttachment>>(
//        VkCallDomain::class.java,
//        VkCuratorDomain::class.java,
//        VkEventDomain::class.java,
//        VkGiftDomain::class.java,
//        VkGraffitiDomain::class.java,
//        VkGroupCallDomain::class.java,
//        VkStoryDomain::class.java,
//        VkAudioMessageDomain::class.java,
//        VkWidgetDomain::class.java
//    )
}
