package ru.melod1n.project.vkm.common

import android.util.Log
import ru.melod1n.project.vkm.BuildConfig
import ru.melod1n.project.vkm.api.VKApi
import ru.melod1n.project.vkm.api.VKApiKeys
import ru.melod1n.project.vkm.api.method.MethodSetter
import ru.melod1n.project.vkm.api.model.VKConversation
import ru.melod1n.project.vkm.api.model.VKGroup
import ru.melod1n.project.vkm.api.model.VKMessage
import ru.melod1n.project.vkm.api.model.VKUser
import ru.melod1n.project.vkm.concurrent.LowThread
import ru.melod1n.project.vkm.database.MemoryCache
import ru.melod1n.project.vkm.event.EventInfo
import ru.melod1n.project.vkm.listener.OnResponseListener
import java.util.*
import java.util.stream.Collectors

object TaskManager {

    private const val TAG = "TaskManager"

    private val groupsTasksIds = arrayListOf<Int>()

    private var groupsTimer: Timer? = null

    private val usersTasksIds = arrayListOf<Int>()

    private var usersTimer: Timer? = null

    private val messagesTasksIds = arrayListOf<Int>()

    private val messagesReadIds = arrayListOf<Int>()

    private var messagesReadTimer: Timer? = null

    private val conversationsTasksIds = arrayListOf<Int>()

    private val listeners = arrayListOf<OnEventListener?>()

    fun addOnEventListener(listener: OnEventListener?) {
        listeners.add(listener)
    }

    fun removeOnEventListener(listener: OnEventListener?) {
        listeners.remove(listener)
    }

    fun execute(runnable: Runnable?) {
        LowThread(runnable).start()
    }

    private fun <T> addProcedure(
        methodSetter: MethodSetter,
        className: Class<T>,
        pushInfo: EventInfo<*>?,
        responseListener: OnResponseListener<T>?
    ) {
        execute {
            methodSetter.executeArray(className, object : OnResponseListener<ArrayList<T>> {
                override fun onResponse(response: ArrayList<T>) {
                    if (response.isEmpty()) return

                    responseListener?.onResponse(response[0])

                    pushInfo?.let { sendEvent(it) }
                }

                override fun onError(t: Throwable) {
                    responseListener?.onError(t)
                }

            })
        }
    }

    fun sendEvent(eventInfo: EventInfo<*>) {
        AppGlobal.handler.post {
            for (listener in listeners) {
                listener?.onNewEvent(eventInfo)
            }
        }
    }

    fun loadUser(
        eventKey: VKApiKeys,
        userId: Int,
        responseListener: OnResponseListener<VKUser>? = null
    ) {
        if (usersTasksIds.contains(userId)) return
        usersTasksIds.add(userId)

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Load user: $userId")
        }

        if (usersTimer != null) {
            usersTimer?.cancel()
        }

