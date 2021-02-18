package ru.melod1n.project.vkm.database

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.melod1n.project.vkm.api.model.*
import ru.melod1n.project.vkm.database.dao.*

@Database(
    entities = [VKConversation::class, VKMessage::class, VKUser::class, VKGroup::class, VKFriend::class],
    version = 1,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {
    abstract val conversations: ConversationsDao
    abstract val messages: MessagesDao
    abstract val users: UsersDao
    abstract val groups: GroupsDao
    abstract val friends: FriendsDao
}