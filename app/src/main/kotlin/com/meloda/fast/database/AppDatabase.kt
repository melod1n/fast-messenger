package com.meloda.fast.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.meloda.fast.api.model.VkConversation
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.database.dao.ConversationsDao
import com.meloda.fast.database.dao.GroupsDao
import com.meloda.fast.database.dao.MessagesDao
import com.meloda.fast.database.dao.UsersDao

@Database(
    entities = [
        VkConversation::class,
        VkMessage::class,
        VkUser::class,
        VkGroup::class
    ],
    version = 15,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun conversationsDao(): ConversationsDao
    abstract fun messagesDao(): MessagesDao
    abstract fun usersDao(): UsersDao
    abstract fun groupsDao(): GroupsDao

}