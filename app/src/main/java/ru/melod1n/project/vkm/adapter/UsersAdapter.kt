package ru.melod1n.project.vkm.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import ru.melod1n.project.vkm.R
import ru.melod1n.project.vkm.adapter.diffutil.UsersCallback
import ru.melod1n.project.vkm.api.model.VKUser
import ru.melod1n.project.vkm.api.util.VKUtil
import ru.melod1n.project.vkm.base.BaseAdapter
import ru.melod1n.project.vkm.base.BaseHolder
import ru.melod1n.project.vkm.util.ImageUtils
import ru.melod1n.project.vkm.widget.CircleImageView

class UsersAdapter(context: Context, values: ArrayList<VKUser>) :
    BaseAdapter<VKUser, UsersAdapter.ViewHolder>(context, values) {

    var isLoading: Boolean = false
    var currentPosition: Int = 0

    fun isLastItem() = currentPosition >= itemCount - 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(view(R.layout.item_user, parent))
    }

    open inner class ViewHolder(v: View) : BaseHolder(v) {
        private val avatar: CircleImageView = v.findViewById(R.id.userAvatar)
        private val name: TextView = v.findViewById(R.id.userName)
        private val online: ImageView = v.findViewById(R.id.userOnline)
        private val onlineText: TextView = v.findViewById(R.id.userOnlineText)

        override fun bind(position: Int) {
            currentPosition = position

            val user = getItem(position)

            name.text = user.toString()

            val avatarPlaceholder = VKUtil.getAvatarPlaceholder(context, user.toString())
            avatar.setImageDrawable(avatarPlaceholder)

            ImageUtils.loadImage(user.photo200, avatar, avatarPlaceholder)

            online.isVisible = false

            VKUtil.getUserOnlineIcon(context, user)?.let {
                online.setImageDrawable(it)
                online.isVisible = true
            }

            onlineText.text = VKUtil.getUserOnline(user)

            //TODO: отладить открытие чата
        }
    }

    fun notifyChanges(oldList: List<VKUser>, newList: List<VKUser> = values) {
        val callback = UsersCallback(oldList, newList)
        val diff = DiffUtil.calculateDiff(callback, false)

        diff.dispatchUpdatesTo(this)
    }
}