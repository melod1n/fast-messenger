package com.meloda.fast.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.meloda.fast.api.model.VkConversation
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.database.dao.*
import com.meloda.fast.model.AppAccount

@Database(
    entities = [
        AppAccount::class,
        VkConversation::class,
        VkMessage::class,
        VkUser::class,
        VkGroup::class
    ],
    version = 30,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract val accountsDao: AccountsDao
    abstract fun conversationsDao(): ConversationsDao
    abstract fun messagesDao(): MessagesDao
    abstract fun usersDao(): UsersDao
    abstract fun groupsDao(): GroupsDao

}