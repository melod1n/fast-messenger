package com.meloda.fast.base.viewmodel

import android.content.Intent
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.meloda.fast.R
import com.meloda.fast.activity.MainActivity
import com.meloda.fast.api.UserConfig
import com.meloda.fast.util.ViewUtils.showErrorDialog

object ViewModelUtils {

    fun parseEvent(activity: FragmentActivity, event: VkEvent) {
        when (event) {
            is ErrorEvent -> activity.showErrorDialog(event.errorText)
            is IllegalTokenEvent -> {
                Toast.makeText(
                    activity, R.string.authorization_failed, Toast.LENGTH_LONG
                ).show()

                UserConfig.clear()
                activity.finishAffinity()
                activity.startActivity(Intent(activity, MainActivity::class.java))
            }
        }
    }

    fun parseEvent(fragment: Fragment, event: VkEvent) {
        parseEvent(fragment.requireActivity(), event)
    }
}