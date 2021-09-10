package com.meloda.fast.database.old

import com.meloda.fast.database.old.DatabaseKeys.ACTION
import com.meloda.fast.database.old.DatabaseKeys.ATTACHMENTS
import com.meloda.fast.database.old.DatabaseKeys.CHAT_STATE
import com.meloda.fast.database.old.DatabaseKeys.CONVERSATION_ID
import com.meloda.fast.database.old.DatabaseKeys.CONVERSATION_MESSAGE_ID
import com.meloda.fast.database.old.DatabaseKeys.DATE
import com.meloda.fast.database.old.DatabaseKeys.DEACTIVATED
import com.meloda.fast.database.old.DatabaseKeys.EDIT_TIME
import com.meloda.fast.database.old.DatabaseKeys.FIRST_NAME
import com.meloda.fast.database.old.DatabaseKeys.FRIEND_ID
import com.meloda.fast.database.old.DatabaseKeys.FROM_ID
import com.meloda.fast.database.old.DatabaseKeys.FWD_MESSAGES
import com.meloda.fast.database.old.DatabaseKeys.GENDER
import com.meloda.fast.database.old.DatabaseKeys.GROUP_ID
import com.meloda.fast.database.old.DatabaseKeys.IN_READ_MESSAGE_ID
import com.meloda.fast.database.old.DatabaseKeys.IS_ALLOWED
import com.meloda.fast.database.old.DatabaseKeys.IS_CLOSED
import com.meloda.fast.database.old.DatabaseKeys.IS_GROUP_CHANNEL
import com.meloda.fast.database.old.DatabaseKeys.IS_NOTIFICATIONS_DISABLED
import com.meloda.fast.database.old.DatabaseKeys.IS_ONLINE
import com.meloda.fast.database.old.DatabaseKeys.IS_ONLINE_MOBILE
import com.meloda.fast.database.old.DatabaseKeys.IS_OUT
import com.meloda.fast.database.old.DatabaseKeys.LAST_MESSAGE_ID
import com.meloda.fast.database.old.DatabaseKeys.LAST_NAME
import com.meloda.fast.database.old.DatabaseKeys.LAST_SEEN
import com.meloda.fast.database.old.DatabaseKeys.LOCAL_ID
import com.meloda.fast.database.old.DatabaseKeys.MEMBERS_COUNT
import com.meloda.fast.database.old.DatabaseKeys.MESSAGE_ID
import com.meloda.fast.database.old.DatabaseKeys.NAME
import com.meloda.fast.database.old.DatabaseKeys.NOT_ALLOWED_REASON
import com.meloda.fast.database.old.DatabaseKeys.OUT_READ_MESSAGE_ID
import com.meloda.fast.database.old.DatabaseKeys.PEER_ID
import com.meloda.fast.database.old.DatabaseKeys.PHOTOS
import com.meloda.fast.database.old.DatabaseKeys.PINNED_MESSAGE_ID
import com.meloda.fast.database.old.DatabaseKeys.RANDOM_ID
import com.meloda.fast.database.old.DatabaseKeys.REPLY_MESSAGE_ID
import com.meloda.fast.database.old.DatabaseKeys.SCREEN_NAME
import com.meloda.fast.database.old.DatabaseKeys.SORT_ID
import com.meloda.fast.database.old.DatabaseKeys.STATUS
import com.meloda.fast.database.old.DatabaseKeys.TEXT
import com.meloda.fast.database.old.DatabaseKeys.TITLE
import com.meloda.fast.database.old.DatabaseKeys.TYPE
import com.meloda.fast.database.old.DatabaseKeys.UNREAD_COUNT
import com.meloda.fast.database.old.DatabaseKeys.USER_ID

object DatabaseUtils {

    const val TABLE_USERS = "users"
    const val TABLE_MESSAGES = "messages"
    const val TABLE_CHATS = "chats"
    const val TABLE_FRIENDS = "friends"
    const val TABLE_GROUPS = "groups"

