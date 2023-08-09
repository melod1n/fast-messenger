package com.meloda.fast.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.api.model.domain.VkConversationDomain
import com.meloda.fast.data.conversations.ConversationsDao
import com.meloda.fast.data.groups.GroupsDao
import com.meloda.fast.data.messages.MessagesDao
import com.meloda.fast.data.users.UsersDao

@Database(
    entities = [
        VkConversationDomain::class,
        VkMessage::class,
        VkUser::class,
        VkGroup::class
    ],
    version = 42,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CacheDatabase : RoomDatabase() {

    abstract val conversationsDao: ConversationsDao
    abstract val messagesDao: MessagesDao
    abstract val usersDao: UsersDao
    abstract val groupsDao: GroupsDao

}
