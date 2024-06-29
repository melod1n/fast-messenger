package com.meloda.app.fast.model.api.responses

import com.meloda.app.fast.model.api.data.VkContactData
import com.meloda.app.fast.model.api.data.VkConversationData
import com.meloda.app.fast.model.api.data.VkGroupData
import com.meloda.app.fast.model.api.data.VkMessageData
import com.meloda.app.fast.model.api.data.VkUserData
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ConversationsGetResponse(
    @Json(name = "count") val count: Int,
    @Json(name = "items") val items: List<ConversationsResponseItem>,
    @Json(name = "unread_count") val unreadCount: Int?,
    @Json(name = "profiles") val profiles: List<VkUserData>?,
    @Json(name = "groups") val groups: List<VkGroupData>?,
    @Json(name = "contacts") val contacts: List<VkContactData>?
) {

//    fun toDomain(): ConversationsResponseDomain {
//        val profilesList = profiles.orEmpty().map(VkUserData::mapToDomain)
//        val groupsList = groups.orEmpty().map(VkGroupData::mapToDomain)
//        val contactsList = contacts.orEmpty().map(VkContactData::mapToDomain)
//
//        VkMemoryCache.appendUsers(profilesList)
//        VkMemoryCache.appendGroups(groupsList)
//        VkMemoryCache.appendContacts(contactsList)
//
//        val usersMap = VkUsersMap.forUsers(profilesList)
//        val groupsMap = VkGroupsMap.forGroups(groupsList)
//
//        val conversations = items.map { item ->
//            val lastMessage = item.lastMessage?.mapToDomain()?.run {
//                val (actionUser, actionGroup) = getActionUserAndGroup(
//                    usersMap = usersMap,
//                    groupsMap = groupsMap
//                )
//
//                val (messageUser, messageGroup) = getUserAndGroup(
//                    usersMap = usersMap,
//                    groupsMap = groupsMap
//                )
//
//                copy(
//                    user = messageUser,
//                    group = messageGroup,
//                    actionUser = actionUser,
//                    actionGroup = actionGroup
//                ).also { message -> VkMemoryCache[message.id] = message }
//            }
//
//            item.conversation.mapToDomain(lastMessage).run {
//                val (user, group) = getUserAndGroup(
//                    usersMap = usersMap,
//                    groupsMap = groupsMap
//                )
//
//                copy(
//                    conversationUser = user,
//                    conversationGroup = group
//                ).also { conversation -> VkMemoryCache[conversation.id] = conversation }
//            }
//        }
//
//        val messages = conversations.mapNotNull(VkConversationDomain::lastMessage)
//
//        return ConversationsResponseDomain(
//            count = count,
//            conversations = conversations,
//            messages = messages,
//            profiles = profilesList,
//            groups = groupsList
//        )
//    }
}

@JsonClass(generateAdapter = true)
data class ConversationsResponseItem(
    @Json(name = "conversation") val conversation: VkConversationData,
    @Json(name = "last_message") val lastMessage: VkMessageData?
)