        usersTimer = Timer()
        usersTimer?.schedule(object : TimerTask() {
            override fun run() {
                val setter = VKApi.users()
                    .get()
                    .userIds(usersTasksIds)
                    .fields(VKUser.DEFAULT_FIELDS)

                val usersIds = arrayListOf<String>()
                usersTasksIds.forEach { usersIds.add(it.toString()) }

                addProcedure(
                    setter,
                    VKUser::class.java,
                    EventInfo(eventKey, usersTasksIds),
                    object : OnResponseListener<VKUser> {
                        override fun onResponse(response: VKUser) {
                            Log.d(
                                TAG,
                                "Loaded users: ${
                                    usersIds.stream().collect(Collectors.joining(", "))
                                }"
                            )

                            usersTasksIds.remove(userId)
                            responseListener?.onResponse(response)

                            execute { MemoryCache.put(response) }
                        }

                        override fun onError(t: Throwable) {
                            Log.w(
                                TAG,
                                "Loaded users: ${
                                    usersIds.stream().collect(Collectors.joining(", "))
                                }\nStack:${Log.getStackTraceString(t)}"
                            )
                            responseListener?.onError(t)
                        }

                    })

                usersTimer = null
            }
        }, 500)
    }

    fun loadGroup(
        eventKey: VKApiKeys,
        groupId: Int,
        responseListener: OnResponseListener<VKGroup>? = null
    ) {
        if (groupsTasksIds.contains(groupId)) return
        groupsTasksIds.add(groupId)

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Load group: $groupId")
        }

        val setter = VKApi.groups().getById()
            .groupIds(groupsTasksIds)
            .fields(VKGroup.DEFAULT_FIELDS)

        if (groupsTimer != null) {
            groupsTimer?.cancel()
        }

        groupsTimer = Timer()
        groupsTimer?.schedule(object : TimerTask() {
            override fun run() {
                val groupsIds = arrayListOf<String>()
                groupsTasksIds.forEach { groupsIds.add(it.toString()) }

                addProcedure(
                    setter,
                    VKGroup::class.java,
                    EventInfo(eventKey, groupsTasksIds),
                    object : OnResponseListener<VKGroup> {
                        override fun onResponse(response: VKGroup) {
                            Log.d(
                                TAG,
                                "Loaded groups: ${
                                    groupsIds.stream().collect(Collectors.joining(", "))
                                }"
                            )

                            groupsTasksIds.remove(groupId)
                            responseListener?.onResponse(response)

                            execute { MemoryCache.put(response) }
                        }

                        override fun onError(t: Throwable) {
                            Log.w(
                                TAG,
                                "Not loaded Group: ${
                                    groupsIds.stream().collect(Collectors.joining(", "))
                                }\nStack: " + Log.getStackTraceString(
                                    t
                                )
                            )
                            responseListener?.onError(t)
                        }
                    })
            }
        }, 500)

    }

    fun loadMessage(
        eventKey: VKApiKeys,
        messageId: Int,
        responseListener: OnResponseListener<VKMessage>? = null
    ) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Load message: $messageId")
        }
        if (messagesTasksIds.contains(messageId)) return
        messagesTasksIds.add(messageId)

        val setter = VKApi.messages().getById()
            .messageIds(messageId)
            .extended(true)
            .filter(VKUser.DEFAULT_FIELDS + "," + VKGroup.DEFAULT_FIELDS)

        addProcedure(
            setter,
            VKMessage::class.java,
            EventInfo(eventKey, messageId),
            object : OnResponseListener<VKMessage> {
                override fun onResponse(response: VKMessage) {
                    Log.d(TAG, "Loaded message: $messageId")

                    messagesTasksIds.remove(messageId)
                    responseListener?.onResponse(response)

                    execute { MemoryCache.put(response) }
                }

                override fun onError(t: Throwable) {
                    Log.w(
                        TAG,
                        "Not loaded message: $messageId. Stack: " + Log.getStackTraceString(t)
                    )
                    responseListener?.onError(t)
                }
            })
    }

    fun readMessage(
        eventKey: VKApiKeys,
        peerId: Int,
        messageId: Int,
        responseListener: OnResponseListener<Any?>? = null
    ) {
        if (messagesReadIds.contains(messageId)) return
        messagesReadIds.add(messageId)

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Read message: $messageId")
        }

        if (messagesReadTimer != null) {
            messagesReadTimer?.cancel()
        }

        messagesReadTimer = Timer()
        messagesReadTimer?.schedule(object : TimerTask() {
            override fun run() {
                val messagesIds = arrayListOf<String>()
                messagesReadIds.forEach { messagesIds.add(it.toString()) }

                val setter = VKApi.messages().markAsRead()
//                    .startMessageId(messageId)
                    .markConversationAsRead(true)
                    .peerId(peerId)

                addProcedure(
                    setter,
                    Int::class.java,
                    EventInfo(eventKey, arrayOf(peerId, messageId)),
                    object : OnResponseListener<Int> {
                        override fun onResponse(response: Int) {
                            Log.d(
                                TAG,
                                "Readed messages: ${
                                    messagesIds.stream().collect(Collectors.joining(", "))
                                }"
                            )

                            messagesReadIds.remove(messageId)
                            responseListener?.onResponse(response)

                            //TODO: update readed messages in cache
//                            execute { MemoryCache.put(response) }
                        }

                        override fun onError(t: Throwable) {
                            Log.w(
                                TAG,
                                "Not readed messages: ${
                                    messagesIds.stream().collect(Collectors.joining(", "))
                                }\nStack: " + Log.getStackTraceString(
                                    t
                                )
                            )
                            responseListener?.onError(t)
                        }
                    }
                )
            }

        }, 500)
    }

    fun loadConversation(
        eventKey: VKApiKeys,
        conversationId: Int,
        responseListener: OnResponseListener<VKConversation>? = null
    ) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Load conversation: $conversationId")
        }
        if (conversationsTasksIds.contains(conversationId)) return
        conversationsTasksIds.add(conversationId)

        val setter = VKApi.messages()
            .getConversationsById()
            .peerIds(conversationId)
            .extended(true)
            .fields(VKUser.DEFAULT_FIELDS + "," + VKGroup.DEFAULT_FIELDS)

        addProcedure(
            setter,
            VKConversation::class.java,
            EventInfo(eventKey, conversationId),
            object : OnResponseListener<VKConversation> {
                override fun onResponse(response: VKConversation) {
                    Log.d(TAG, "Loaded conversation: $conversationId")

                    conversationsTasksIds.remove(conversationId)
                    responseListener?.onResponse(response)

                    execute { MemoryCache.put(response) }
                }

                override fun onError(t: Throwable) {
                    Log.w(
                        TAG,
                        "Not loaded conversation: $conversationId. Stack: " + Log.getStackTraceString(
                            t
                        )
                    )
                    responseListener?.onError(t)
                }

            })
    }

    interface OnEventListener {
        fun onNewEvent(info: EventInfo<*>)
    }
}