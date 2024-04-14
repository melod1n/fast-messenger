package com.meloda.fast.api.network.conversations

import com.meloda.fast.api.VkGroupsMap
import com.meloda.fast.api.VkMemoryCache
import com.meloda.fast.api.VkUsersMap
import com.meloda.fast.api.model.data.VkContactData
import com.meloda.fast.api.model.data.VkConversationData
import com.meloda.fast.api.model.data.VkGroupData
import com.meloda.fast.api.model.data.VkMessageData
import com.meloda.fast.api.model.data.VkUserData
import com.meloda.fast.api.model.domain.VkConversationDomain
import com.meloda.fast.api.model.domain.VkGroupDomain
import com.meloda.fast.api.model.domain.VkMessageDomain
import com.meloda.fast.api.model.domain.VkUserDomain
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ConversationsGetResponse(
    @Json(name = "count") val count: Int,
    @Json(name = "items") val items: List<ConversationsResponseItems>,
    @Json(name = "unread_count") val unreadCount: Int?,
    @Json(name = "profiles") val profiles: List<VkUserData>?,
    @Json(name = "groups") val groups: List<VkGroupData>?,
    @Json(name = "contacts") val contacts: List<VkContactData>?
) {

    fun toDomain(): ConversationsResponseDomain {
        val profilesList = profiles.orEmpty().map(VkUserData::mapToDomain)
        val groupsList = groups.orEmpty().map(VkGroupData::mapToDomain)
        val contactsList = contacts.orEmpty().map(VkContactData::mapToDomain)

        VkMemoryCache.appendUsers(profilesList)
        VkMemoryCache.appendGroups(groupsList)
        VkMemoryCache.appendContacts(contactsList)

        val usersMap = VkUsersMap.forUsers(profilesList)
        val groupsMap = VkGroupsMap.forGroups(groupsList)

        val conversations = items.map { item ->
            val lastMessage = item.lastMessage?.mapToDomain()?.run {
                val (actionUser, actionGroup) = getActionUserAndGroup(
                    usersMap = usersMap,
                    groupsMap = groupsMap
                )

                val (messageUser, messageGroup) = getUserAndGroup(
                    usersMap = usersMap,
                    groupsMap = groupsMap
                )

                copy(
                    user = messageUser,
                    group = messageGroup,
                    actionUser = actionUser,
                    actionGroup = actionGroup
                ).also { message -> VkMemoryCache[message.id] = message }
            }

            item.conversation.mapToDomain(lastMessage).run {
                val (user, group) = getUserAndGroup(
                    usersMap = usersMap,
                    groupsMap = groupsMap
                )

                copy(
                    conversationUser = user,
                    conversationGroup = group
                ).also { conversation -> VkMemoryCache[conversation.id] = conversation }
            }
        }

        val messages = conversations.mapNotNull(VkConversationDomain::lastMessage)

        return ConversationsResponseDomain(
            count = count,
            conversations = conversations,
            messages = messages,
            profiles = profilesList,
            groups = groupsList
        )
    }
}

@JsonClass(generateAdapter = true)
data class ConversationsResponseItems(
    @Json(name = "conversation") val conversation: VkConversationData,
    @Json(name = "last_message") val lastMessage: VkMessageData?
)


data class ConversationsResponseDomain(
    val count: Int,
    val conversations: List<VkConversationDomain>,
    val messages: List<VkMessageDomain>,
    val profiles: List<VkUserDomain>,
    val groups: List<VkGroupDomain>,
)
