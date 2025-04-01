package dev.meloda.fast.auth.validation.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
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
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.meloda.fast.auth.validation.ValidationViewModel
import dev.meloda.fast.auth.validation.ValidationViewModelImpl
import dev.meloda.fast.auth.validation.model.ValidationScreenState
import dev.meloda.fast.auth.validation.model.ValidationType
import dev.meloda.fast.ui.components.ActionInvokeDismiss
import dev.meloda.fast.ui.components.MaterialDialog
import dev.meloda.fast.ui.components.TextFieldErrorText
import org.koin.androidx.compose.koinViewModel
import dev.meloda.fast.ui.R as UiR

@Composable
fun ValidationRoute(
    onBack: () -> Unit,
    onResult: (String) -> Unit,
    viewModel: ValidationViewModel = koinViewModel<ValidationViewModelImpl>()
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val isNeedToOpenLogin by viewModel.isNeedToOpenLogin.collectAsStateWithLifecycle()
    val validationType by viewModel.validationType.collectAsStateWithLifecycle()

    LaunchedEffect(isNeedToOpenLogin) {
        if (isNeedToOpenLogin) {
            viewModel.onNavigatedToLogin()

            val code = screenState.code
            if (code == null) {
                onBack()
            } else {
                onResult(code)
            }
        }
    }

    ValidationScreen(
        screenState = screenState,
        validationType = validationType,
        onBack = onBack,
        onCodeInputChanged = viewModel::onCodeInputChanged,
        onTextFieldDoneAction = viewModel::onTextFieldDoneAction,
        onRequestSmsButtonClicked = viewModel::onRequestSmsButtonClicked,
        onDoneButtonClicked = viewModel::onDoneButtonClicked
    )
}

@Composable
fun ValidationScreen(
    screenState: ValidationScreenState = ValidationScreenState.EMPTY,
    validationType: ValidationType? = null,
    onBack: () -> Unit = {},
    onCodeInputChanged: (String) -> Unit = {},
    onTextFieldDoneAction: () -> Unit = {},
    onRequestSmsButtonClicked: () -> Unit = {},
    onDoneButtonClicked: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current

    var confirmedExit by remember {
        mutableStateOf(false)
    }

    var showExitAlert by rememberSaveable {
        mutableStateOf(false)
    }

    val validationText by remember(validationType) {
        mutableStateOf(
            when (validationType) {
                ValidationType.SMS -> "SMS with the code is sent to ${screenState.phoneMask}"
                ValidationType.APP -> "Enter the code from the code generator application"

                null -> ""
            }
        )
    }

    LaunchedEffect(confirmedExit) {
        if (confirmedExit) {
            onBack()
        }
    }

    BackHandler(enabled = !confirmedExit) {
        if (!confirmedExit) {
            showExitAlert = true
        }
    }

    if (showExitAlert) {
        MaterialDialog(
            onDismissRequest = { showExitAlert = false },
            title = stringResource(id = UiR.string.warning_confirmation),
            text = stringResource(id = UiR.string.validation_exit_warning),
            confirmAction = { confirmedExit = true },
            confirmText = stringResource(id = UiR.string.yes),
            cancelText = stringResource(id = UiR.string.no),
            actionInvokeDismiss = ActionInvokeDismiss.Always
        )
    }

    var code by remember { mutableStateOf(TextFieldValue(screenState.code.orEmpty())) }

    Scaffold(
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.union(WindowInsets.ime)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(30.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            ExtendedFloatingActionButton(
                onClick = onBack,
                text = {
                    Text(
                        text = "Cancel",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close icon",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
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
                    text = validationText.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))

                val isResendTextVisible by remember {
                    derivedStateOf { screenState.delayTime > 0 }
                }
                AnimatedVisibility(visible = isResendTextVisible) {
                    Text(
                        text = "Can resend after ${screenState.delayTime} seconds",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                TextField(
                    value = code,
                    onValueChange = { newText ->
                        if (newText.text.length > 6) return@TextField

                        code = newText
                        onCodeInputChanged(newText.text)
                    },
                    label = { Text(text = "Code") },
                    placeholder = { Text(text = "Code") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .semantics { contentType = ContentType.SmsOtpCode },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = UiR.drawable.round_qr_code_24),
                            contentDescription = "QR Code icon",
                            tint = if (screenState.codeError) {
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
                            onTextFieldDoneAction()
                        }
                    ),
                    isError = screenState.codeError
                )

                AnimatedVisibility(screenState.codeError) {
                    TextFieldErrorText(text = "Field must not be empty")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val canResendSms = screenState.isSmsButtonVisible

                AnimatedVisibility(
                    visible = canResendSms,
                ) {
                    ExtendedFloatingActionButton(
                        onClick = onRequestSmsButtonClicked,
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
                    onClick = onDoneButtonClicked,
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
private fun ValidationScreenPreview() {
    ValidationScreen(
        screenState = ValidationScreenState.EMPTY.copy(
            phoneMask = "+7 (***) ***-**-21",
            code = "222222"
        ),
        validationType = ValidationType.SMS
    )
}
