package com.meloda.fast.api.network.conversations

import com.meloda.fast.api.VkUtils.fill
import com.meloda.fast.api.model.domain.VkGroupDomain
import com.meloda.fast.api.model.domain.VkMessageDomain
import com.meloda.fast.api.model.domain.VkUserDomain
import com.meloda.fast.api.model.data.VkGroupData
import com.meloda.fast.api.model.data.VkMessageData
import com.meloda.fast.api.model.data.VkUserData
import com.meloda.fast.api.model.data.VkConversationData
import com.meloda.fast.api.model.domain.VkConversationDomain
import com.meloda.fast.ext.toMap
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
        val profiles = profiles
            ?.map(VkUserData::mapToDomain)
            ?.toMap(hashMapOf(), VkUserDomain::id) ?: hashMapOf()

        val groups = groups
            ?.map(VkGroupData::mapToDomain)
            ?.toMap(hashMapOf(), VkGroupDomain::id) ?: hashMapOf()

        val conversations = items
            .map { item ->
                val lastMessage = item.lastMessage?.asVkMessage()
                item.conversation.mapToDomain()
                    .fill(
                        lastMessage = lastMessage,
                        profiles = profiles,
                        groups = groups
                    )
            }

        val messages = conversations.mapNotNull { conversation ->
            val message = conversation.lastMessage
            message?.copy(
                user = profiles[message.fromId],
                group = groups[message.fromId],
                actionUser = profiles[message.actionMemberId],
                actionGroup = groups[message.actionMemberId]
            )
        }

        return ConversationsResponseDomain(
            count = count,
            conversations = conversations,
            messages = messages,
            profiles = profiles.values.toList(),
            groups = groups.values.toList()
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
