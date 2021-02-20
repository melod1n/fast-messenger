package com.meloda.fast.database

import android.util.SparseArray
import androidx.annotation.WorkerThread
import com.meloda.fast.api.model.*
import com.meloda.fast.common.AppGlobal

object MemoryCache {

    private val users = SparseArray<VKUser>()
    private val groups = SparseArray<VKGroup>()

    @WorkerThread
    fun getUserById(id: Int): VKUser? {
        var user = users[id]
        if (user == null) {
            user = AppGlobal.database.users.getById(id)

            user?.let { append(it) }
        }
        return user
    }

    @WorkerThread
    fun getGroupById(positiveId: Int): VKGroup? {
        var group = groups[positiveId]
        if (group == null) {
            group = AppGlobal.database.groups.getById(positiveId)

            group?.let { append(it) }
        }
        return group
    }

    @WorkerThread
    fun getMessageById(id: Int): VKMessage? {
        return AppGlobal.database.messages.getById(id)
    }

    @WorkerThread
    fun getMessagesByPeerId(peerId: Int): List<VKMessage> {
        return AppGlobal.database.messages.getByPeerId(peerId)
    }

    @WorkerThread
    fun getMessages(): List<VKMessage> {
        return AppGlobal.database.messages.getAll()
    }

    @WorkerThread
    fun getConversationById(id: Int): VKConversation? {
        return AppGlobal.database.conversations.getById(id)
    }

    @WorkerThread
    fun getConversations(): List<VKConversation> {
        return AppGlobal.database.conversations.getAll()
    }

    @WorkerThread
    fun getFriends(userId: Int): List<VKFriend> {
        return AppGlobal.database.friends.getByUserId(userId)
    }

    fun appendUsers(users: Collection<VKUser>) {
        for (user in users) {
            append(user)
        }
    }

    fun appendGroups(groups: Collection<VKGroup>) {
        for (group in groups) {
            append(group)
        }
    }

    fun append(value: VKGroup) {
        groups.append(value.groupId, value)
    }

    fun append(value: VKUser) {
        users.append(value.userId, value)
    }

    @WorkerThread
    fun put(value: VKUser) {
        append(value)

        AppGlobal.database.users.insert(value)
    }

    @WorkerThread
    fun putUsers(users: List<VKUser>) {
        appendUsers(users)

        AppGlobal.database.users.insert(users)
    }

    @WorkerThread
    fun put(value: VKFriend) {
        AppGlobal.database.friends.insert(value)
    }

    @WorkerThread
    fun putFriends(friends: List<VKFriend>) {
        AppGlobal.database.friends.insert(friends)
    }

    @WorkerThread
    fun put(value: VKMessage) {
        AppGlobal.database.messages.insert(value)
    }

    @WorkerThread
    fun putMessages(messages: List<VKMessage>) {
        AppGlobal.database.messages.insert(messages)
    }

    @WorkerThread
    fun put(value: VKGroup) {
        append(value)

        AppGlobal.database.groups.insert(value)
    }

    @WorkerThread
    fun putGroups(groups: List<VKGroup>) {
        appendGroups(groups)

        AppGlobal.database.groups.insert(groups)
    }

    @WorkerThread
    fun put(value: VKConversation) {
        AppGlobal.database.conversations.insert(value)
    }

    @WorkerThread
    fun putConversations(conversations: List<VKConversation>) {
        AppGlobal.database.conversations.insert(conversations)
    }

    @WorkerThread
    fun deleteMessage(messageId: Int, safe: Boolean = true) {
        if (safe) {
            AppGlobal.database.messages.getById(messageId) ?: return
        }

        AppGlobal.database.messages.deleteById(messageId)
    }

    @WorkerThread
    fun deleteConversation(conversationId: Int, safe: Boolean = true) {
        if (safe) {
            AppGlobal.database.conversations.getById(conversationId) ?: return
        }

        AppGlobal.database.conversations.deleteById(conversationId)
    }

    @WorkerThread
    fun edit(message: VKMessage?, safe: Boolean = true) {
        message ?: return

        if (safe) {
            AppGlobal.database.messages.getById(message.messageId) ?: return
        }

        AppGlobal.database.messages.update(message)
    }

    fun clear() {
        users.clear()
        groups.clear()
    }
}