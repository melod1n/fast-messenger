package com.meloda.fast.fragment.ui.repository

import android.util.Log
import com.meloda.fast.api.VKApi
import com.meloda.fast.api.model.VKFriend
import com.meloda.fast.api.model.VKUser
import com.meloda.fast.common.TaskManager
import com.meloda.fast.database.MemoryCache
import com.meloda.fast.listener.OnResponseListener
import com.meloda.mvp.MvpOnLoadListener
import com.meloda.mvp.MvpRepository

class FriendsRepository : MvpRepository<VKUser>() {

    fun loadFriends(
        userId: Int,
        offset: Int,
        count: Int,
        listener: MvpOnLoadListener<ArrayList<VKUser>>
    ) {
        TaskManager.execute {
            VKApi.friends()
                .get()
                .order("hints")
                .userId(userId)
                .fields(VKUser.DEFAULT_FIELDS)
                .count(count)
                .offset(offset)
                .executeArray(VKUser::class.java,
                    object : OnResponseListener<ArrayList<VKUser>> {
                        override fun onResponse(response: ArrayList<VKUser>) {
                            Log.d("FriendsRepository", "get ${response.size} friends from api")

                            TaskManager.execute {
                                cacheLoadedUsers(userId, response)
                            }

                            sendResponse(listener, response)
                        }

                        override fun onError(t: Throwable) {
                            sendError(listener, t)
                        }
                    })
        }
    }

    fun getCachedFriends(
        userId: Int, offset: Int, count: Int, onlyOnline: Boolean,
        listener: MvpOnLoadListener<ArrayList<VKUser>>
    ) {
        TaskManager.execute {
            val friendsArray = MemoryCache.getFriends(userId)

            Log.d("FriendsRepository", "get ${friendsArray.size} friends from cache")

            if (friendsArray.isEmpty()) {
                sendError(listener, NullPointerException("Friends list is empty"))
                return@execute
            }

            val friends = arrayListOf<VKUser>()

            for (friend in friendsArray) {
                val user = MemoryCache.getUserById(friend.friendId)

                user?.let {
                    if (onlyOnline && user.isOnline || !onlyOnline) {
                        friends.add(user)
                    }
                }
            }

            sendResponse(listener, friends)
        }
    }

    private fun cacheLoadedUsers(userId: Int, users: ArrayList<VKUser>) {
        MemoryCache.putUsers(users)

        val friends = ArrayList<VKFriend>()

        for (user in users) {
            friends.add(VKFriend(user.userId, userId))
        }

        MemoryCache.putFriends(friends)
    }
}