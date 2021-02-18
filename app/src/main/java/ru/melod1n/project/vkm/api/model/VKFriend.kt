package ru.melod1n.project.vkm.api.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friends")
class VKFriend() {

    @PrimaryKey(autoGenerate = false)
    var friendId: Int = -1

    var userId: Int = -1

    constructor(friendId: Int, userId: Int): this() {
        this.friendId = friendId
        this.userId = userId
    }
}