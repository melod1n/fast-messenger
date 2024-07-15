package com.meloda.app.fast.auth.captcha.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.meloda.app.fast.auth.captcha.CaptchaViewModel
import com.meloda.app.fast.auth.captcha.CaptchaViewModelImpl
import com.meloda.app.fast.auth.captcha.model.CaptchaScreenState
import com.meloda.app.fast.designsystem.ActionInvokeDismiss
import com.meloda.app.fast.designsystem.MaterialDialog
import com.meloda.app.fast.designsystem.TextFieldErrorText
import org.koin.androidx.compose.koinViewModel
import com.meloda.app.fast.designsystem.R as UiR

@Composable
fun CaptchaRoute(
    onBack: () -> Unit,
    onResult: (String) -> Unit,
    viewModel: CaptchaViewModel = koinViewModel<CaptchaViewModelImpl>()
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val isNeedToOpenLogin by viewModel.isNeedToOpenLogin.collectAsStateWithLifecycle()

    LaunchedEffect(isNeedToOpenLogin) {
        if (isNeedToOpenLogin) {
            viewModel.onNavigatedToLogin()
            onResult(screenState.code)
        }
    }

    CaptchaScreen(
        screenState = screenState,
        onBack = onBack,
        onCodeInputChanged = viewModel::onCodeInputChanged,
        onTextFieldDoneAction = viewModel::onTextFieldDoneAction,
        onDoneButtonClicked = viewModel::onDoneButtonClicked
    )
}

@Composable
fun CaptchaScreen(
    screenState: CaptchaScreenState = CaptchaScreenState.EMPTY,
    onBack: () -> Unit = {},
    onCodeInputChanged: (String) -> Unit = {},
    onTextFieldDoneAction: () -> Unit = {},
    onDoneButtonClicked: () -> Unit = {}
) {
    var confirmedExit by remember {
        mutableStateOf(false)
    }

    var showExitAlert by rememberSaveable {
        mutableStateOf(false)
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
            text = stringResource(id = UiR.string.captcha_exit_warning),
            confirmAction = { confirmedExit = true },
            confirmText = stringResource(id = UiR.string.yes),
            cancelText = stringResource(id = UiR.string.no),
            actionInvokeDismiss = ActionInvokeDismiss.Always
        )
    }

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
                onClick = onBack,
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
                    text = "Captcha",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(38.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "To proceed with your action, enter a code from the picture",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(0.5f)
                    )
                    Spacer(modifier = Modifier.width(24.dp))

                    val imageModifier = Modifier
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clip(RoundedCornerShape(10.dp))
                        .height(48.dp)
                        .width(130.dp)

                    if (LocalView.current.isInEditMode) {
                        Image(
                            painter = painterResource(id = UiR.drawable.test_captcha),
                            contentDescription = "Captcha image",
                            modifier = imageModifier
                        )
                    } else {
                        AsyncImage(
                            model = screenState.captchaImageUrl,
                            contentDescription = "Captcha image",
                            contentScale = ContentScale.FillBounds,
                            modifier = imageModifier
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                var code by remember { mutableStateOf(TextFieldValue(screenState.code)) }
                val showError = screenState.codeError

                TextField(
                    value = code,
                    onValueChange = { newText ->
                        code = newText
                        onCodeInputChanged(newText.text)
                    },
                    label = { Text(text = "Code") },
                    placeholder = { Text(text = "Code") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp)),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = UiR.drawable.round_qr_code_24),
                            contentDescription = "QR code icon",
                            tint = if (showError) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            onTextFieldDoneAction()
                        }
                    ),
                    isError = showError
                )

                AnimatedVisibility(visible = showError) {
                    TextFieldErrorText(text = "Field must not be empty")
                }
            }

            FloatingActionButton(
                onClick = onDoneButtonClicked,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Done,
                    contentDescription = "Done icon",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}
