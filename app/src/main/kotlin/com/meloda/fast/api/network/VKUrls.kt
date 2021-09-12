package com.meloda.fast.api.network

object VKUrls {

    const val OAUTH = "https://oauth.vk.com"
    const val API = "https://api.vk.com/method"

    object Auth {
        const val DirectAuth = "$OAUTH/token"
        const val SendSms = "$API/auth.validatePhone"
    }

    object Conversations {
        const val Get = "$API/messages.getConversations"
    }

    object Users {
        const val GetById = "$API/users.get"
    }

    object Messages {
        const val GetHistory = "$API/messages.getHistory"
        const val Send = "$API/messages.send"
    }


}


