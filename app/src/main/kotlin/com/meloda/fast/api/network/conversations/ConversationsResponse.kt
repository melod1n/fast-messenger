package com.meloda.fast.api.network.conversations

import com.meloda.fast.api.VkGroupsMap
import com.meloda.fast.api.VkUsersMap
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
    @Json(name = "groups") val groups: List<VkGroupData>?
) {

    fun toDomain(): ConversationsResponseDomain {
        val usersMap = VkUsersMap.forUsers(profiles.orEmpty())
        val groupsMap = VkGroupsMap.forGroups(groups.orEmpty())

        val conversations = items
            .map { item ->
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
                    )
                }

                item.conversation.mapToDomain(lastMessage).run {
                    val (user, group) = getUserAndGroup(
                        usersMap = usersMap,
                        groupsMap = groupsMap
                    )

                    copy(
                        conversationUser = user,
                        conversationGroup = group
                    )
                }
            }

        val messages = conversations.mapNotNull(VkConversationDomain::lastMessage)

        return ConversationsResponseDomain(
            count = count,
            conversations = conversations,
            messages = messages,
            profiles = usersMap.users(),
            groups = groupsMap.groups()
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
