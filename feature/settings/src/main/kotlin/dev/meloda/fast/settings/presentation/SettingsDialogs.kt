package dev.meloda.fast.settings.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import dev.meloda.fast.data.UserConfig
import dev.meloda.fast.datastore.SettingsKeys
import dev.meloda.fast.settings.model.SettingsDialog
import dev.meloda.fast.settings.model.SettingsScreenState
import dev.meloda.fast.ui.components.ActionInvokeDismiss
import dev.meloda.fast.ui.components.MaterialDialog
import dev.meloda.fast.ui.R as UiR

@Composable
fun HandleDialogs(
    screenState: SettingsScreenState,
    dialog: SettingsDialog?,
    onConfirmed: (SettingsDialog, Bundle) -> Unit = { _, _ -> },
    onDismissed: (SettingsDialog) -> Unit = {},
    onItemPicked: (SettingsDialog, Bundle) -> Unit = { _, _ -> }
) {
    if (dialog == null) return

    val context = LocalContext.current

    when (dialog) {
        is SettingsDialog.LogOut -> {
            val isEasterEgg = UserConfig.userId == SettingsKeys.ID_DMITRY

            MaterialDialog(
                onDismissRequest = { onDismissed(dialog) },
                title = stringResource(
                    id = if (isEasterEgg) UiR.string.easter_egg_log_out_dmitry
                    else UiR.string.sign_out_confirm_title
                ),
                text = stringResource(id = UiR.string.sign_out_confirm),
                confirmAction = { onConfirmed(dialog, bundleOf()) },
                confirmText = stringResource(
                    id = if (isEasterEgg) UiR.string.easter_egg_log_out_dmitry
                    else UiR.string.action_sign_out
                ),
                cancelText = stringResource(id = UiR.string.no),
                actionInvokeDismiss = ActionInvokeDismiss.Always
            )
        }

        is SettingsDialog.PerformCrash -> {
            MaterialDialog(
                onDismissRequest = { onDismissed(dialog) },
                title = "Perform crash",
                text = "App will be crashed. Are you sure?",
                confirmAction = { onConfirmed(dialog, bundleOf()) },
                confirmText = stringResource(id = UiR.string.yes),
                cancelText = stringResource(id = UiR.string.cancel),
                actionInvokeDismiss = ActionInvokeDismiss.Always
            )
        }

        is SettingsDialog.ImportAuthData -> {
            var accessToken by rememberSaveable {
                mutableStateOf("")
            }
            var exchangeToken by rememberSaveable {
                mutableStateOf("")
            }
            var trustedHash by rememberSaveable {
                mutableStateOf("")
            }

            MaterialDialog(
                onDismissRequest = { onDismissed(dialog) },
                title = "Import auth data",
                confirmAction = {
                    onConfirmed(
                        dialog,
                        bundleOf(
                            "ACCESS_TOKEN" to accessToken,
                            "EXCHANGE_TOKEN" to exchangeToken.ifEmpty { null },
                            "TRUSTED_HASH" to trustedHash.ifEmpty { null }
                        )
                    )
                },
                confirmText = "Import",
                cancelText = stringResource(UiR.string.cancel)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = accessToken,
                        onValueChange = { accessToken = it.trim() },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(text = "Access token") }
                    )

                    TextField(
                        value = exchangeToken,
                        onValueChange = { exchangeToken = it.trim() },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(text = "Exchange token") }
                    )

                    TextField(
                        value = trustedHash,
                        onValueChange = { trustedHash = it.trim() },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(text = "Trusted hash") }
                    )

                    Button(
                        onClick = {
                            val manager =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

                            manager.primaryClip?.let { data ->
                                val importedData = try {
                                    val data = data.getItemAt(0).text.trim()
                                    if (data.isEmpty()) {
                                        null
                                    } else {
                                        val split = data.split("\n")
                                        if (split.isEmpty() || split.size < 3) {
                                            null
                                        } else {
                                            val (newAccessToken) = split
                                            val newExchangeToken = split[1].trim().ifEmpty { null }
                                            val newTrustedHash = split[2].trim().ifEmpty { null }

                                            accessToken = newAccessToken
                                            exchangeToken = newExchangeToken.orEmpty()
                                            trustedHash = newTrustedHash.orEmpty()
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    null
                                }

                                if (importedData == null) {
                                    Toast.makeText(
                                        context,
                                        "Invalid data format. Can\'t import",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@let
                                }

                                Toast.makeText(
                                    context,
                                    "Success",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(text = "Import from clipboard")
                    }
                }
            }
        }

        is SettingsDialog.ExportAuthData -> {
            var accessToken by rememberSaveable {
                mutableStateOf(dialog.accessToken)
            }
            var exchangeToken by rememberSaveable {
                mutableStateOf(dialog.exchangeToken.orEmpty())
            }
            var trustedHash by rememberSaveable {
                mutableStateOf(dialog.trustedHash.orEmpty())
            }

            MaterialDialog(
                onDismissRequest = { onDismissed(dialog) },
                title = "Export auth data",
                confirmAction = {
                    onConfirmed(
                        dialog,
                        bundleOf(
                            "ACCESS_TOKEN" to accessToken,
                            "EXCHANGE_TOKEN" to exchangeToken.ifEmpty { null },
                            "TRUSTED_HASH" to trustedHash.ifEmpty { null }
                        )
                    )
                },
                confirmText = stringResource(UiR.string.ok),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = accessToken,
                        onValueChange = { accessToken = it.trim() },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(text = "Access token") }
                    )

                    TextField(
                        value = exchangeToken,
                        onValueChange = { exchangeToken = it.trim() },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(text = "Exchange token") }
                    )

                    TextField(
                        value = trustedHash,
                        onValueChange = { trustedHash = it.trim() },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(text = "Trusted hash") }
                    )

                    Button(
                        onClick = {
                            val manager =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

                            val textToCopy = buildString {
                                append(accessToken)
                                append("\n")

                                if (exchangeToken.isNotEmpty()) {
                                    append(exchangeToken)
                                }

                                append("\n")
                                if (trustedHash.isNotEmpty()) {
                                    append(trustedHash)
                                }
                            }

                            manager.setPrimaryClip(
                                ClipData.newPlainText("Fast auth data", textToCopy)
                            )
                            Toast.makeText(
                                context,
                                "Auth data copied to clipboard. Be careful with this data. If another person gets it, your account will be at risk",
                                Toast.LENGTH_LONG
                            ).show()
                            onDismissed(dialog)
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(text = "Copy to clipboard")
                    }
                }
            }
        }
    }
}
