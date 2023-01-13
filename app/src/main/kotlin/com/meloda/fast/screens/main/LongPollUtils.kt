package com.meloda.fast.screens.main

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.edit
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.fondesa.kpermissions.coroutines.sendSuspend
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.fondesa.kpermissions.isGranted
import com.fondesa.kpermissions.isPermanentlyDenied
import com.meloda.fast.R
import com.meloda.fast.common.AppGlobal
import com.meloda.fast.ext.sdk33AndUp
import com.meloda.fast.model.base.Text
import com.meloda.fast.screens.settings.SettingsFragment
import com.meloda.fast.util.ViewUtils.showDialog
import kotlinx.coroutines.launch

object LongPollUtils {

    fun requestNotificationsPermission(
        fragmentActivity: FragmentActivity,
        onStateChangedAction: (LongPollState) -> Unit,
        fromSettings: Boolean = false,
    ) {
        val longPollInForegroundEnabled =
            AppGlobal.preferences.getBoolean(
                SettingsFragment.KEY_FEATURES_LONG_POLL_IN_BACKGROUND,
                SettingsFragment.DEFAULT_VALUE_FEATURES_LONG_POLL_IN_BACKGROUND
            )

        sdk33AndUp {
            fragmentActivity.lifecycleScope.launch {
                val result =
                    fragmentActivity.permissionsBuilder(Manifest.permission.POST_NOTIFICATIONS)
                        .build()
                        .sendSuspend()
                        .first()

                val resultToEmit: LongPollState = when {
                    longPollInForegroundEnabled && result.isGranted() -> LongPollState.ForegroundService
                    else -> LongPollState.DefaultService
                }

                onStateChangedAction.invoke(resultToEmit)

                val isLongPollOnlyInsideApp =
                    AppGlobal.preferences.getBoolean("lp_inside_app", false)

                if (result.isGranted()) {
                    AppGlobal.preferences.edit { putBoolean("lp_inside_app", false) }
                }

                if (longPollInForegroundEnabled &&
                    !result.isGranted() &&
                    (!isLongPollOnlyInsideApp || fromSettings)
                ) {
                    showNotificationsPermissionAlert(
                        fragmentActivity,
                        onStateChangedAction,
                        result.isPermanentlyDenied(),
                    )
                }
            }
        } ?: run {
            onStateChangedAction.invoke(
                if (longPollInForegroundEnabled) LongPollState.ForegroundService
                else LongPollState.DefaultService
            )
        }
    }

    private fun showNotificationsPermissionAlert(
        fragmentActivity: FragmentActivity,
        onStateChangedAction: (LongPollState) -> Unit,
        permanentlyDenied: Boolean,
    ) {
        val positiveText =
            Text.Simple(if (permanentlyDenied) "Open settings" else "Grant")
        val positiveAction = {
            if (permanentlyDenied) {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:${fragmentActivity.packageName}")
                )
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                try {
                    fragmentActivity.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                requestNotificationsPermission(fragmentActivity, onStateChangedAction)
            }
        }

        val neutralText =
            if (permanentlyDenied) Text.Resource(R.string.ok)
            else Text.Simple("Dismiss")
        val neutralAction = {
            if (permanentlyDenied) {
                AppGlobal.preferences.edit {
                    putBoolean("lp_inside_app", true)
                    putBoolean(SettingsFragment.KEY_FEATURES_LONG_POLL_IN_BACKGROUND, false)
                }
            } else Unit
        }

        fragmentActivity.showDialog(
            title = Text.Resource(R.string.warning),
            message = Text.Simple(
                "You denied notifications permission." +
                        "\nWithout notifications LongPoll service will work only inside app." +
                        "\nThis means that messages will only be updated while app is on the screen"
            ),
            positiveText = positiveText,
            positiveAction = positiveAction,
            neutralText = neutralText,
            neutralAction = neutralAction,
            isCancelable = false
        )
    }

}
