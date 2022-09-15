package com.meloda.fast.base.viewmodel

import android.content.Intent
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.meloda.fast.R
import com.meloda.fast.api.UserConfig
import com.meloda.fast.base.BaseFragment
import com.meloda.fast.common.Screens
import com.meloda.fast.screens.main.MainActivity
import com.meloda.fast.util.ViewUtils.showErrorDialog

object ViewModelUtils {

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
                (activity as? MainActivity)?.router?.navigateTo(
                    Screens.UserBanned(
                        memberName = event.memberName,
                        message = event.message,
                        restoreUrl = event.restoreUrl,
                        accessToken = event.accessToken
                    )
                )
            }

            is VkErrorEvent -> {
                event.errorText?.run {
                    activity.showErrorDialog(this)
                }
            }
        }
    }

    fun parseEvent(fragment: Fragment, event: VkEvent) {
        if (event is VkProgressEvent) {
            if (fragment is BaseFragment) {
                if (event is StartProgressEvent) {
                    fragment.startProgress()
                } else if (event is StopProgressEvent) {
                    fragment.stopProgress()
                }
            }
        } else {
            parseEvent(fragment.requireActivity(), event)
        }
    }
}