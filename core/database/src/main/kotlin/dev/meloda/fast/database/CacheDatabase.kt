package dev.meloda.fast.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.meloda.fast.database.dao.ConversationDao
import dev.meloda.fast.database.dao.GroupDao
import dev.meloda.fast.database.dao.MessageDao
import dev.meloda.fast.database.dao.UsersDao
import dev.meloda.fast.database.typeconverters.Converters
import dev.meloda.fast.model.database.VkConversationEntity
import dev.meloda.fast.model.database.VkGroupEntity
import dev.meloda.fast.model.database.VkMessageEntity
import dev.meloda.fast.model.database.VkUserEntity

@Database(
    entities = [
        VkUserEntity::class,
        VkGroupEntity::class,
        VkMessageEntity::class,
        VkConversationEntity::class
    ],

    version = 8
)
@TypeConverters(Converters::class)
abstract class CacheDatabase : RoomDatabase() {
    abstract fun userDao(): UsersDao
    abstract fun groupDao(): GroupDao
    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao
}
