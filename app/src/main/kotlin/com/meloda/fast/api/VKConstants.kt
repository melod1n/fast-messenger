package com.meloda.fast.api

object VKConstants {

    const val GROUP_FIELDS = "description,members_count,counters,status,verified"

    const val USER_FIELDS =
        "photo_50,photo_100,photo_200,status,screen_name,online,online_mobile,last_seen,verified,sex"


    const val API_VERSION = "5.132"
    const val VK_APP_ID = "2274003"
    const val VK_SECRET = "hHbZxrka2uZ6jB1inYsH"


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
}