package com.meloda.fast.api.network

object VKUrls {

    const val OAUTH = "https://oauth.vk.com"
    const val API = "https://api.vk.com/method"

    object Auth {
        const val directAuth = "$OAUTH/token"
        const val sendSms = "$API/auth.validatePhone"
    }

    object Conversations {
        const val get = "$API/messages.getConversations"
    }

    object Users {
        const val getById = "$API/users.get"
    }


}


