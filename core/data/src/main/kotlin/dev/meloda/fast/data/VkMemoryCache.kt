package dev.meloda.fast.data

import dev.meloda.fast.data.UserConfig.userId
import dev.meloda.fast.model.api.domain.VkContactDomain
import dev.meloda.fast.model.api.domain.VkConvo
import dev.meloda.fast.model.api.domain.VkGroupDomain
import dev.meloda.fast.model.api.domain.VkMessage
import dev.meloda.fast.model.api.domain.VkUser
import kotlin.math.abs

object VkMemoryCache {

    private val users: HashMap<Long, VkUser> = hashMapOf()
    private val groups: HashMap<Long, VkGroupDomain> = hashMapOf()
    private val messages: HashMap<Long, VkMessage> = hashMapOf()
    private val convos: HashMap<Long, VkConvo> = hashMapOf()
    private val contacts: HashMap<Long, VkContactDomain> = hashMapOf()

    fun appendUsers(users: List<VkUser>) {
        users.forEach { user -> VkMemoryCache.users[user.id] = user }
    }

    fun appendGroups(groups: List<VkGroupDomain>) {
        groups.forEach { group -> VkMemoryCache.groups[abs(group.id)] = group }
    }

    fun appendMessages(messages: List<VkMessage>) {
        messages.forEach { message -> VkMemoryCache.messages[message.id] = message }
    }

    fun appendConvos(convos: List<VkConvo>) {
        convos.forEach { convo ->
            VkMemoryCache.convos[convo.id] = convo
        }
    }

    fun appendContacts(contacts: List<VkContactDomain>) {
        contacts.forEach { contact -> VkMemoryCache.contacts[contact.userId] = contact }
    }

    operator fun set(userid: Long, user: VkUser) {
        users[userId] = user
    }

    operator fun set(groupId: Long, group: VkGroupDomain) {
        groups[groupId] = group
    }

    operator fun set(messageId: Long, message: VkMessage) {
        messages[messageId] = message
    }

    operator fun set(convoId: Long, convo: VkConvo) {
        convos[convoId] = convo
    }

    operator fun set(contactId: Long, contact: VkContactDomain) {
        contacts[contactId] = contact
    }

    fun getUser(id: Long): VkUser? {
        return getUsers(id).firstOrNull()
    }

    fun getUsers(vararg ids: Long): List<VkUser> {
        return getUsers(ids.toList())
    }

    fun getUsers(ids: List<Long>): List<VkUser> {
        return ids.mapNotNull { id -> users[id] }
    }

    fun getGroup(id: Long): VkGroupDomain? {
        return getGroups(id).firstOrNull()
    }

    fun getGroups(vararg ids: Long): List<VkGroupDomain> {
        return getGroups(ids.toList())
    }

    fun getGroups(ids: List<Long>): List<VkGroupDomain> {
        return ids.mapNotNull { id -> groups[id] }
    }

    fun getMessage(id: Long): VkMessage? {
        return getMessages(id).firstOrNull()
    }

    fun getMessages(vararg ids: Long): List<VkMessage> {
        return getMessages(ids.toList())
    }

    fun getMessages(ids: List<Long>): List<VkMessage> {
        return ids.mapNotNull { id -> messages[id] }
    }

    fun getConvo(id: Long): VkConvo? {
        return getConvos(id).firstOrNull()
    }

    fun getConvos(vararg ids: Long): List<VkConvo> {
        return getConvos(ids.toList())
    }

    fun getConvos(ids: List<Long>): List<VkConvo> {
        return ids.mapNotNull { id -> convos[id] }
    }

    fun getContact(id: Long): VkContactDomain? {
        return getContacts(id).firstOrNull()
    }

    fun getContacts(vararg ids: Long): List<VkContactDomain> {
        return getContacts(ids.toList())
    }

    fun getContacts(ids: List<Long>): List<VkContactDomain> {
        return ids.mapNotNull { id -> contacts[id] }
    }
}
