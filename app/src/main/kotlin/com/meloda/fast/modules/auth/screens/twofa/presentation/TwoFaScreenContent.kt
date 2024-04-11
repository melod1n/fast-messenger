package com.meloda.fast.modules.auth.screens.twofa.presentation

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meloda.fast.R
import com.meloda.fast.compose.MaterialDialog
import com.meloda.fast.ext.getString
import com.meloda.fast.model.base.UiText
import com.meloda.fast.model.base.toUiText
import com.meloda.fast.modules.auth.screens.twofa.TwoFaViewModel
import com.meloda.fast.modules.auth.screens.twofa.TwoFaViewModelImpl
import com.meloda.fast.modules.auth.screens.twofa.model.TwoFaArguments
import com.meloda.fast.modules.auth.screens.twofa.model.TwoFaScreenState
import com.meloda.fast.modules.auth.screens.twofa.model.UiAction
import com.meloda.fast.ui.AppTheme
import com.meloda.fast.ui.widgets.TextFieldErrorText
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.KoinContext
import org.koin.compose.koinInject

typealias OnAction = (UiAction) -> Unit

@Composable
fun TwoFaRoute(
    onAction: OnAction,
    viewModel: TwoFaViewModel = koinViewModel<TwoFaViewModelImpl>()
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    var confirmedExit by rememberSaveable {
        mutableStateOf(false)
    }

    var showExitAlert by rememberSaveable {
        mutableStateOf(false)
    }

    if (confirmedExit) {
        onAction(UiAction.BackClicked)
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
            confirmText = UiText.Resource(R.string.yes),
            confirmAction = {
                confirmedExit = true
            },
            cancelText = UiText.Resource(R.string.no)
        )
    }

    if (screenState.isNeedToOpenLogin) {
        viewModel.onNavigatedToLogin()

        val code = screenState.twoFaCode
        if (code == null) {
            onAction(UiAction.BackClicked)
        } else {
            onAction(UiAction.CodeResult(code = code))
        }
    }

    TwoFaScreenContent(
        onAction = onAction,
        screenState = screenState
    )
}

@Composable
fun TwoFaScreenContent(
    onAction: OnAction,
    screenState: TwoFaScreenState,
) {
    val focusManager = LocalFocusManager.current

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(30.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            ExtendedFloatingActionButton(
                onClick = { onAction(UiAction.BackClicked) },
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
                        onAction(UiAction.CodeInputChanged(newText.text))
                    },
                    label = { Text(text = "Code") },
                    placeholder = { Text(text = "Code") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp)),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.round_qr_code_24),
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
                            onAction(UiAction.TextFieldDoneClicked)
                        }
                    ),
                    isError = codeError != null
                )

                AnimatedVisibility(visible = codeError != null) {
                    TextFieldErrorText(text = codeError.getString().orEmpty())
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
                        onClick = { onAction(UiAction.RequestSmsButtonClicked) },
                        text = {
                            Text(
                                text = "Request SMS",
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.round_sms_24),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                contentDescription = "SMS icon"
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                FloatingActionButton(
                    onClick = { onAction(UiAction.DoneButtonClicked) },
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

@Preview
@Composable
private fun TwoFaPreview() {
    KoinContext {
        val viewModel = TwoFaViewModelImpl(
            validator = koinInject(), authUseCase = koinInject()
        )

        viewModel.setArguments(
            TwoFaArguments(
                validationSid = "",
                redirectUri = "",
                phoneMask = "+7 *** *** ** 50",
                validationType = "sms",
                canResendSms = true,
                wrongCodeError = "Code error".toUiText(),
            )
        )

        viewModel.startTickTimer(60)

        AppTheme {
            TwoFaRoute(
                onAction = { action ->
                    when (action) {
                        is UiAction.CodeInputChanged -> viewModel.onCodeInputChanged(action.newCode)
                        is UiAction.DoneButtonClicked -> viewModel.onDoneButtonClicked()
                        is UiAction.RequestSmsButtonClicked -> viewModel.onRequestSmsButtonClicked()
                        is UiAction.TextFieldDoneClicked -> viewModel.onTextFieldDoneClicked()

                        else -> Unit
                    }
                },
                viewModel = viewModel
            )
        }
    }

}
