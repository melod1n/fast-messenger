package com.meloda.fast.screens.twofa.presentation

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
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.meloda.fast.R
import com.meloda.fast.ext.getString
import com.meloda.fast.screens.twofa.TwoFaViewModel
import com.meloda.fast.screens.twofa.TwoFaViewModelImpl
import com.meloda.fast.screens.twofa.model.TwoFaScreenState
import com.meloda.fast.ui.widgets.TextFieldErrorText
import org.koin.androidx.compose.koinViewModel

@Composable
fun TwoFaRoute(
    codeResult: (code: String) -> Unit,
    onBackClicked: () -> Unit,
    viewModel: TwoFaViewModel = koinViewModel<TwoFaViewModelImpl>()
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    if (screenState.isNeedToOpenLogin) {
        viewModel.onNavigatedToLogin()
        onBackClicked()
        codeResult(screenState.twoFaCode)
    }

    TwoFaScreenContent(
        onBackClicked = viewModel::onBackButtonClicked,
        onCodeInputChanged = viewModel::onCodeInputChanged,
        onTextFieldDoneClicked = viewModel::onTextFieldDoneClicked,
        onRequestSmsButtonClicked = viewModel::onRequestSmsButtonClicked,
        onDoneButtonClicked = viewModel::onDoneButtonClicked,
        screenState = screenState
    )
}

@Composable
fun TwoFaScreenContent(
    onBackClicked: () -> Unit,
    onCodeInputChanged: (String) -> Unit,
    onTextFieldDoneClicked: () -> Unit,
    onRequestSmsButtonClicked: () -> Unit,
    onDoneButtonClicked: () -> Unit,
    screenState: TwoFaScreenState,
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        ExtendedFloatingActionButton(
            onClick = onBackClicked,
            text = {
                Text(
                    text = "Cancel",
                    color = MaterialTheme.colorScheme.primary
                )
            },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_round_close_24),
                    contentDescription = null,
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
            Spacer(modifier = Modifier.height(30.dp))

            val delayRemainedTime = screenState.delayTime
            AnimatedVisibility(visible = delayRemainedTime > 0) {
                Text(text = "Can resend after $delayRemainedTime seconds")
            }

            var code by remember { mutableStateOf(TextFieldValue(screenState.twoFaCode)) }
            val codeError = screenState.codeError

            TextField(
                value = code,
                onValueChange = { newText ->
                    code = newText
                    onCodeInputChanged.invoke(newText.text)
                },
                label = { Text(text = "Code") },
                placeholder = { Text(text = "Code") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp)),
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.round_qr_code_24),
                        contentDescription = null,
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
                        onTextFieldDoneClicked.invoke()
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
                    onClick = onRequestSmsButtonClicked,
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
                            contentDescription = null
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
                    painter = painterResource(id = R.drawable.ic_round_done_24),
                    contentDescription = null,
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

