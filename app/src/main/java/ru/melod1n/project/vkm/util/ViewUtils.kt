package ru.melod1n.project.vkm.util

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import ru.melod1n.project.vkm.R
import ru.melod1n.project.vkm.api.model.VKUser
import ru.melod1n.project.vkm.api.util.VKUtil
import ru.melod1n.project.vkm.extensions.ContextExtensions.color
import ru.melod1n.project.vkm.widget.CircleImageView


object ViewUtils {

    fun showErrorSnackbar(view: View, t: Throwable) {
        Snackbar.make(
            view,
            Utils.getLocalizedThrowable(view.context, t),
            Snackbar.LENGTH_LONG
        ).show()
    }

    fun showErrorToast(context: Context, t: Throwable) {
        Toast.makeText(
            context,
            Utils.getLocalizedThrowable(context, t),
            Toast.LENGTH_LONG
        ).show()
    }

    fun prepareNavigationHeader(view: View, user: VKUser) {
        val profileName = view.findViewById<TextView>(R.id.headerName)

        profileName.text = user.toString()

        val profileStatus = view.findViewById<TextView>(R.id.headerStatus)

        val statusText = if (TextUtils.isEmpty(user.status)) "@id${user.userId}" else user.status

        profileStatus.text = statusText

        val profileAvatar: CircleImageView = view.findViewById(R.id.headerAvatar)

        if (AndroidUtils.hasConnection()) {
            Picasso.get().load(VKUtil.getUserPhoto(user)).into(profileAvatar)
        } else {
            profileAvatar.setImageDrawable(ColorDrawable(view.context.color(R.color.accent)))
        }
    }

}