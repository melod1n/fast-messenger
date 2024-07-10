package com.meloda.app.fast.model.api.domain

data class VkChatDomain(
    val type: String,
    val title: String,
    val adminId: Int,
    val membersCount: Int,
    val id: Int,
    val members: List<ChatMember> = emptyList(),
    val photo50: String,
    val photo100: String,
    val photo200: String,
    val isDefaultPhoto: Boolean
) {
    data class ChatMember(
        val id: Int,
        val type: ChatMemberType,
        val isOnline: Boolean?,
        val lastSeen: Int?,
        val name: String?,
        val firstName: String?,
        val lastName: String?,
        val invitedBy: Int,
        val photo50: String?,
        val photo100: String?,
        val photo200: String?,
        val isOwner: Boolean,
        val isAdmin: Boolean,
        val canKick: Boolean
    ) {

        fun isProfile(): Boolean = type == ChatMemberType.Profile

        fun isGroup(): Boolean = type == ChatMemberType.Group

        enum class ChatMemberType(val value: String) {
            Profile("profile"), Group("group");

            companion object {
                fun parse(value: String) = entries.first { it.value == value }
            }
        }
    }
}
