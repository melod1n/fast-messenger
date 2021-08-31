package com.meloda.fast.api

import android.os.Handler
import android.util.Log
import androidx.annotation.WorkerThread
import com.meloda.fast.BuildConfig
import com.meloda.fast.api.method.MessageMethodSetter
import com.meloda.fast.api.method.MethodSetter
import com.meloda.fast.api.method.UserMethodSetter
import com.meloda.fast.api.network.ErrorCodes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Suppress("UNCHECKED_CAST")
object VKApi {

    private const val TAG = "VKM:VKApi"

    const val BASE_URL = "https://api.vk.com/method/"

    const val API_VERSION = "5.132"

    var language: String = ""
    var token: String = ""

    private lateinit var handler: Handler

    fun init(language: String, token: String, handler: Handler) {
        VKApi.language = language
        VKApi.token = token
        VKApi.handler = handler
    }

    @WorkerThread
    @Suppress("UNCHECKED_CAST")
    fun <T> execute(url: String, cls: Class<T>?): ArrayList<T>? {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, "url: $url")
        }

        val buffer = com.meloda.fast.net.HttpRequest[url].asString()

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "response: $buffer")
        }

        val json = JSONObject(buffer)

        try {
            checkError(json, url)
        } catch (ex: VKException) {
            throw ex
//            if (ex.code == ErrorCodes.TOO_MANY_REQUESTS) {
//                Timer().schedule(object : TimerTask() {
//                    override fun run() {
//                        execute(url, cls)
//                    }
//                }, 1000)
//            } else throw ex
        }

        when (cls) {
            null -> return null

            com.meloda.fast.api.model.VKLongPollServer::class.java -> {
                json.optJSONObject("response")?.let {
                    return arrayListOf(com.meloda.fast.api.model.VKLongPollServer(it)) as ArrayList<T>?
                }
            }

            Boolean::class.java -> {
                val value = json.optInt("response") == 1
                return arrayListOf(value) as ArrayList<T>?
            }

            Long::class.java -> {
                val value = json.optLong("response")
                return arrayListOf(value) as ArrayList<T>?
            }

            Int::class.java -> {
                val value = json.optInt("response")
                return arrayListOf(value) as ArrayList<T>?
            }
        }

        val response = json.opt("response") ?: return null

        val array = optItems(json) ?: return null
        val models = ArrayList<T>(array.length())

        when (cls) {
            com.meloda.fast.api.model.VKUser::class.java -> {
                json.optJSONObject("response")?.let { r ->
                    com.meloda.fast.api.model.VKUser.friendsCount = r.optInt("count")
                }

                for (i in 0 until array.length()) {
                    models.add(com.meloda.fast.api.model.VKUser(array.optJSONObject(i)) as T)
                }
            }

            com.meloda.fast.api.model.VKMessage::class.java -> {
                response as JSONObject

                if (url.contains("messages.getHistory")) {
                    com.meloda.fast.api.model.VKMessage.lastHistoryCount = response.optInt("count")

                    response.optJSONArray("profiles")?.let {
                        val profiles = arrayListOf<com.meloda.fast.api.model.VKUser>()

                        for (j in 0 until it.length()) {
                            profiles.add(com.meloda.fast.api.model.VKUser(it.optJSONObject(j)))
                        }

                        com.meloda.fast.api.model.VKMessage.profiles = profiles
                    }

                    response.optJSONArray("groups")?.let {
                        val groups = arrayListOf<com.meloda.fast.api.model.VKGroup>()

                        for (j in 0 until it.length()) {
                            groups.add(com.meloda.fast.api.model.VKGroup(it.optJSONObject(j)))
                        }

                        com.meloda.fast.api.model.VKMessage.groups = groups
                    }

                    response.optJSONArray("conversations")?.let {
                        val conversations = arrayListOf<com.meloda.fast.api.model.VKConversation>()

                        for (j in 0 until it.length()) {
                            conversations.add(
                                com.meloda.fast.api.model.VKConversation(
                                    it.optJSONObject(
                                        j
                                    )
                                )
                            )
                        }

                        com.meloda.fast.api.model.VKMessage.conversations = conversations
                    }
                }

                for (i in 0 until array.length()) {
                    var source = array.optJSONObject(i)
                    if (source.has("message")) {
                        source = source.optJSONObject("message")
                    }

                    val message = com.meloda.fast.api.model.VKMessage(source)
                    models.add(message as T)
                }
            }

            com.meloda.fast.api.model.VKGroup::class.java -> {
                for (i in 0 until array.length()) {
                    models.add(com.meloda.fast.api.model.VKGroup(array.optJSONObject(i)) as T)
                }
            }

            com.meloda.fast.api.model.VKModel::class.java -> {
                if (url.contains("messages.getHistoryAttachments")) {
                    return com.meloda.fast.api.model.VKAttachments.parse(array) as ArrayList<T>
                }
            }

            com.meloda.fast.api.model.VKConversation::class.java -> {
                if (url.contains("getConversationsById")) {
                    for (i in 0 until array.length()) {
                        val source = array.optJSONObject(i)
                        models.add(com.meloda.fast.api.model.VKConversation(source) as T)
                    }

                    return models
                }

                json.optJSONObject("response")?.let { r ->
                    com.meloda.fast.api.model.VKConversation.conversationsCount = r.optInt("count")
                }

                for (i in 0 until array.length()) {
                    response as JSONObject

                    val source = array.optJSONObject(i)
                    val oConversation = source.optJSONObject("conversation") ?: return null
                    val oLastMessage = source.optJSONObject("last_message") ?: return null

                    val conversation = com.meloda.fast.api.model.VKConversation(oConversation).also {
                        it.lastMessage = com.meloda.fast.api.model.VKMessage(oLastMessage)
                    }

                    response.optJSONArray("profiles")?.let {
                        val profiles = arrayListOf<com.meloda.fast.api.model.VKUser>()

                        for (j in 0 until it.length()) {
                            profiles.add(com.meloda.fast.api.model.VKUser(it.optJSONObject(j)))
                        }

                        com.meloda.fast.api.model.VKConversation.profiles = profiles
                    }

                    response.optJSONArray("groups")?.let {
                        val groups = arrayListOf<com.meloda.fast.api.model.VKGroup>()

                        for (j in 0 until it.length()) {
                            groups.add(com.meloda.fast.api.model.VKGroup(it.optJSONObject(j)))
                        }

                        com.meloda.fast.api.model.VKConversation.groups = groups
                    }

                    models.add(conversation as T)
                }
            }
        }

        return models
    }

    fun <E> execute(url: String, cls: Class<E>, listener: OnResponseListener<E>?) {
        com.meloda.fast.concurrent.TaskManager.execute {
            try {
                val models = execute(url, cls) ?: return@execute

//                listener?.onResponse(models)
            } catch (e: Exception) {
                e.printStackTrace()
                listener?.onError(e)
//                it.resumeWithException(e)
            }
        }

    }

    suspend fun <E> suspendExecute(url: String, cls: Class<E>): Flow<E> {
        return suspendCoroutine {
            try {
                val models = execute(url, cls)?.asFlow() ?: return@suspendCoroutine

                it.resume(models)
            } catch (e: Exception) {
                e.printStackTrace()
                it.resumeWithException(e)
            }
        }
    }

    fun <E> executeArray(url: String, cls: Class<E>, listener: OnResponseListener<ArrayList<E>>) {
        com.meloda.fast.concurrent.TaskManager.execute {
            try {
                val models = execute(url, cls)

                handler.post { listener.onResponse(models as ArrayList<E>) }
            } catch (e: Exception) {
                e.printStackTrace()

                listener.onError(e)
            }
        }
    }

    private fun optItems(source: JSONObject): JSONArray? {
        val response = source.opt("response")

        return when (response) {
            is JSONArray -> response
            is JSONObject -> response.optJSONArray("items")
            else -> null
        }
    }

    private fun checkError(json: JSONObject, url: String) {
        if (json.has("error")) {
            val error = json.optJSONObject("error") ?: return

            val code = error.optInt("error_code", -1)
            val message = error.optString("error_msg", "")
//            val e = VKException(url, message, code)

            //TODO: add checking invalid session
            if (code == 5 && message.contains("invalid session")) {
//                context?.startActivity(Intent(context, DropUserDataActivity::class.java).apply {
//                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                })
            }

//            if (code == ErrorCodes.CAPTCHA_NEEDED) {
//                e.captchaImg = error.optString("captcha_img")
//                e.captchaSid = error.optString("captcha_sid")
//            }
//
//            if (code == ErrorCodes.VALIDATION_REQUIRED) {
//                e.redirectUri = error.optString("redirect_uri")
//            }

//            throw e
        }
    }

    fun users(): VKUsers {
        return VKUsers()
    }

    fun friends(): VKFriends {
        return VKFriends()
    }

    fun messages(): VKMessages {
        return VKMessages()
    }

    fun groups(): VKGroups {
        return VKGroups()
    }

    fun account(): VKAccounts {
        return VKAccounts()
    }

    class VKFriends {
        fun get(): MethodSetter {
            return MethodSetter("friends.get")
        }
    }

    class VKUsers {
        fun get(): UserMethodSetter {
            return UserMethodSetter("users.get")
        }
    }

    class VKMessages {
        fun get(): MessageMethodSetter {
            return MessageMethodSetter("messages.get")
        }

        fun getConversations(): MessageMethodSetter {
            return MessageMethodSetter("messages.getConversations")
        }

        fun getConversationsById(): MessageMethodSetter {
            return MessageMethodSetter("messages.getConversationsById")
        }

        fun getById(): MessageMethodSetter {
            return MessageMethodSetter("messages.getById")
        }

        fun search(): MessageMethodSetter {
            return MessageMethodSetter("messages.search")
        }

        fun getHistory(): MessageMethodSetter {
            return MessageMethodSetter("messages.getHistory")
        }

        fun getHistoryAttachments(): MessageMethodSetter {
            return MessageMethodSetter("messages.getHistoryAttachments")
        }

        fun send(): MessageMethodSetter {
            return MessageMethodSetter("messages.send")
        }

        fun sendSticker(): MessageMethodSetter {
            return MessageMethodSetter("messages.sendSticker")
        }

        fun delete(): MessageMethodSetter {
            return MessageMethodSetter("messages.delete")
        }

        fun deleteDialog(): MessageMethodSetter {
            return MessageMethodSetter("messages.deleteDialog")
        }

        fun restore(): MessageMethodSetter {
            return MessageMethodSetter("messages.restore")
        }

        fun markAsRead(): MessageMethodSetter {
            return MessageMethodSetter("messages.markAsRead")
        }

        fun markAsImportant(): MessageMethodSetter {
            return MessageMethodSetter("messages.markAsImportant")
        }

        fun getLongPollServer(): MessageMethodSetter {
            return MessageMethodSetter("messages.getLongPollServer")
        }

        /**
         * Returns updates in user's private messages.
         * To speed up handling of private messages,
         * it can be useful to cache previously loaded messages on
         * a user's mobile device/desktop, to prevent re-receipt at each call.
         * With this method, you can synchronize a local copy of
         * the message list with the actual version.
         *
         *
         * Result:
         * Returns an object that contains the following fields:
         * 1 — history:     An array similar to updates field returned
         * from the Long Poll server,
         * with these exceptions:
         * - For events with code 4 (addition of a new message),
         * there are no fields except the first three.
         * - There are no events with codes 8, 9 (friend goes online/offline)
         * or with codes 61, 62 (typing during conversation/chat).
         *
         *
         * 2 — messages:    An array of private message objects that were found
         * among events with code 4 (addition of a new message)
         * from the history field.
         * Each object of message contains a set of fields described here.
         * The first array element is the total number of messages
         */
        fun getLongPollHistory(): MessageMethodSetter {
            return MessageMethodSetter("messages.getLongPollHistory")
        }

        fun getChat(): MessageMethodSetter {
            return MessageMethodSetter("messages.getChat")
        }

        fun createChat(): MessageMethodSetter {
            return MessageMethodSetter("messages.createChat")
        }

        fun editChat(): MessageMethodSetter {
            return MessageMethodSetter("messages.editChat")
        }

        val chatUsers: MessageMethodSetter
            get() = MessageMethodSetter("messages.getChatUsers")

        fun setActivity(): MessageMethodSetter {
            return MessageMethodSetter("messages.setActivity").type(true)
        }

        fun addChatUser(): MessageMethodSetter {
            return MessageMethodSetter("messages.addChatUser")
        }

        fun removeChatUser(): MessageMethodSetter {
            return MessageMethodSetter("messages.removeChatUser")
        }
    }

    class VKGroups {
        fun getById(): MethodSetter {
            return MethodSetter("groups.getById")
        }

        fun join(): MethodSetter {
            return MethodSetter("groups.join")
        }
    }

    class VKAccounts {
        fun setOffline(): MethodSetter {
            return MethodSetter("account.setOffline")
        }

        fun setOnline(): MethodSetter {
            return MethodSetter("account.setOnline")
        }
    }

    class SuccessCallback<E>(
        private val listener: OnResponseListener<E>?,
        private val response: E
    ) : Runnable {
        override fun run() {
            listener?.onResponse(response)
        }
    }

    class SuccessArrayCallback<E>(
        private val listener: OnResponseListener<ArrayList<E>>?,
        private val response: ArrayList<E>
    ) : Runnable {
        override fun run() {
            listener?.onResponse(response)
        }
    }

    class ErrorCallback<E>(
        private val listener: OnResponseListener<E>?,
        private val exception: Exception
    ) : Runnable {
        override fun run() {
            listener?.onError(exception)
        }
    }
}