package com.meloda.app.fast.auth.screens.twofa.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meloda.app.fast.auth.screens.twofa.TwoFaViewModel
import com.meloda.app.fast.auth.screens.twofa.TwoFaViewModelImpl
import com.meloda.app.fast.auth.screens.twofa.model.TwoFaArguments
import com.meloda.app.fast.auth.screens.twofa.model.TwoFaUiAction
import com.meloda.app.fast.common.UiText
import com.meloda.app.fast.designsystem.MaterialDialog
import com.meloda.app.fast.designsystem.TextFieldErrorText
import com.meloda.app.fast.designsystem.getString
import org.koin.androidx.compose.koinViewModel
import com.meloda.app.fast.designsystem.R as UiR

private typealias OnAction = (TwoFaUiAction) -> Unit

@Composable
fun TwoFaScreen(
    arguments: TwoFaArguments,
    onAction: OnAction,
    viewModel: TwoFaViewModel = koinViewModel<TwoFaViewModelImpl>(),
) {
    viewModel.setArguments(arguments)

    val focusManager = LocalFocusManager.current

    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    var confirmedExit by rememberSaveable {
        mutableStateOf(false)
    }

    var showExitAlert by rememberSaveable {
        mutableStateOf(false)
    }

    if (confirmedExit) {
        onAction(TwoFaUiAction.BackClicked)
    }

    BackHandler(enabled = !confirmedExit) {
        if (!confirmedExit) {
            showExitAlert = true
        }
    }

    if (showExitAlert) {
        MaterialDialog(
            onDismissAction = { showExitAlert = false },
            title = UiText.Simple("Confirmation"),
            text = UiText.Simple("Are you sure? Authorization process will be cancelled."),
            confirmText = UiText.Resource(UiR.string.yes),
            confirmAction = {
                confirmedExit = true
            },
            cancelText = UiText.Resource(UiR.string.no)
        )
    }

    if (screenState.isNeedToOpenLogin) {
        viewModel.onNavigatedToLogin()

        val code = screenState.twoFaCode
        if (code == null) {
            onAction(TwoFaUiAction.BackClicked)
        } else {
            onAction(TwoFaUiAction.CodeResult(code = code))
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(30.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            ExtendedFloatingActionButton(
                onClick = { onAction(TwoFaUiAction.BackClicked) },
                text = {
                    Text(
                        text = "Cancel",
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close icon",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {

                Text(
                    text = "Two-Factor\nAuthentication",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(38.dp))
                Text(
                    text = screenState.twoFaText.getString().orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))

                val delayRemainedTime = screenState.delayTime
                AnimatedVisibility(visible = delayRemainedTime > 0) {
                    Text(
                        text = "Can resend after $delayRemainedTime seconds",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                var code by remember { mutableStateOf(TextFieldValue(screenState.twoFaCode.orEmpty())) }
                val codeError = screenState.codeError

                TextField(
                    value = code,
                    onValueChange = { newText ->
                        if (newText.text.length > 6) return@TextField

                        code = newText
                        viewModel.onCodeInputChanged((newText.text))
                    },
                    label = { Text(text = "Code") },
                    placeholder = { Text(text = "Code") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp)),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = UiR.drawable.round_qr_code_24),
                            contentDescription = "QR Code icon",
                            tint = if (codeError != null) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Number
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            viewModel.onTextFieldDoneClicked()
                        }
                    ),
                    isError = codeError != null
                )

                AnimatedVisibility(visible = codeError != null) {
                    TextFieldErrorText(text = codeError.orEmpty())
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val canResendSms = screenState.canResendSms

                AnimatedVisibility(
                    visible = canResendSms,
                ) {
                    ExtendedFloatingActionButton(
                        onClick = viewModel::onRequestSmsButtonClicked,
                        text = {
                            Text(
                                text = "Request SMS",
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = UiR.drawable.round_sms_24),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                contentDescription = "SMS icon"
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                FloatingActionButton(
                    onClick = viewModel::onDoneButtonClicked,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Done,
                        contentDescription = "Done icon",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                AnimatedVisibility(
                    visible = !canResendSms,
                    exit = shrinkHorizontally()
                ) {
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }
        }
    }
}