    private val usersTableMap = HashMap<String, String>().apply {
        this[USER_ID] = "integer primary key on conflict replace"
        this[FIRST_NAME] = "varchar(255)"
        this[LAST_NAME] = "varchar(255)"
        this[DEACTIVATED] = "varchar(255)"
        this[GENDER] = "integer default 0"
        this[SCREEN_NAME] = "varchar(255)"
        this[PHOTOS] = "text"
        this[IS_ONLINE] = "integer default 0"
        this[IS_ONLINE_MOBILE] = "integer default 0"
        this[STATUS] = "varchar(255)"
        this[LAST_SEEN] = "integer"
    }

    private val groupsTableMap = HashMap<String, String>().apply {
        this[GROUP_ID] = "integer primary key on conflict replace"
        this[NAME] = "varchar(255)"
        this[SCREEN_NAME] = "varchar(255)"
        this[IS_CLOSED] = "integer default 0"
        this[DEACTIVATED] = "varchar(255)"
        this[TYPE] = "varchar(255)"
        this[PHOTOS] = "text"
    }

    private val messagesTableMap = HashMap<String, String>().apply {
        this[MESSAGE_ID] = "integer primary key on conflict replace"
        this[DATE] = "integer"
        this[PEER_ID] = "integer"
        this[FROM_ID] = "integer"
        this[EDIT_TIME] = "integer"
        this[IS_OUT] = "integer default 0"
        this[TEXT] = "text"
        this[RANDOM_ID] = "integer"
        this[CONVERSATION_MESSAGE_ID] = "integer"
        this[ATTACHMENTS] = "blob"
        this[REPLY_MESSAGE_ID] = "integer"
        this[ACTION] = "blob"

        //2,3,4,5 - message_ids
        this[FWD_MESSAGES] = "text"
    }

    private val chatsTableMap = HashMap<String, String>().apply {
        this[CONVERSATION_ID] = "integer primary key on conflict replace"
        this[IS_ALLOWED] = "integer default 1"
        this[NOT_ALLOWED_REASON] = "integer"
        this[IN_READ_MESSAGE_ID] = "integer"
        this[OUT_READ_MESSAGE_ID] = "integer"
        this[LAST_MESSAGE_ID] = "integer"
        this[UNREAD_COUNT] = "integer"
        this[LOCAL_ID] = "integer"
        this[IS_NOTIFICATIONS_DISABLED] = "integer default 0"
        this[MEMBERS_COUNT] = "integer"
        this[TITLE] = "varchar(255)"
        this[IS_GROUP_CHANNEL] = "integer default 0"
        this[TYPE] = "integer"
        this[CHAT_STATE] = "integer"
        this[PHOTOS] = "text"

        this[PINNED_MESSAGE_ID] = "integer"
    }

    private val friendsTableMap = HashMap<String, String>().apply {
        this[FRIEND_ID] = "integer primary key on conflict replace"
        this[SORT_ID] = "integer"

        //id which user friend
        this[USER_ID] = "integer"
    }

    fun createUsersTable() = createTableQuery(TABLE_USERS, usersTableMap)
    fun createGroupsTable() = createTableQuery(TABLE_GROUPS, groupsTableMap)
    fun createMessagesTable() = createTableQuery(TABLE_MESSAGES, messagesTableMap)
    fun createChatsTable() = createTableQuery(TABLE_CHATS, chatsTableMap)
    fun createFriendsTable() = createTableQuery(TABLE_FRIENDS, friendsTableMap)

    private fun createTableQuery(tableName: String, tableData: HashMap<String, String>): String {
        val builder = StringBuilder("create table $tableName (")

        val entry: Map.Entry<String, String> = tableData.entries.first()
        builder.append(entry.key)
        builder.append(" ")
        builder.append(entry.value)

        tableData.forEach {
            if (it == entry) return@forEach
            builder.append(", ")
            builder.append(it.key)
            builder.append(" ")
            builder.append(it.value)
        }

        builder.append(");")

        return builder.toString();
    }

}