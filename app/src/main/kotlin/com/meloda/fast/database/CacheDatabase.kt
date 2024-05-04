package com.meloda.fast.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.meloda.fast.database.dao.ConversationsDao
import com.meloda.fast.database.dao.GroupsDao
import com.meloda.fast.database.dao.MessagesDao
import com.meloda.fast.database.dao.UsersDao
import com.meloda.fast.database.model.VkConversationDB
import com.meloda.fast.database.model.VkGroupDB
import com.meloda.fast.database.model.VkMessageDB
import com.meloda.fast.database.model.VkUserDB

@Database(
    entities = [
        VkUserDB::class,
        VkGroupDB::class,
        VkMessageDB::class,
        VkConversationDB::class
    ],
    version = 3
)
abstract class CacheDatabase : RoomDatabase() {
    abstract fun usersDao(): UsersDao
    abstract fun groupsDao(): GroupsDao
    abstract fun messagesDao(): MessagesDao
    abstract fun conversationsDao(): ConversationsDao
}
