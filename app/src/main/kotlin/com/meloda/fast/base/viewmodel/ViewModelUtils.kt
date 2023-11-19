package com.meloda.fast.base.viewmodel

import android.content.Intent
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.ext.showDialog
import com.meloda.fast.model.base.UiText
import com.meloda.fast.screens.main.MainActivity

object ViewModelUtils {

    @Deprecated("rewrite")
    @Suppress("MemberVisibilityCanBePrivate")
    fun parseEvent(activity: FragmentActivity, event: VkEvent) {
        when (event) {
            is AuthorizationErrorEvent -> {
                Toast.makeText(
                    activity, R.string.authorization_failed, Toast.LENGTH_LONG
                ).show()

                UserConfig.clear()
                activity.finishAffinity()
                activity.startActivity(Intent(activity, MainActivity::class.java))
            }
            is TokenExpiredErrorEvent -> {
                Toast.makeText(
                    activity, R.string.token_expired, Toast.LENGTH_LONG
                ).show()

                UserConfig.clear()
                activity.finishAffinity()
                activity.startActivity(Intent(activity, MainActivity::class.java))
            }
            is UserBannedEvent -> {
                // TODO: 17.04.2023, Danil Nikolaev: handle banned event
//                (activity as? MainActivity)?.accessRouter()?.newRootScreen(
//                    Screens.UserBanned(
//                        memberName = event.memberName,
//                        message = event.message,
//                        restoreUrl = event.restoreUrl,
//                        accessToken = event.accessToken
//                    )
//                )
            }
            is UnknownErrorEvent -> {
                activity.showDialog(
                    title = UiText.Resource(R.string.title_error),
                    message = UiText.Resource(R.string.unknown_error_occurred),
                    positiveText = UiText.Resource(R.string.ok)
                )
            }
            is VkErrorEvent -> {
                event.errorText?.run {
                    activity.showDialog(
                        title = UiText.Resource(R.string.title_error),
                        message = UiText.Simple(this),
                        positiveText = UiText.Resource(R.string.ok)
                    )
                }
            }
        }
    }

    @Deprecated("rewrite")
    fun parseEvent(fragment: Fragment, event: VkEvent) {
        parseEvent(fragment.requireActivity(), event)
    }
}
