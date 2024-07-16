package dev.meloda.fast.data

import dev.meloda.fast.model.api.domain.VkContactDomain
import dev.meloda.fast.model.api.domain.VkConversation
import dev.meloda.fast.model.api.domain.VkGroupDomain
import dev.meloda.fast.model.api.domain.VkMessage
import dev.meloda.fast.model.api.domain.VkUser
import kotlin.math.abs

object VkMemoryCache {

    private val users: HashMap<Int, VkUser> = hashMapOf()
    private val groups: HashMap<Int, VkGroupDomain> = hashMapOf()
    private val messages: HashMap<Int, VkMessage> = hashMapOf()
    private val conversations: HashMap<Int, VkConversation> = hashMapOf()
    private val contacts: HashMap<Int, VkContactDomain> = hashMapOf()

    fun appendUsers(users: List<VkUser>) {
        users.forEach { user -> VkMemoryCache.users[user.id] = user }
    }

    fun appendGroups(groups: List<VkGroupDomain>) {
        groups.forEach { group -> VkMemoryCache.groups[abs(group.id)] = group }
    }

    fun appendMessages(messages: List<VkMessage>) {
        messages.forEach { message -> VkMemoryCache.messages[message.id] = message }
    }

    fun appendConversations(conversations: List<VkConversation>) {
        conversations.forEach { conversation ->
            VkMemoryCache.conversations[conversation.id] = conversation
        }
    }

    fun appendContacts(contacts: List<VkContactDomain>) {
        contacts.forEach { contact -> VkMemoryCache.contacts[contact.userId] = contact }
    }

    operator fun set(userId: Int, user: VkUser) {
        users[userId] = user
    }

    operator fun set(groupId: Int, group: VkGroupDomain) {
        groups[groupId] = group
    }

    operator fun set(messageId: Int, message: VkMessage) {
        messages[messageId] = message
    }

    operator fun set(conversationId: Int, conversation: VkConversation) {
        conversations[conversationId] = conversation
    }

    operator fun set(contactId: Int, contact: VkContactDomain) {
        contacts[contactId] = contact
    }

    fun getUser(id: Int): VkUser? {
        return getUsers(id).firstOrNull()
    }

    fun getUsers(vararg ids: Int): List<VkUser> {
        return getUsers(ids.toList())
    }

    fun getUsers(ids: List<Int>): List<VkUser> {
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

    fun getMessage(id: Int): VkMessage? {
        return getMessages(id).firstOrNull()
    }

    fun getMessages(vararg ids: Int): List<VkMessage> {
        return getMessages(ids.toList())
    }

    fun getMessages(ids: List<Int>): List<VkMessage> {
        return ids.mapNotNull { id -> messages[id] }
    }

    fun getConversation(id: Int): VkConversation? {
        return getConversations(id).firstOrNull()
    }

    fun getConversations(vararg ids: Int): List<VkConversation> {
        return getConversations(ids.toList())
    }

    fun getConversations(ids: List<Int>): List<VkConversation> {
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
