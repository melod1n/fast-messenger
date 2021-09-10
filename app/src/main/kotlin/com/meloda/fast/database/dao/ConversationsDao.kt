package com.meloda.fast.database.dao

import androidx.room.Dao
import com.meloda.fast.api.model.VkConversation

@Dao
interface ConversationsDao : KindaDao<VkConversation> {
}