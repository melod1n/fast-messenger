package com.meloda.fast.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.meloda.fast.api.model.VkConversation
import com.meloda.fast.api.model.VkGroup
import com.meloda.fast.api.model.VkMessage
import com.meloda.fast.api.model.VkUser
import com.meloda.fast.data.account.AccountsDao
import com.meloda.fast.data.conversations.ConversationsDao
import com.meloda.fast.data.groups.GroupsDao
import com.meloda.fast.data.messages.MessagesDao
import com.meloda.fast.data.users.UsersDao
import com.meloda.fast.model.AppAccount

@Database(
    entities = [
        AppAccount::class,
        VkConversation::class,
        VkMessage::class,
        VkUser::class,
        VkGroup::class
    ],
    version = 33,
    exportSchema = true,
    autoMigrations = []
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract val accountsDao: AccountsDao
    abstract val conversationsDao: ConversationsDao
    abstract val messagesDao: MessagesDao
    abstract val usersDao: UsersDao
    abstract val groupsDao: GroupsDao

}