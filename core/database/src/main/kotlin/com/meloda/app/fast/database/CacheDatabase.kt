package com.meloda.app.fast.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.meloda.app.fast.database.dao.ConversationDao
import com.meloda.app.fast.database.dao.GroupDao
import com.meloda.app.fast.database.dao.MessageDao
import com.meloda.app.fast.database.dao.UsersDao
import com.meloda.app.fast.database.typeconverters.Converters
import com.meloda.app.fast.model.database.VkConversationEntity
import com.meloda.app.fast.model.database.VkGroupEntity
import com.meloda.app.fast.model.database.VkMessageEntity
import com.meloda.app.fast.model.database.VkUserEntity

@Database(
    entities = [
        VkUserEntity::class,
        VkGroupEntity::class,
        VkMessageEntity::class,
        VkConversationEntity::class
    ],

    version = 6
)
@TypeConverters(Converters::class)
abstract class CacheDatabase : RoomDatabase() {
    abstract fun userDao(): UsersDao
    abstract fun groupDao(): GroupDao
    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao
}
