package com.meloda.fast.api

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.WorkerThread
import com.meloda.fast.BuildConfig
import com.meloda.fast.activity.DropUserDataActivity
import com.meloda.fast.api.method.MessageMethodSetter
import com.meloda.fast.api.method.MethodSetter
import com.meloda.fast.api.method.UserMethodSetter
import com.meloda.fast.api.model.*
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.common.TaskManager
import com.meloda.fast.listener.OnResponseListener
import com.meloda.fast.net.HttpRequest
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

@Suppress("UNCHECKED_CAST")
object VKApi {

    private const val TAG = "VKM:VKApi"

    const val BASE_URL = "https://api.vk.com/method/"

    const val API_VERSION = "5.132"

    val language: String = AppGlobal.locale.language

    @WorkerThread
    @Suppress("UNCHECKED_CAST")
    fun <T> execute(url: String, cls: Class<T>?): ArrayList<T>? {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, "url: $url")
        }

        val buffer = HttpRequest[url].asString()
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "response: $buffer")
        }

        val json = JSONObject(buffer)

        try {
            checkError(json, url)
        } catch (ex: VKException) {
            if (ex.code == ErrorCodes.TOO_MANY_REQUESTS) {
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        execute(url, cls)
                    }
                }, 1000)
            } else throw ex
        }

        when (cls) {
            null -> return null

            VKLongPollServer::class.java -> {
                json.optJSONObject("response")?.let {
                    return arrayListOf(VKLongPollServer(it)) as ArrayList<T>?
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
            VKUser::class.java -> {
                json.optJSONObject("response")?.let { r ->
                    VKUser.friendsCount = r.optInt("count")
                }

                for (i in 0 until array.length()) {
                    models.add(VKUser(array.optJSONObject(i)) as T)
                }
            }

            VKMessage::class.java -> {
                response as JSONObject

                if (url.contains("messages.getHistory")) {
                    VKMessage.lastHistoryCount = response.optInt("count")

                    response.optJSONArray("profiles")?.let {
                        val profiles = arrayListOf<VKUser>()

                        for (j in 0 until it.length()) {
                            profiles.add(VKUser(it.optJSONObject(j)))
                        }

                        VKMessage.profiles = profiles
                    }

                    response.optJSONArray("groups")?.let {
                        val groups = arrayListOf<VKGroup>()

                        for (j in 0 until it.length()) {
                            groups.add(VKGroup(it.optJSONObject(j)))
                        }

                        VKMessage.groups = groups
                    }

                    response.optJSONArray("conversations")?.let {
                        val conversations = arrayListOf<VKConversation>()

                        for (j in 0 until it.length()) {
                            conversations.add(VKConversation(it.optJSONObject(j)))
                        }

                        VKMessage.conversations = conversations
                    }
                }

                for (i in 0 until array.length()) {
                    var source = array.optJSONObject(i)
                    if (source.has("message")) {
                        source = source.optJSONObject("message")
                    }

                    val message = VKMessage(source)
                    models.add(message as T)
                }

                val profiles = ArrayList<VKUser>()
                response.optJSONArray("profiles")?.let {
                    profiles.addAll(VKUser.parse(it))
                }

                val groups = ArrayList<VKGroup>()
                response.optJSONArray("groups")?.let {
                    groups.addAll(VKGroup.parse(it))
                }

                AppGlobal.database.let {
                    it.users.insert(profiles)
                    it.groups.insert(groups)
                }
            }

            VKGroup::class.java -> {
                for (i in 0 until array.length()) {
                    models.add(VKGroup(array.optJSONObject(i)) as T)
                }
            }

            VKModel::class.java -> {
                if (url.contains("messages.getHistoryAttachments")) {
                    return VKAttachments.parse(array) as ArrayList<T>
                }
            }

            VKConversation::class.java -> {
                if (url.contains("getConversationsById")) {
                    for (i in 0 until array.length()) {
                        val source = array.optJSONObject(i)
                        models.add(VKConversation(source) as T)
                    }

                    return models
                }

                json.optJSONObject("response")?.let { r ->
                    VKConversation.conversationsCount = r.optInt("count")
                }

                for (i in 0 until array.length()) {
                    response as JSONObject

                    val source = array.optJSONObject(i)
                    val oConversation = source.optJSONObject("conversation") ?: return null
                    val oLastMessage = source.optJSONObject("last_message") ?: return null

                    val conversation = VKConversation(oConversation).also {
                        it.lastMessage = VKMessage(oLastMessage)
                    }

                    response.optJSONArray("profiles")?.let {
                        val profiles = arrayListOf<VKUser>()

                        for (j in 0 until it.length()) {
                            profiles.add(VKUser(it.optJSONObject(j)))
                        }

                        VKConversation.profiles = profiles
                    }

                    response.optJSONArray("groups")?.let {
                        val groups = arrayListOf<VKGroup>()

                        for (j in 0 until it.length()) {
                            groups.add(VKGroup(it.optJSONObject(j)))
                        }

                        VKConversation.groups = groups
                    }

                    models.add(conversation as T)
                }
            }
        }

        return models
    }

    fun <E> execute(url: String, cls: Class<E>, listener: OnResponseListener<E>?) {
        TaskManager.execute {
            try {
                val models = execute(url, cls)

                listener?.let {
                    AppGlobal.handler.post(SuccessCallback(listener, models as E))
                }
            } catch (e: Exception) {
                e.printStackTrace()

                listener?.let {
                    AppGlobal.handler.post(ErrorCallback(listener, e))
                }
            }
        }
    }

    fun <E> executeArray(url: String, cls: Class<E>, listener: OnResponseListener<ArrayList<E>>?) {
        TaskManager.execute {
            try {
                val models = execute(url, cls)

                listener?.let {
                    AppGlobal.handler.post(SuccessArrayCallback(listener, models as ArrayList<E>))
                }
            } catch (e: Exception) {
                e.printStackTrace()

                listener?.let {
                    AppGlobal.handler.post(ErrorCallback(listener, e))
                }
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
            val e = VKException(url, message, code)

            if (code == 5 && message.contains("invalid session")) {
//                context?.startActivity(Intent(context, DropUserDataActivity::class.java).apply {
//                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                })
            }

            if (code == ErrorCodes.CAPTCHA_NEEDED) {
                e.captchaImg = error.optString("captcha_img")
                e.captchaSid = error.optString("captcha_sid")
            }

            if (code == ErrorCodes.VALIDATION_REQUIRED) {
                e.redirectUri = error.optString("redirect_uri")
            }

            throw e
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