package com.meloda.fast.auth.login.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.meloda.app.fast.common.UiText
import com.meloda.app.fast.designsystem.MaterialDialog
import com.meloda.app.fast.designsystem.TextFieldErrorText
import com.meloda.app.fast.designsystem.autoFillRequestHandler
import com.meloda.app.fast.designsystem.connectNode
import com.meloda.app.fast.designsystem.defaultFocusChangeAutoFill
import com.meloda.app.fast.designsystem.handleEnterKey
import com.meloda.app.fast.designsystem.handleTabKey
import com.meloda.app.fast.model.BaseError
import com.meloda.fast.auth.login.LoginViewModel
import com.meloda.fast.auth.login.LoginViewModelImpl
import com.meloda.fast.auth.login.model.LoginCaptchaArguments
import com.meloda.fast.auth.login.model.LoginError
import com.meloda.fast.auth.login.model.LoginTwoFaArguments
import com.meloda.fast.auth.login.model.LoginUserBannedArguments
import com.theapache64.rebugger.Rebugger
import org.koin.androidx.compose.koinViewModel
import com.meloda.app.fast.designsystem.R as UiR

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginScreen(
    onError: (BaseError) -> Unit,
    onNavigateToUserBanned: (LoginUserBannedArguments) -> Unit,
    onNavigateToMain: () -> Unit,
    onNavigateToCaptcha: (LoginCaptchaArguments) -> Unit,
    onNavigateToTwoFa: (LoginTwoFaArguments) -> Unit,
    twoFaCode: String?,
    captchaCode: String?,
    viewModel: LoginViewModel = koinViewModel<LoginViewModelImpl>()
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val isNeedToOpenMain by viewModel.isNeedToOpenMain.collectAsStateWithLifecycle()
    val userBannedArguments by viewModel.userBannedArguments.collectAsStateWithLifecycle()
    val captchaArguments by viewModel.captchaArguments.collectAsStateWithLifecycle()
    val twoFaArguments by viewModel.twoFaArguments.collectAsStateWithLifecycle()
    val loginError by viewModel.loginError.collectAsStateWithLifecycle()

    LaunchedEffect(isNeedToOpenMain) {
        if (isNeedToOpenMain) {
            viewModel.onNavigatedToMain()
            onNavigateToMain()
        }
    }

    LaunchedEffect(userBannedArguments) {
        userBannedArguments?.let { arguments ->
            viewModel.onNavigatedToUserBanned()
            onNavigateToUserBanned(arguments)
        }
    }

    LaunchedEffect(captchaArguments) {
        captchaArguments?.let { arguments ->
            viewModel.onNavigatedToCaptcha()
            onNavigateToCaptcha(arguments)
        }
    }

    LaunchedEffect(twoFaArguments) {
        twoFaArguments?.let { arguments ->
            viewModel.onNavigatedToTwoFa()
            onNavigateToTwoFa(arguments)
        }
    }

    LaunchedEffect(twoFaCode) {
        if (twoFaCode != null) {
            viewModel.onTwoFaCodeReceived(twoFaCode)
        }
    }

    LaunchedEffect(captchaCode) {
        if (captchaCode != null) {
            viewModel.onCaptchaCodeReceived(captchaCode)
        }
    }

    val focusManager = LocalFocusManager.current
    val (loginFocusable, passwordFocusable) = FocusRequester.createRefs()

    var loginText by remember { mutableStateOf(TextFieldValue(screenState.login)) }
    val showLoginError = screenState.loginError

    val autoFillEmailHandler = autoFillRequestHandler(
        autofillTypes = listOf(AutofillType.EmailAddress),
        onFill = { value ->
            loginText =
                TextFieldValue(text = value, selection = TextRange(value.length))
            viewModel.onLoginInputChanged(value)
        }
    )

    var passwordText by remember { mutableStateOf(TextFieldValue(screenState.password)) }
    val showPasswordError = screenState.passwordError

    val autoFillPasswordHandler = autoFillRequestHandler(
        autofillTypes = listOf(AutofillType.Password),
        onFill = { value ->
            passwordText =
                TextFieldValue(text = value, selection = TextRange(value.length))
            viewModel.onPasswordInputChanged(value)
        }
    )

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(30.dp)
                    .imePadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                ) {
                    Text(
                        text = stringResource(id = UiR.string.sign_in_to_vk),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.displayMedium
                    )

                    Spacer(modifier = Modifier.height(58.dp))

                    TextField(
                        modifier = Modifier
                            .height(58.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .handleEnterKey {
                                passwordFocusable.requestFocus()
                                true
                            }
                            .handleTabKey {
                                passwordFocusable.requestFocus()
                                true
                            }
                            .focusRequester(loginFocusable)
                            .connectNode(handler = autoFillEmailHandler)
                            .defaultFocusChangeAutoFill(handler = autoFillEmailHandler),
                        value = loginText,
                        onValueChange = { newText ->
                            val text = newText.text
                            if (text.isEmpty()) {
                                autoFillEmailHandler.requestVerifyManual()
                            }

                            loginText = newText
                            viewModel.onLoginInputChanged(text)
                        },
                        label = { Text(text = stringResource(id = UiR.string.login_hint)) },
                        placeholder = { Text(text = stringResource(id = UiR.string.login_hint)) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = UiR.drawable.ic_round_person_24),
                                contentDescription = "Login icon",
                                tint = if (showLoginError) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Email
                        ),
                        keyboardActions = KeyboardActions(onNext = { passwordFocusable.requestFocus() }),
                        isError = showLoginError,
                        singleLine = true
                    )
                    AnimatedVisibility(visible = showLoginError) {
                        TextFieldErrorText(text = stringResource(id = UiR.string.error_empty_field))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        modifier = Modifier
                            .height(58.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .handleEnterKey {
                                focusManager.clearFocus()
                                viewModel.onSignInButtonClicked()
                                true
                            }
                            .focusRequester(passwordFocusable)
                            .connectNode(handler = autoFillPasswordHandler)
                            .defaultFocusChangeAutoFill(handler = autoFillPasswordHandler),
                        value = passwordText,
                        onValueChange = { newText ->
                            val text = newText.text
                            if (text.isEmpty()) {
                                autoFillPasswordHandler.requestVerifyManual()
                            }

                            passwordText = newText
                            viewModel.onPasswordInputChanged(text)
                        },
                        label = { Text(text = stringResource(id = UiR.string.password_login_hint)) },
                        placeholder = { Text(text = stringResource(id = UiR.string.password_login_hint)) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = UiR.drawable.round_vpn_key_24),
                                contentDescription = "Password icon",
                                tint = if (showPasswordError) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                        },
                        trailingIcon = {
                            val imagePainter = painterResource(
                                id = if (screenState.passwordVisible) UiR.drawable.round_visibility_off_24
                                else UiR.drawable.round_visibility_24
                            )

                            IconButton(onClick = viewModel::onPasswordVisibilityButtonClicked) {
                                Icon(
                                    painter = imagePainter,
                                    contentDescription = if (screenState.passwordVisible) "Password visible icon"
                                    else "Password invisible icon"
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Go,
                            keyboardType = KeyboardType.Password
                        ),
                        keyboardActions = KeyboardActions(
                            onGo = {
                                focusManager.clearFocus()
                                viewModel.onSignInButtonClicked()
                            }
                        ),
                        isError = showPasswordError,
                        visualTransformation = if (screenState.passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        singleLine = true
                    )
                    AnimatedVisibility(visible = showPasswordError) {
                        TextFieldErrorText(text = stringResource(id = UiR.string.error_empty_field))
                    }
                }

                Box(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    contentAlignment = Alignment.Center
                ) {

                    FloatingActionButton(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.onSignInButtonClicked()
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.testTag("Sign in button")
                    ) {
                        Icon(
                            painter = painterResource(id = UiR.drawable.ic_arrow_end),
                            contentDescription = "Sign in icon",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    AnimatedVisibility(
                        visible = screenState.isLoading,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    HandleError(
        onDismiss = viewModel::onErrorDialogDismissed,
        error = loginError
    )

    Rebugger(
        trackMap = mapOf(
            "onError" to onError,
            "onNavigateToUserBanned" to onNavigateToUserBanned,
            "onNavigateToMain" to onNavigateToMain,
            "onNavigateToCaptcha" to onNavigateToCaptcha,
            "onNavigateToTwoFa" to onNavigateToTwoFa,
            "viewModel" to viewModel,
            "screenState" to screenState,
            "isNeedToOpenMain" to isNeedToOpenMain,
            "userBannedArguments" to userBannedArguments,
            "captchaArguments" to captchaArguments,
            "twoFaArguments" to twoFaArguments,
            "loginError" to loginError,
            "focusManager" to focusManager,
            "loginText" to loginText,
            "showLoginError" to showLoginError,
            "autoFillEmailHandler" to autoFillEmailHandler,
            "passwordText" to passwordText,
            "showPasswordError" to showPasswordError,
            "autoFillPasswordHandler" to autoFillPasswordHandler,
        ),
    )
}

@Composable
fun HandleError(
    onDismiss: () -> Unit,
    error: LoginError?,
) {
    when (error) {
        null -> Unit

        LoginError.Unknown -> {
            MaterialDialog(
                onDismissAction = onDismiss,
                title = UiText.Simple("Error"),
                text = UiText.Simple("Unknown error."),
                confirmText = UiText.Resource(UiR.string.ok)
            )
        }

        LoginError.WrongCredentials -> {
            MaterialDialog(
                onDismissAction = onDismiss,
                title = UiText.Simple("Error"),
                text = UiText.Simple("Wrong login or password."),
                confirmText = UiText.Resource(UiR.string.ok)
            )
        }

        LoginError.TooManyTries -> {
            MaterialDialog(
                onDismissAction = onDismiss,
                title = UiText.Simple("Error"),
                text = UiText.Simple("Too many tries. Try in another hour or later."),
                confirmText = UiText.Resource(UiR.string.ok)
            )
        }


        LoginError.WrongTwoFaCode -> {
            MaterialDialog(
                onDismissAction = onDismiss,
                title = UiText.Simple("Error"),
                text = UiText.Simple("Wrong validation code."),
                confirmText = UiText.Resource(UiR.string.ok)
            )
        }

        LoginError.WrongTwoFaCodeFormat -> {
            MaterialDialog(
                onDismissAction = onDismiss,
                title = UiText.Simple("Error"),
                text = UiText.Simple("Wrong validation code format."),
                confirmText = UiText.Resource(UiR.string.ok)
            )
        }
    }
}
