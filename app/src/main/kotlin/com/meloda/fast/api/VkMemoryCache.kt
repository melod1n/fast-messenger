package com.meloda.fast.api

import com.meloda.fast.api.model.domain.VkContactDomain
import com.meloda.fast.api.model.domain.VkConversationDomain
import com.meloda.fast.api.model.domain.VkGroupDomain
import com.meloda.fast.api.model.domain.VkMessageDomain
import com.meloda.fast.api.model.domain.VkUserDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.math.abs

object VkMemoryCache {

    private val users: HashMap<Int, VkUserDomain> = hashMapOf()
    private val groups: HashMap<Int, VkGroupDomain> = hashMapOf()
    private val messages: HashMap<Int, VkMessageDomain> = hashMapOf()
    private val conversations: HashMap<Int, VkConversationDomain> = hashMapOf()
    private val contacts: HashMap<Int, VkContactDomain> = hashMapOf()

    fun appendUsers(users: List<VkUserDomain>) {
        users.forEach { user -> this.users[user.id] = user }
    }

    fun appendGroups(groups: List<VkGroupDomain>) {
        groups.forEach { group -> this.groups[abs(group.id)] = group }
    }

    fun appendMessages(messages: List<VkMessageDomain>) {
        messages.forEach { message -> this.messages[message.id] = message }
    }

    fun appendConversations(conversations: List<VkConversationDomain>) {
        conversations.forEach { conversation -> this.conversations[conversation.id] = conversation }
    }

    fun appendContacts(contacts: List<VkContactDomain>) {
        contacts.forEach { contact -> this.contacts[contact.userId] = contact }
    }

    operator fun set(userId: Int, user: VkUserDomain) {
        users[userId] = user
    }

    operator fun set(groupId: Int, group: VkGroupDomain) {
        groups[groupId] = group
    }

    operator fun set(messageId: Int, message: VkMessageDomain) {
        messages[messageId] = message
    }

    operator fun set(conversationId: Int, conversation: VkConversationDomain) {
        conversations[conversationId] = conversation
    }

    operator fun set(contactId: Int, contact: VkContactDomain) {
        contacts[contactId] = contact
    }

    fun getUser(id: Int): VkUserDomain? {
        return getUsers(id).firstOrNull()
    }

    fun getUsers(vararg ids: Int): List<VkUserDomain> {
        return getUsers(ids.toList())
    }

    fun getUsers(ids: List<Int>): List<VkUserDomain> {
        return ids.mapNotNull { id -> users[id] }
    }

    fun getGroup(id: Int): VkGroupDomain? {
        return getGroups(id).firstOrNull()
    }

    fun getGroups(vararg ids: Int): List<VkGroupDomain> {
        return getGroups(ids.toList())
    }

    fun getGroups(ids: List<Int>): List<VkGroupDomain> {
        return ids.mapNotNull { id -> groups[id] }
    }

    fun getMessage(id: Int): VkMessageDomain? {
        return getMessages(id).firstOrNull()
    }

    fun getMessages(vararg ids: Int): List<VkMessageDomain> {
        return getMessages(ids.toList())
    }

    fun getMessages(ids: List<Int>): List<VkMessageDomain> {
        return ids.mapNotNull { id -> messages[id] }
    }

    fun getConversation(id: Int): VkConversationDomain? {
        return getConversations(id).firstOrNull()
    }

    fun getConversations(vararg ids: Int): List<VkConversationDomain> {
        return getConversations(ids.toList())
    }

    fun getConversations(ids: List<Int>): List<VkConversationDomain> {
        return ids.mapNotNull { id -> conversations[id] }
    }

    fun getContact(id: Int): VkContactDomain? {
        return getContacts(id).firstOrNull()
    }

    fun getContacts(vararg ids: Int): List<VkContactDomain> {
        return getContacts(ids.toList())
    }

    fun getContacts(ids: List<Int>): List<VkContactDomain> {
        return ids.mapNotNull { id -> contacts[id] }
    }
}
