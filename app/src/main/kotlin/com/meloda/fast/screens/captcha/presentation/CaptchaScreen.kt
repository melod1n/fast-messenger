package com.meloda.fast.screens.captcha.presentation

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.request.ImageRequest
import com.meloda.fast.R
import com.meloda.fast.screens.captcha.model.CaptchaScreenState
import com.meloda.fast.ui.widgets.CoilImage
import com.meloda.fast.ui.widgets.TextFieldErrorText

@Composable
fun CaptchaRoute(
    codeResult: (code: String) -> Unit,
    onBackClicked: () -> Unit,
    viewModel: CaptchaViewModel
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    if (screenState.isNeedToOpenLogin) {
        viewModel.onNavigatedToLogin()
        onBackClicked()
        codeResult(screenState.captchaCode)
    }

    CaptchaScreen(
        onCancelButtonClicked = onBackClicked,
        onCodeInputChanged = viewModel::onCodeInputChanged,
        onTextFieldDoneClicked = viewModel::onTextFieldDoneClicked,
        onDoneButtonClicked = viewModel::onDoneButtonClicked,
        screenState = screenState
    )
}

@Composable
fun CaptchaScreen(
    onCancelButtonClicked: () -> Unit,
    onCodeInputChanged: (String) -> Unit,
    onTextFieldDoneClicked: () -> Unit,
    onDoneButtonClicked: () -> Unit,
    screenState: CaptchaScreenState,
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        ExtendedFloatingActionButton(
            onClick = onCancelButtonClicked,
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

                CoilImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(screenState.captchaImage)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clip(RoundedCornerShape(10.dp))
                        .height(48.dp)
                        .width(130.dp),
                    contentScale = ContentScale.FillBounds,
                    previewPainter = painterResource(id = R.drawable.test_captcha)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            var code by remember { mutableStateOf(TextFieldValue(screenState.captchaCode)) }
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
                        painter = painterResource(id = R.drawable.round_qr_code_24),
                        contentDescription = null,
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
                        onTextFieldDoneClicked()
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
                painter = painterResource(id = R.drawable.ic_round_done_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
